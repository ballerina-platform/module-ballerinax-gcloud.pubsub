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

import io.ballerina.lib.gcloud.pubsub.config.GcpCredentialConfig;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Represents the configuration for a Google Cloud Pub/Sub listener.
 *
 * @param auth GCP pub/sub authentication configurations
 */
public record ListenerConfiguration(GcpCredentialConfig auth) {

    private static final BString AUTH = StringUtils.fromString("auth");

    /**
     * Constructor to create ListenerConfiguration from Ballerina configuration map.
     *
     * @param config Ballerina configuration map
     */
    public ListenerConfiguration(BMap<BString, Object> config) {
        this(
                !config.containsKey(AUTH) ? null:
                        new GcpCredentialConfig((BMap<BString, Object>) config.getMapValue(AUTH))
        );
    }
}
