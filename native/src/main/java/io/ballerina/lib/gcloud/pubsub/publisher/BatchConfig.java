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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils.decimalToMillis;

/**
 * Message batching settings for the Publisher.
 *
 * @param maxDelay        The maximum delay before sending a batch (in milliseconds)
 * @param maxMessageCount The maximum number of messages to include in a single batch
 * @param maxBytes        The maximum total size in bytes for a single batch
 */
public record BatchConfig(
        long maxDelay,
        long maxMessageCount,
        long maxBytes
) {
    private static final BString MAX_DELAY = StringUtils.fromString("maxDelay");
    private static final BString MAX_MESSAGE_COUNT = StringUtils.fromString("maxMessageCount");
    private static final BString MAX_BYTES = StringUtils.fromString("maxBytes");

    /**
     * Constructor to create BatchConfig from Ballerina configuration map.
     *
     * @param config Ballerina configuration map
     */
    public BatchConfig(BMap<BString, Object> config) {
        this(
                decimalToMillis(((BDecimal) config.get(MAX_DELAY)).decimalValue()),
                config.getIntValue(MAX_MESSAGE_COUNT),
                config.getIntValue(MAX_BYTES)
        );
    }
}
