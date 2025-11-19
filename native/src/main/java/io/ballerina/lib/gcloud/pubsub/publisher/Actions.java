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

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.ballerina.lib.gcloud.pubsub.config.GcpCredentialConfig;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Actions class for GCP Pub/Sub {@link Publisher} with utility methods to invoke as inter-op functions.
 */
public final class Actions {

    private static final String NATIVE_PUBLISHER = "native.publisher";

    private Actions() {
    }

    /**
     * Initializes the Google Cloud Pub/Sub publisher.
     *
     * @param publisher The Ballerina GCP Pub/Sub publisher object
     * @param project   GCP project Id
     * @param topic     GCP Topic name
     * @param config    Ballerina GCP Pub/Sub publisher configurations
     * @return Error if initialization fails, null otherwise
     */
    public static Object init(BObject publisher, BString project, BString topic, BMap<BString, Object> config) {
        try {
            PublisherConfiguration publisherConfig = new PublisherConfiguration(config);
            ProjectTopicName topicName = ProjectTopicName.of(project.getValue(), topic.getValue());
            Publisher.Builder builder = Publisher.newBuilder(topicName);

            BatchConfig batchConfig = publisherConfig.batchConfig();
            if (Objects.nonNull(batchConfig)) {
                BatchingSettings batchingSettings = BatchingSettings.newBuilder()
                        .setIsEnabled(true)
                        .setDelayThresholdDuration(Duration.ofMillis(batchConfig.maxDelay()))
                        .setElementCountThreshold(batchConfig.maxMessageCount())
                        .setRequestByteThreshold(batchConfig.maxBytes())
                        .build();
                builder.setBatchingSettings(batchingSettings);
            }

            Compression compressionConfig = publisherConfig.compression();
            if (Objects.nonNull(compressionConfig)) {
                builder.setEnableCompression(true);
                builder.setCompressionBytesThreshold(compressionConfig.threshold());
            }

            CredentialsProvider credentialsProvider = getCredentialsProvider(publisherConfig.auth());
            builder.setCredentialsProvider(credentialsProvider);
            builder.setEnableMessageOrdering(publisherConfig.enableMessageOrdering());

            RetryConfig retryConfig = publisherConfig.retryConfig();
            if (Objects.nonNull(retryConfig)) {
                RetrySettings retrySettings = RetrySettings.newBuilder()
                        .setMaxAttempts(retryConfig.maxAttempts())
                        .setInitialRetryDelayDuration(Duration.ofMillis(retryConfig.initialRetryDelay()))
                        .setInitialRpcTimeoutDuration(Duration.ofMillis(retryConfig.initialRpcTimeout()))
                        .setMaxRetryDelayDuration(Duration.ofMillis(retryConfig.maxRetryDelay()))
                        .setMaxRpcTimeoutDuration(Duration.ofMillis(retryConfig.maxRpcTimeout()))
                        .setRetryDelayMultiplier(retryConfig.retryDelayMultiplier())
                        .setRpcTimeoutMultiplier(retryConfig.rpcTimeoutMultiplier())
                        .setTotalTimeoutDuration(Duration.ofMillis(retryConfig.totalTimeout()))
                        .build();
                builder.setRetrySettings(retrySettings);
            }

            Publisher nativePublisher = builder.build();
            publisher.addNativeData(NATIVE_PUBLISHER, nativePublisher);
            return null;
        } catch (IOException e) {
            return PubSubUtils.createError("Failed to initialize publisher: " + e.getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during publisher initialization: " +
                    e.getMessage(), e);
        }
    }

    private static CredentialsProvider getCredentialsProvider(GcpCredentialConfig authConfig) {
        if (Objects.isNull(authConfig)) {
            return GoogleCredentials::getApplicationDefault;
        }
        return () -> ServiceAccountCredentials.fromStream(new FileInputStream(authConfig.path()));
    }

    /**
     * Publishes a message to the Google Cloud Pub/Sub topic.
     *
     * @param publisher The Ballerina GCP Pub/Sub publisher object containing the native publisher instance
     * @param message   The message to publish, containing data, attributes, and optional ordering key
     * @return A {@link BString} containing the message ID if publishing succeeds, or a
     *         {@link io.ballerina.runtime.api.values.BError} if the operation fails
     */
    public static Object publish(BObject publisher, BMap<BString, Object> message) {
        Publisher nativePublisher = (Publisher) publisher.getNativeData(NATIVE_PUBLISHER);
        CompletableFuture<Object> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                PubsubMessage pubsubMessage = MessageConverter.toPubSubMessage(message);
                String messageId = nativePublisher.publish(pubsubMessage).get();
                future.complete(StringUtils.fromString(messageId));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.complete(PubSubUtils.createError("Publishing was interrupted: " + e.getMessage(), e));
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
                future.complete(PubSubUtils.createError("Failed to publish message: " + errorMessage, e));
            } catch (RuntimeException e) {
                future.complete(PubSubUtils.createError(
                        "Unexpected error during publish: " + e.getMessage(), e));
            }
        });

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError(
                    "Waiting for publish operation was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            return PubSubUtils.createError(
                    "Error occurred while waiting for publish operation to complete: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the Google Cloud Pub/Sub publisher and releases all associated resources.
     *
     * @param publisher The Ballerina GCP Pub/Sub publisher object
     * @param timeout The maximum time to wait for the publisher to terminate, in seconds.
     *                This value is converted to milliseconds internally
     * @return {@code null} if the publisher is closed successfully within the timeout period,
     *         or a {@link io.ballerina.runtime.api.values.BError} if the shutdown is interrupted
     *         or any other exception occurs during the close operation
     */
    public static Object close(BObject publisher, BDecimal timeout) {
        try {
            Publisher nativePublisher = (Publisher) publisher.getNativeData(NATIVE_PUBLISHER);
            nativePublisher.shutdown();
            nativePublisher.awaitTermination(
                    PubSubUtils.decimalToMillis(timeout.decimalValue()), TimeUnit.MILLISECONDS);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Publisher shutdown was interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to close publisher: " + e.getMessage(), e);
        }
    }
}
