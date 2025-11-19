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
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils.decimalToMillis;

/**
 * GCP Pub/Sub service configurations.
 *
 * @param subscription               The name of the Pub/Sub subscription to pull messages from
 * @param maxAckExtensionPeriod      Maximum period a message ack deadline will be extended (in milliseconds)
 * @param maxDurationPerAckExtension Upper bound for a single ack deadline extension period (in milliseconds)
 * @param minDurationPerAckExtension Minimum duration for each acknowledgment deadline extension (in milliseconds)
 * @param parallelPullCount          Number of StreamingPull streams to pull messages from the subscription
 * @param flowControlSettings        Flow control settings for managing outstanding messages
 */
public record ServiceConfig(
        String subscription,
        long maxAckExtensionPeriod,
        long maxDurationPerAckExtension,
        long minDurationPerAckExtension,
        long parallelPullCount,
        FlowControlConfig flowControlSettings) {

    private static final BString SUBSCRIPTION = StringUtils.fromString("subscription");
    private static final BString MAX_ACK_EXTENSION_PERIOD = StringUtils.fromString("maxAckExtensionPeriod");
    private static final BString MAX_DURATION_PER_ACK_EXTENSION = StringUtils.fromString(
            "maxDurationPerAckExtension");
    private static final BString MIN_DURATION_PER_ACK_EXTENSION = StringUtils.fromString(
            "minDurationPerAckExtension");
    private static final BString PARALLEL_PULL_COUNT = StringUtils.fromString("parallelPullCount");
    private static final BString FLOW_CONTROL_SETTINGS = StringUtils.fromString("flowControlSettings");

    /**
     * Constructor to create ServiceConfig from Ballerina GCP Pub/Sub service annotation.
     *
     * @param config Ballerina configuration map
     */
    public ServiceConfig(BMap<BString, Object> config) {
        this(
                config.getStringValue(SUBSCRIPTION).getValue(),
                decimalToMillis(((BDecimal) config.get(MAX_ACK_EXTENSION_PERIOD)).decimalValue()),
                decimalToMillis(((BDecimal) config.get(MAX_DURATION_PER_ACK_EXTENSION)).decimalValue()),
                decimalToMillis(((BDecimal) config.get(MIN_DURATION_PER_ACK_EXTENSION)).decimalValue()),
                config.getIntValue(PARALLEL_PULL_COUNT),
                getFlowControlConfig(config)
        );
    }

    private static FlowControlConfig getFlowControlConfig(BMap<BString, Object> config) {
        if (!config.containsKey(FLOW_CONTROL_SETTINGS)) {
            return null;
        }
        BMap<BString, Object> flowControlConfig = (BMap<BString, Object>) config.getMapValue(FLOW_CONTROL_SETTINGS);
        return new FlowControlConfig(flowControlConfig);
    }
}
