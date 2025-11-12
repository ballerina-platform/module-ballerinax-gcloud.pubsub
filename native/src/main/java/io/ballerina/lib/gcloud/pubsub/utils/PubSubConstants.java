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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Constants for Google Cloud Pub/Sub module.
 */
public class PubSubConstants {

    // Module constants
    public static final String PACKAGE_ORG = "ballerinax";
    public static final String PACKAGE_NAME = "gcloud.pubsub";

    // Native data keys
    public static final String NATIVE_PUBLISHER = "NATIVE_PUBLISHER";
    public static final String NATIVE_SUBSCRIBER = "NATIVE_SUBSCRIBER";
    public static final String NATIVE_SUBSCRIPTION_NAME = "NATIVE_SUBSCRIPTION_NAME";
    public static final String ACK_REPLY_CONSUMER = "ACK_REPLY_CONSUMER";
    public static final String CONSUMER_SERVICES = "CONSUMER_SERVICES";
    public static final String STARTED_SERVICES = "STARTED_SERVICES";
    public static final String CONFIG = "CONFIG";

    // Error type
    public static final String ERROR_TYPE = "Error";

    // Ballerina types
    public static final String CALLER = "Caller";
    public static final String ON_MESSAGE = "onMessage";
    public static final String MESSAGE = "Message";

    // Ballerina type field names
    public static final BString PROJECT_ID_FIELD = StringUtils.fromString("projectId");
    public static final BString CREDENTIALS_FIELD = StringUtils.fromString("credentials");
    public static final BString ENABLE_BATCHING_FIELD = StringUtils.fromString("enableBatching");
    public static final BString BATCH_SETTINGS_FIELD = StringUtils.fromString("batchSettings");
    public static final BString ENABLE_MESSAGE_ORDERING_FIELD = StringUtils.fromString("enableMessageOrdering");

    public static final BString ELEMENT_COUNT_THRESHOLD_FIELD = StringUtils.fromString("elementCountThreshold");
    public static final BString REQUEST_BYTE_SIZE_THRESHOLD_FIELD = StringUtils.fromString("requestByteSizeThreshold");
    public static final BString DELAY_THRESHOLD_MILLIS_FIELD = StringUtils.fromString("delayThresholdMillis");

    public static final BString DATA_FIELD = StringUtils.fromString("data");
    public static final BString ATTRIBUTES_FIELD = StringUtils.fromString("attributes");
    public static final BString MESSAGE_ID_FIELD = StringUtils.fromString("messageId");
    public static final BString PUBLISH_TIME_FIELD = StringUtils.fromString("publishTime");
    public static final BString ORDERING_KEY_FIELD = StringUtils.fromString("orderingKey");
    public static final BString CREDENTIALS_PATH_FIELD = StringUtils.fromString("credentialsPath");
    public static final BString CREDENTIALS_JSON_FIELD = StringUtils.fromString("credentialsJson");

    private PubSubConstants() {
    }
}
