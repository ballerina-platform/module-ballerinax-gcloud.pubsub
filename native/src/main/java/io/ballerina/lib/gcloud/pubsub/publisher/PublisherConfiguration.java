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

import io.ballerina.lib.gcloud.pubsub.config.GcpCredentialConfig;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Represents the configuration for a Google Cloud Pub/Sub Publisher.
 *
 * @param batchConfig           Message batching settings for the Publisher
 * @param compression           Message compression configurations
 * @param auth                  GCP pub/sub authentication configurations
 * @param enableMessageOrdering Enable message ordering to ensure messages are received in the order they were published
 * @param retryConfig           Retry configuration for handling transient failures
 */
public record PublisherConfiguration(
        BatchConfig batchConfig,
        Compression compression,
        GcpCredentialConfig auth,
        boolean enableMessageOrdering,
        RetryConfig retryConfig
) {

    private static final BString BATCH_CONFIG = StringUtils.fromString("batchConfig");
    private static final BString COMPRESSION = StringUtils.fromString("compression");
    private static final BString AUTH = StringUtils.fromString("auth");
    private static final BString ENABLE_MESSAGE_ORDERING = StringUtils.fromString("enableMessageOrdering");
    private static final BString RETRY_CONFIG = StringUtils.fromString("retryConfig");

    /**
     * Constructor to create PublisherConfiguration from Ballerina configuration map.
     *
     * @param config Ballerina configuration map
     */
    public PublisherConfiguration(BMap<BString, Object> config) {
        this(
                getBatchConfig(config),
                getCompression(config),
                getAuth(config),
                config.getBooleanValue(ENABLE_MESSAGE_ORDERING),
                getRetryConfig(config)
        );
    }

    private static BatchConfig getBatchConfig(BMap<BString, Object> config) {
        if (!config.containsKey(BATCH_CONFIG)) {
            return null;
        }
        BMap<BString, Object> batchConfigMap = (BMap<BString, Object>) config.getMapValue(BATCH_CONFIG);
        return new BatchConfig(batchConfigMap);
    }

    private static Compression getCompression(BMap<BString, Object> config) {
        if (!config.containsKey(COMPRESSION)) {
            return null;
        }
        BMap<BString, Object> compressionMap = (BMap<BString, Object>) config.getMapValue(COMPRESSION);
        return new Compression(compressionMap);
    }

    private static GcpCredentialConfig getAuth(BMap<BString, Object> config) {
        if (!config.containsKey(AUTH)) {
            return null;
        }
        BMap<BString, Object> authMap = (BMap<BString, Object>) config.getMapValue(AUTH);
        return new GcpCredentialConfig(authMap);
    }

    private static RetryConfig getRetryConfig(BMap<BString, Object> config) {
        if (!config.containsKey(RETRY_CONFIG)) {
            return null;
        }
        BMap<BString, Object> retryConfigMap = (BMap<BString, Object>) config.getMapValue(RETRY_CONFIG);
        return new RetryConfig(retryConfigMap);
    }
}
