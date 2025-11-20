/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.gcloud.pubsub.listener;

import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.PubsubMessage;
import io.ballerina.lib.gcloud.pubsub.DataBindingException;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.stdlib.time.nativeimpl.Utc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

import static io.ballerina.runtime.api.types.TypeTags.STRING_TAG;
import static io.ballerina.runtime.api.types.TypeTags.UNION_TAG;

/**
 * Converter for GCP Pub/Sub message to a Ballerina message.
 */
public final class MessageConverter {
    private static final String MESSAGE = "Message";
    private static final BString DATA_FIELD = StringUtils.fromString("data");
    private static final BString ATTRIBUTES_FIELD = StringUtils.fromString("attributes");
    private static final BString MESSAGE_ID_FIELD = StringUtils.fromString("messageId");
    private static final BString PUBLISH_TIME_FIELD = StringUtils.fromString("publishTime");
    private static final BString ORDERING_KEY_FIELD = StringUtils.fromString("orderingKey");

    private MessageConverter() {}

    /**
     * Converts a Pub/Sub message to a Ballerina Pub/Sub Message record.
     *
     * @param message The Pub/Sub message
     * @param expectedType The expected message record type
     * @return Ballerina Pub/Sub Message record
     */
    static BMap<BString, Object> toBallerinaMessage(PubsubMessage message, BTypedesc expectedType)
            throws DataBindingException {
        RecordType messageType = getRecordType(expectedType);
        RecordType recordType = getRecordType(messageType);
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(recordType);

        bMessage.put(MESSAGE_ID_FIELD, StringUtils.fromString(message.getMessageId()));

        byte[] msgData = message.getData().toByteArray();
        Object data = getMessageDataWithIntendedBType(msgData, recordType);
        bMessage.put(DATA_FIELD, data);

        if (!message.getAttributesMap().isEmpty()) {
            BMap<BString, Object> attributes = ValueCreator.createMapValue();
            for (Map.Entry<String, String> entry : message.getAttributesMap().entrySet()) {
                attributes.put(StringUtils.fromString(entry.getKey()), StringUtils.fromString(entry.getValue()));
            }
            bMessage.put(ATTRIBUTES_FIELD, attributes);
        }

        Timestamp publishTime = message.getPublishTime();
        Instant instant = Instant.ofEpochSecond(publishTime.getSeconds(), publishTime.getNanos());
        bMessage.put(PUBLISH_TIME_FIELD, new Utc(instant).build());

        String orderingKey = message.getOrderingKey();
        if (!orderingKey.isEmpty()) {
            bMessage.put(ORDERING_KEY_FIELD, StringUtils.fromString(orderingKey));
        }

        return bMessage;
    }

    private static RecordType getRecordType(BTypedesc bTypedesc) {
        RecordType recordType;
        if (bTypedesc.getDescribingType().isReadOnly()) {
            recordType = (RecordType) ((IntersectionType) (bTypedesc.getDescribingType())).getConstituentTypes().get(0);
        } else {
            recordType = (RecordType) bTypedesc.getDescribingType();
        }
        return recordType;
    }

    private static RecordType getRecordType(Type type) {
        if (type.getTag() == TypeTags.INTERSECTION_TAG) {
            return (RecordType) TypeUtils.getReferredType(((IntersectionType) (type)).getConstituentTypes().get(0));
        }
        return (RecordType) type;
    }

    private static Object getMessageDataWithIntendedBType(byte[] msgData, RecordType recordType)
            throws DataBindingException {
        Type intendedType = TypeUtils.getReferredType(recordType.getFields().get(DATA_FIELD.getValue()).getFieldType());
        return constructMessageDataWithBType(msgData, intendedType);
    }

    private static Object constructMessageDataWithBType(byte[] data, Type type) throws DataBindingException {
        String strValue = new String(data, StandardCharsets.UTF_8);
        try {
            switch (type.getTag()) {
                case STRING_TAG:
                    return StringUtils.fromString(strValue);
                case TypeTags.XML_TAG:
                    return XmlUtils.parse(strValue);
                case TypeTags.ANYDATA_TAG:
                    return ValueCreator.createArrayValue(data);
                case TypeTags.RECORD_TYPE_TAG:
                    return ValueUtils.convert(JsonUtils.parse(strValue), type);
                case UNION_TAG:
                    if (hasStringType((UnionType) type)) {
                        return StringUtils.fromString(strValue);
                    }
                    return getValueFromJson(type, strValue);
                case TypeTags.ARRAY_TAG:
                    if (TypeUtils.getReferredType(((ArrayType) type).getElementType()).getTag() == TypeTags.BYTE_TAG) {
                        return ValueCreator.createArrayValue(data);
                    }
                    /*-fallthrough*/
                default:
                    return getValueFromJson(type, strValue);
            }
        } catch (BError bError) {
            String errMsg = String.format("Data binding failed: %s", bError.getMessage());
            throw new DataBindingException(errMsg, bError);
        }
    }

    private static boolean hasStringType(UnionType type) {
        return type.getMemberTypes().stream().anyMatch(memberType -> memberType.getTag() == STRING_TAG);
    }

    private static Object getValueFromJson(Type type, String stringValue) {
        return ValueUtils.convert(JsonUtils.parse(stringValue), type);
    }
}
