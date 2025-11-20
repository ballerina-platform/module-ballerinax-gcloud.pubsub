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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Flow control configuration for managing outstanding messages.
 *
 * @param maxOutstandingMessageCount Maximum number of outstanding messages
 * @param maxOutstandingRequestBytes Maximum total size of outstanding messages in bytes
 */
public record FlowControlConfig(
        long maxOutstandingMessageCount,
        long maxOutstandingRequestBytes) {

    private static final BString MAX_OUTSTANDING_MESSAGE_COUNT = StringUtils.fromString(
            "maxOutstandingMessageCount");
    private static final BString MAX_OUTSTANDING_REQUEST_BYTES = StringUtils.fromString(
            "maxOutstandingRequestBytes");

    /**
     * Constructor to create FlowControlConfig from Ballerina configuration.
     *
     * @param config Ballerina configuration map
     */
    public FlowControlConfig(BMap<BString, Object> config) {
        this(
                config.getIntValue(MAX_OUTSTANDING_MESSAGE_COUNT),
                config.getIntValue(MAX_OUTSTANDING_REQUEST_BYTES)
        );
    }
}
