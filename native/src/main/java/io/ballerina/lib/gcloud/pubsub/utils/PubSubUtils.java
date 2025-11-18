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

package io.ballerina.lib.gcloud.pubsub.utils;

import io.ballerina.lib.gcloud.pubsub.ModuleUtils;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;

import java.math.BigDecimal;

/**
 * Utility functions for Google Cloud Pub/Sub module.
 */
public class PubSubUtils {
    public static final String PUBSUB_ERROR = "Error";

    /**
     * Creates a Ballerina error with given message.
     *
     * @param message error message
     * @return Ballerina error
     */
    public static BError createError(String message) {
        return ErrorCreator.createError(ModuleUtils.getModule(), PUBSUB_ERROR,
                StringUtils.fromString(message), null, null);
    }

    /**
     * Creates a Ballerina error with given message and cause.
     *
     * @param message error message
     * @param cause   exception cause
     * @return Ballerina error
     */
    public static BError createError(String message, Throwable cause) {
        return ErrorCreator.createError(ModuleUtils.getModule(), PUBSUB_ERROR,
                StringUtils.fromString(message), ErrorCreator.createError(cause), null);
    }

    public static long decimalToMillis(BigDecimal seconds) {
        return seconds.multiply(BigDecimal.valueOf(1000)).longValue();
    }

    private PubSubUtils() {
    }
}
