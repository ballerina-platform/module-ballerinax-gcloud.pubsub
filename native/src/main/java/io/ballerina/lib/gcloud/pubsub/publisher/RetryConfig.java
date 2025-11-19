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
 * GCP Pub/Sub client retry configuration for handling transient failures.
 *
 * @param maxAttempts Maximum number of retry attempts for a failed publish operation
 * @param initialRetryDelay Initial delay before the first retry attempt (in milliseconds)
 * @param initialRpcTimeout Initial timeout for the first RPC call (in milliseconds)
 * @param maxRetryDelay Maximum delay between retry attempts (in milliseconds)
 * @param maxRpcTimeout Maximum timeout for any single RPC call (in milliseconds)
 * @param retryDelayMultiplier Multiplier applied to the retry delay after each failed attempt for exponential backoff
 * @param rpcTimeoutMultiplier Multiplier applied to the RPC timeout after each failed attempt
 * @param totalTimeout Total timeout for all retry attempts combined (in milliseconds)
 */
public record RetryConfig(
        int maxAttempts,
        long initialRetryDelay,
        long initialRpcTimeout,
        long maxRetryDelay,
        long maxRpcTimeout,
        double retryDelayMultiplier,
        double rpcTimeoutMultiplier,
        long totalTimeout
) {
    private static final BString MAX_ATTEMPTS = StringUtils.fromString("maxAttempts");
    private static final BString INITIAL_RETRY_DELAY = StringUtils.fromString("initialRetryDelay");
    private static final BString INITIAL_RPC_TIMEOUT = StringUtils.fromString("initialRpcTimeout");
    private static final BString MAX_RETRY_DELAY = StringUtils.fromString("maxRetryDelay");
    private static final BString MAX_RPC_TIMEOUT = StringUtils.fromString("maxRpcTimeout");
    private static final BString RETRY_DELAY_MULTIPLIER = StringUtils.fromString("retryDelayMultiplier");
    private static final BString RPC_TIMEOUT_MULTIPLIER = StringUtils.fromString("rpcTimeoutMultiplier");
    private static final BString TOTAL_TIMEOUT = StringUtils.fromString("totalTimeout");

    /**
     * Constructor to create RetryConfig from Ballerina configuration map.
     *
     * @param config Ballerina configuration map
     */
    public RetryConfig(BMap<BString, Object> config) {
        this(
                config.getIntValue(MAX_ATTEMPTS).intValue(),
                decimalToMillis(((BDecimal) config.get(INITIAL_RETRY_DELAY)).decimalValue()),
                decimalToMillis(((BDecimal) config.get(INITIAL_RPC_TIMEOUT)).decimalValue()),
                decimalToMillis(((BDecimal) config.get(MAX_RETRY_DELAY)).decimalValue()),
                decimalToMillis(((BDecimal) config.get(MAX_RPC_TIMEOUT)).decimalValue()),
                config.getFloatValue(RETRY_DELAY_MULTIPLIER),
                config.getFloatValue(RPC_TIMEOUT_MULTIPLIER),
                decimalToMillis(((BDecimal) config.get(TOTAL_TIMEOUT)).decimalValue())
        );
    }
}
