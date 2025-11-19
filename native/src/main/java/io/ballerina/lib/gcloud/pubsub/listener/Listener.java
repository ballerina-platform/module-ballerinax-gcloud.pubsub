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

import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.ballerina.lib.gcloud.pubsub.config.GcpCredentialConfig;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.FileInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Native class for the Ballerina GCP Pub/Sub Listener.
 */
public final class Listener {
    private static final String GCP_PROJECT_ID = "gcp.project";
    private static final String LISTENER_CONFIG = "listener.config";

    private static final String NATIVE_SERVICE_LIST = "native.service.list";
    private static final String NATIVE_SERVICE = "native.service";
    private static final String NATIVE_SUBSCRIBER = "native.subscriber";
    private static final String NATIVE_PROCESSOR = "native.processor";
    private static final String LISTENER_STARTED = "listener.started";

    private Listener() {
    }

    public static Object init(BObject bListener, BString project, BMap<BString, Object> config) {
        ListenerConfiguration listenerConfig = new ListenerConfiguration(config);
        bListener.addNativeData(GCP_PROJECT_ID, project.getValue());
        bListener.addNativeData(LISTENER_CONFIG, listenerConfig);
        bListener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        return null;
    }

    public static Object attach(Environment env, BObject bListener, BObject bService, Object name) {
        Object started = bListener.getNativeData(LISTENER_STARTED);
        try {
            Service.validateService(bService);
            Service nativeService = new Service(bService);
            String project = (String) bListener.getNativeData(GCP_PROJECT_ID);
            ListenerConfiguration listenerConfig = (ListenerConfiguration) bListener.getNativeData(LISTENER_CONFIG);
            MessageDispatcher messageDispatcher = new MessageDispatcher(env.getRuntime(), nativeService);
            MessageProcessor messageProcessor = new MessageProcessor(messageDispatcher);
            Subscriber subscriber = createPubSubSubscriber(
                    project, nativeService.getServiceConfig(), listenerConfig, messageProcessor.gcpPubSubReceiver());
            bService.addNativeData(NATIVE_SERVICE, nativeService);
            bListener.addNativeData(NATIVE_SUBSCRIBER, subscriber);
            bService.addNativeData(NATIVE_PROCESSOR, messageProcessor);
            List<BObject> serviceList = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
            serviceList.add(bService);
            if (Objects.nonNull(started)) {
                AtomicBoolean listenerStarted = (AtomicBoolean) started;
                if (listenerStarted.get()) {
                    subscriber.startAsync().awaitRunning();
                    messageProcessor.start();
                }
            }
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return PubSubUtils.createError(String.format("Failed to attach service to listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object detach(BObject bService) {
        Object subscriber = bService.getNativeData(NATIVE_SUBSCRIBER);
        Object processor = bService.getNativeData(NATIVE_PROCESSOR);
        try {
            if (Objects.isNull(subscriber)) {
                return PubSubUtils.createError("Could not find the native native GCP Pub/Sub subscriber");
            }
            if (Objects.isNull(processor)) {
                return PubSubUtils.createError("Could not find the native native message receiver");
            }
            ((Subscriber) subscriber).stopAsync().awaitTerminated();
            ((MessageProcessor) processor).stop();
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return PubSubUtils.createError(
                    String.format("Failed to detach a service from the listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object start(BObject bListener) {
        List<BObject> bServices = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
        try {
            for (BObject bService : bServices) {
                Subscriber subscriber = (Subscriber) bService.getNativeData(NATIVE_SUBSCRIBER);
                MessageProcessor processor = (MessageProcessor) bService.getNativeData(NATIVE_PROCESSOR);
                subscriber.startAsync().awaitRunning();
                processor.start();
            }
            AtomicBoolean listenerStarted = new AtomicBoolean(true);
            bListener.addNativeData(LISTENER_STARTED, listenerStarted);
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return PubSubUtils.createError(
                    String.format("Error occurred while starting the Ballerina GCP Pub/Sub listener: %s", errorMsg), e);
        }
        return null;
    }

    public static Object gracefulStop(BObject bListener) {
        List<BObject> bServices = (List<BObject>) bListener.getNativeData(NATIVE_SERVICE_LIST);
        try {
            for (BObject bService : bServices) {
                Subscriber subscriber = (Subscriber) bService.getNativeData(NATIVE_SUBSCRIBER);
                MessageProcessor processor = (MessageProcessor) bService.getNativeData(NATIVE_PROCESSOR);
                subscriber.stopAsync().awaitTerminated();
                processor.stop();
            }
        } catch (Exception e) {
            String errorMsg = Objects.isNull(e.getMessage()) ? "Unknown error" : e.getMessage();
            return PubSubUtils.createError(
                    String.format("Error occurred while gracefully stopping the Ballerina GCP Pub/Sub listener: %s",
                            errorMsg), e);
        }
        return null;
    }

    public static Object immediateStop(BObject bListener) {
        return gracefulStop(bListener);
    }

    private static Subscriber createPubSubSubscriber(String project, ServiceConfig svcConfig,
                                                     ListenerConfiguration listenerConfig,
                                                     com.google.cloud.pubsub.v1.MessageReceiver receiver) {
        ProjectSubscriptionName projectSubscription = ProjectSubscriptionName.of(project, svcConfig.subscription());
        Subscriber.Builder builder = Subscriber.newBuilder(projectSubscription, receiver);

        CredentialsProvider credentialsProvider = getCredentialsProvider(listenerConfig.auth());
        builder.setCredentialsProvider(credentialsProvider);

        builder.setMaxAckExtensionPeriodDuration(Duration.ofMillis(svcConfig.maxAckExtensionPeriod()));
        builder.setMaxDurationPerAckExtensionDuration(Duration.ofMillis(svcConfig.maxDurationPerAckExtension()));
        builder.setMinDurationPerAckExtensionDuration(Duration.ofMillis(svcConfig.minDurationPerAckExtension()));
        builder.setParallelPullCount(1);

        FlowControlConfig flowControlConfig = svcConfig.flowControlSettings();
        if (Objects.nonNull(flowControlConfig)) {
            FlowControlSettings flowControlSettings = FlowControlSettings.newBuilder()
                    .setMaxOutstandingElementCount(flowControlConfig.maxOutstandingMessageCount())
                    .setMaxOutstandingRequestBytes(flowControlConfig.maxOutstandingRequestBytes())
                    .build();
            builder.setFlowControlSettings(flowControlSettings);
        }

        return builder.build();
    }

    private static CredentialsProvider getCredentialsProvider(GcpCredentialConfig authConfig) {
        if (Objects.isNull(authConfig)) {
            return GoogleCredentials::getApplicationDefault;
        }
        return () -> ServiceAccountCredentials.fromStream(new FileInputStream(authConfig.path()));
    }
}
