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

package io.ballerina.lib.gcloud.pubsub.publisher;

import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;

/**
 * Converter for Ballerina messages to GCP Pub/SUb {@link PubsubMessage}.
 */
public final class MessageConverter {
    public static final BString DATA_FIELD = StringUtils.fromString("data");
    public static final BString ATTRIBUTES_FIELD = StringUtils.fromString("attributes");
    public static final BString ORDERING_KEY_FIELD = StringUtils.fromString("orderingKey");

    private MessageConverter() {}

    static PubsubMessage toPubSubMessage(BMap<BString, Object> message) {
        PubsubMessage.Builder builder = PubsubMessage.newBuilder();
        BArray data = message.getArrayValue(DATA_FIELD);
        builder.setData(ByteString.copyFrom(data.getBytes()));

        if (message.containsKey(ATTRIBUTES_FIELD)) {
            BMap<BString, BString> attributes = (BMap<BString, BString>) message.get(ATTRIBUTES_FIELD);
            for (Map.Entry<BString, BString> entry : attributes.entrySet()) {
                builder.putAttributes(entry.getKey().getValue(), entry.getValue().getValue());
            }
        }

        if (message.containsKey(ORDERING_KEY_FIELD)) {
            String orderingKey = message.getStringValue(ORDERING_KEY_FIELD).getValue();
            if (orderingKey != null && !orderingKey.isEmpty()) {
                builder.setOrderingKey(orderingKey);
            }
        }
        return builder.build();
    }
}
