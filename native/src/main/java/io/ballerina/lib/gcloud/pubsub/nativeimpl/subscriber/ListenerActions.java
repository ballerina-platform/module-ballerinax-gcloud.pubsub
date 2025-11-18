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

package io.ballerina.lib.gcloud.pubsub.nativeimpl.subscriber;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.CONFIG;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.CONSUMER_SERVICES;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.CREDENTIALS_FIELD;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.CREDENTIALS_JSON_FIELD;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.CREDENTIALS_PATH_FIELD;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.PROJECT_ID_FIELD;
import static io.ballerina.lib.gcloud.pubsub.utils.PubSubConstants.STARTED_SERVICES;

/**
 * Native implementation for Google Cloud Pub/Sub Listener actions.
 */
public class ListenerActions {
    /**
     * Initializes the Google Cloud Pub/Sub listener.
     *
     * @param listenerObject Listener object from Ballerina
     * @param subscriptionName Subscription name
     * @param config Subscriber configuration
     * @return Error if initialization fails, null otherwise
     */
    public static Object init(BObject listenerObject, BString subscriptionName, BMap<BString, Object> config) {
        try {
            String subscription = subscriptionName.getValue();
            String projectId = config.getStringValue(PROJECT_ID_FIELD).getValue();

            // Parse subscription name
            ProjectSubscriptionName subscriptionFullName;
            if (subscription.startsWith("projects/")) {
                String[] parts = subscription.split("/");
                if (parts.length == 4 && "subscriptions".equals(parts[2])) {
                    subscriptionFullName = ProjectSubscriptionName.of(parts[1], parts[3]);
                } else {
                    return PubSubUtils.createError("Invalid subscription name format. " +
                            "Expected: projects/{project}/subscriptions/{subscription}");
                }
            } else {
                subscriptionFullName = ProjectSubscriptionName.of(projectId, subscription);
            }

            listenerObject.addNativeData(PubSubConstants.NATIVE_SUBSCRIPTION_NAME, subscriptionFullName);
            listenerObject.addNativeData(CONFIG, config);
            listenerObject.addNativeData(CONSUMER_SERVICES, new ArrayList<BObject>());
            listenerObject.addNativeData(STARTED_SERVICES, new ArrayList<BObject>());

            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to initialize listener: " + e.getMessage(), e);
        }
    }

    /**
     * Attaches a service to the listener.
     *
     * @param environment Ballerina runtime environment
     * @param listenerObject Listener object from Ballerina
     * @param serviceObject Service object to register
     * @param name Service name (optional)
     * @return Error if registration fails, null otherwise
     */
    public static Object attach(Environment environment, BObject listenerObject, BObject serviceObject,
                                Object name) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<BObject> services = (ArrayList<BObject>) listenerObject.getNativeData(CONSUMER_SERVICES);
            if (services == null) {
                services = new ArrayList<>();
                listenerObject.addNativeData(CONSUMER_SERVICES, services);
            }
            services.add(serviceObject);
            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to register service: " + e.getMessage(), e);
        }
    }

    /**
     * Starts the listener and begins consuming messages.
     *
     * @param environment Ballerina runtime environment
     * @param listenerObject Listener object from Ballerina
     * @return Error if start fails, null otherwise
     */
    public static Object start(Environment environment, BObject listenerObject) {
        try {
            ProjectSubscriptionName subscription = (ProjectSubscriptionName) listenerObject.getNativeData(
                    PubSubConstants.NATIVE_SUBSCRIPTION_NAME);
            BMap<BString, Object> config = (BMap<BString, Object>) listenerObject.getNativeData(CONFIG);

            @SuppressWarnings("unchecked")
            ArrayList<BObject> services = (ArrayList<BObject>) listenerObject.getNativeData(CONSUMER_SERVICES);
            @SuppressWarnings("unchecked")
            ArrayList<BObject> startedServices = (ArrayList<BObject>) listenerObject.getNativeData(STARTED_SERVICES);

            if (services == null || services.isEmpty()) {
                return null;
            }

            Runtime runtime = environment.getRuntime();

            for (BObject service : services) {
                if (startedServices != null && !startedServices.contains(service)) {
                    // Get credentials
                    GoogleCredentials credentials = getCredentials(config);

                    // Create message receiver with dispatcher
                    MessageDispatcher dispatcher = new MessageDispatcher(service, runtime);
                    MessageReceiver receiver = dispatcher.getMessageReceiver();

                    // Build subscriber
                    Subscriber.Builder subscriberBuilder = Subscriber.newBuilder(subscription, receiver);

                    if (credentials != null) {
                        subscriberBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
                    }

                    Subscriber subscriber = subscriberBuilder.build();

                    // Store subscriber
                    service.addNativeData(PubSubConstants.NATIVE_SUBSCRIBER, subscriber);

                    // Start the subscriber
                    subscriber.startAsync().awaitRunning();

                    startedServices.add(service);
                }
            }

            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to start listener: " + e.getMessage(), e);
        }
    }

    /**
     * Detaches a service from the listener.
     *
     * @param environment Ballerina runtime environment
     * @param listenerObject Listener object from Ballerina
     * @param serviceObject Service object to unregister
     * @return Error if unregistration fails, null otherwise
     */
    public static Object detach(Environment environment, BObject listenerObject, BObject serviceObject) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<BObject> services = (ArrayList<BObject>) listenerObject.getNativeData(CONSUMER_SERVICES);
            @SuppressWarnings("unchecked")
            ArrayList<BObject> startedServices = (ArrayList<BObject>) listenerObject.getNativeData(STARTED_SERVICES);

            Subscriber subscriber = (Subscriber) serviceObject.getNativeData(PubSubConstants.NATIVE_SUBSCRIBER);
            if (subscriber != null) {
                subscriber.stopAsync().awaitTerminated();
            }

            if (services != null) {
                services.remove(serviceObject);
            }
            if (startedServices != null) {
                startedServices.remove(serviceObject);
            }

            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to unregister service: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the listener gracefully.
     *
     * @param environment Ballerina runtime environment
     * @param listenerObject Listener object from Ballerina
     * @return Error if stop fails, null otherwise
     */
    public static Object gracefulStop(Environment environment, BObject listenerObject) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<BObject> services = (ArrayList<BObject>) listenerObject.getNativeData(CONSUMER_SERVICES);

            if (services != null) {
                for (BObject service : services) {
                    Subscriber subscriber = (Subscriber) service.getNativeData(PubSubConstants.NATIVE_SUBSCRIBER);
                    if (subscriber != null) {
                        subscriber.stopAsync().awaitTerminated();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to stop listener: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the listener immediately.
     *
     * @param environment Ballerina runtime environment
     * @param listenerObject Listener object from Ballerina
     * @return Error if stop fails, null otherwise
     */
    public static Object immediateStop(Environment environment, BObject listenerObject) {
        try {
            @SuppressWarnings("unchecked")
            ArrayList<BObject> services = (ArrayList<BObject>) listenerObject.getNativeData(CONSUMER_SERVICES);

            if (services != null) {
                for (BObject service : services) {
                    Subscriber subscriber = (Subscriber) service.getNativeData(PubSubConstants.NATIVE_SUBSCRIBER);
                    if (subscriber != null) {
                        subscriber.stopAsync();
                    }
                }
            }

            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to stop listener: " + e.getMessage(), e);
        }
    }

    // Helper methods

    private static GoogleCredentials getCredentials(BMap<BString, Object> config) throws IOException {
        if (!config.containsKey(CREDENTIALS_FIELD)) {
            return GoogleCredentials.getApplicationDefault();
        }

        BMap<BString, Object> credentials = (BMap<BString, Object>) config.get(CREDENTIALS_FIELD);

        if (credentials.containsKey(CREDENTIALS_PATH_FIELD)) {
            String credentialsPath = credentials.getStringValue(CREDENTIALS_PATH_FIELD).getValue();
            return ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath));
        } else if (credentials.containsKey(CREDENTIALS_JSON_FIELD)) {
            String credentialsJson = credentials.getStringValue(CREDENTIALS_JSON_FIELD).getValue();
            return ServiceAccountCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
        }

        return GoogleCredentials.getApplicationDefault();
    }
}
