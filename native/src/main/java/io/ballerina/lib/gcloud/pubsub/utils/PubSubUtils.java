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

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

/**
 * Utility functions for Google Cloud Pub/Sub module.
 */
public class PubSubUtils {

    private static Module pubsubModule;

    /**
     * Sets the Pub/Sub module from the runtime environment.
     *
     * @param env Ballerina runtime environment
     */
    public static void setModule(Environment env) {
        pubsubModule = env.getCurrentModule();
    }

    /**
     * Gets the Pub/Sub module.
     *
     * @return Module instance
     */
    public static Module getModule() {
        return pubsubModule;
    }

    /**
     * Creates a Pub/Sub error.
     *
     * @param message Error message
     * @return BError instance
     */
    public static BError createError(String message) {
        return ErrorCreator.createDistinctError(PubSubConstants.ERROR_TYPE, getModule(),
                StringUtils.fromString(message));
    }

    /**
     * Creates a Pub/Sub error with a cause.
     *
     * @param message Error message
     * @param cause Cause of the error
     * @return BError instance
     */
    public static BError createError(String message, Throwable cause) {
        BError causeError = ErrorCreator.createError(cause);
        return ErrorCreator.createDistinctError(PubSubConstants.ERROR_TYPE, getModule(),
                StringUtils.fromString(message), causeError);
    }

    /**
     * Creates an empty record of the given type.
     *
     * @param recordTypeName Name of the record type
     * @return BMap instance
     */
    public static BMap<BString, Object> createRecord(String recordTypeName) {
        return ValueCreator.createRecordValue(getModule(), recordTypeName);
    }

    private PubSubUtils() {
    }
}
