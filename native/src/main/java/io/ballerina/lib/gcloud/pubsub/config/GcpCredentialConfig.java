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

package io.ballerina.lib.gcloud.pubsub.config;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * GCP Pub/Sub authentication configurations.
 *
 * @param path Path to the service account JSON key file
 */
public record GcpCredentialConfig(
        String path
) {
    private static final BString PATH = StringUtils.fromString("path");

    /**
     * Constructor to create GcpCredentialConfig from Ballerina configuration map.
     *
     * @param config Ballerina configuration map
     */
    public GcpCredentialConfig(BMap<BString, Object> config) {
        this(
                config.containsKey(PATH) ?
                        config.getStringValue(PATH).getValue() : null
        );
    }
}
