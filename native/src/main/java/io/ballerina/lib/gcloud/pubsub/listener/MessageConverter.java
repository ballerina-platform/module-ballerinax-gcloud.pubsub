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
import io.ballerina.lib.gcloud.pubsub.ModuleUtils;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.time.nativeimpl.Utc;

import java.time.Instant;
import java.util.Map;

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
     * @return Ballerina Pub/Sub Message record
     */
    static BMap<BString, Object> toBallerinaMessage(PubsubMessage message) {
        BMap<BString, Object> bMessage = ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE);
        bMessage.put(MESSAGE_ID_FIELD, StringUtils.fromString(message.getMessageId()));

        byte[] data = message.getData().toByteArray();
        bMessage.put(DATA_FIELD, ValueCreator.createArrayValue(data));

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
}
