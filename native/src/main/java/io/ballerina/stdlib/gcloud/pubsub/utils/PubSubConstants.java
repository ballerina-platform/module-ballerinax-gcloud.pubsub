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

package io.ballerina.stdlib.gcloud.pubsub.utils;

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
    public static final String NATIVE_SUBSCRIBER_STUB = "NATIVE_SUBSCRIBER_STUB";
    public static final String NATIVE_MESSAGE_RECEIVER = "NATIVE_MESSAGE_RECEIVER";
    public static final String NATIVE_SUBSCRIPTION_NAME = "NATIVE_SUBSCRIPTION_NAME";

    // Configuration keys
    public static final String PROJECT_ID = "projectId";
    public static final String CREDENTIALS = "credentials";
    public static final String CREDENTIALS_PATH = "credentialsPath";
    public static final String CREDENTIALS_JSON = "credentialsJson";
    public static final String TOPIC_NAME = "topicName";
    public static final String SUBSCRIPTION_NAME = "subscriptionName";

    // Error type
    public static final String ERROR_TYPE = "Error";

    private PubSubConstants() {
    }
}
