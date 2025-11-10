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

package io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.api.gax.batching.BatchingSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.gcloud.pubsub.utils.PubSubConstants;
import io.ballerina.stdlib.gcloud.pubsub.utils.PubSubUtils;
import org.threeten.bp.Duration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Native implementation for Google Cloud Pub/Sub Publisher actions.
 */
public class PublisherActions {

    /**
     * Initializes the Google Cloud Pub/Sub publisher.
     *
     * @param publisherObject Publisher object from Ballerina
     * @return Error if initialization fails, null otherwise
     */
    public static Object init(BObject publisherObject) {
        try {
            String topicName = publisherObject.getStringValue(StringUtils.fromString("topicName")).getValue();
            BMap<BString, Object> config = (BMap<BString, Object>) publisherObject.get(
                    StringUtils.fromString("publisherConfig"));

            String projectId = config.getStringValue(StringUtils.fromString("projectId")).getValue();

            // Parse topic name
            TopicName topic;
            if (topicName.startsWith("projects/")) {
                String[] parts = topicName.split("/");
                if (parts.length == 4 && "topics".equals(parts[2])) {
                    topic = TopicName.of(parts[1], parts[3]);
                } else {
                    return PubSubUtils.createError("Invalid topic name format. " +
                            "Expected: projects/{project}/topics/{topic}");
                }
            } else {
                topic = TopicName.of(projectId, topicName);
            }

            Publisher.Builder publisherBuilder = Publisher.newBuilder(topic);

            // Set credentials
            GoogleCredentials credentials = getCredentials(config);
            if (credentials != null) {
                publisherBuilder.setCredentialsProvider(() -> credentials);
            }

            // Configure batching if enabled
            boolean enableBatching = config.getBooleanValue(StringUtils.fromString("enableBatching"));
            if (enableBatching && config.containsKey(StringUtils.fromString("batchSettings"))) {
                BMap<BString, Object> batchSettings = (BMap<BString, Object>) config.get(
                        StringUtils.fromString("batchSettings"));

                long elementCountThreshold = batchSettings.getIntValue(
                        StringUtils.fromString("elementCountThreshold"));
                long requestByteSizeThreshold = batchSettings.getIntValue(
                        StringUtils.fromString("requestByteSizeThreshold"));
                long delayThresholdMillis = batchSettings.getIntValue(
                        StringUtils.fromString("delayThresholdMillis"));

                BatchingSettings batchingSettings = BatchingSettings.newBuilder()
                        .setElementCountThreshold(elementCountThreshold)
                        .setRequestByteThreshold(requestByteSizeThreshold)
                        .setDelayThreshold(Duration.ofMillis(delayThresholdMillis))
                        .build();

                publisherBuilder.setBatchingSettings(batchingSettings);
            }

            // Enable message ordering if configured
            boolean enableMessageOrdering = config.getBooleanValue(
                    StringUtils.fromString("enableMessageOrdering"));
            if (enableMessageOrdering) {
                publisherBuilder.setEnableMessageOrdering(true);
            }

            Publisher publisher = publisherBuilder.build();
            publisherObject.addNativeData(PubSubConstants.NATIVE_PUBLISHER, publisher);

            return null;
        } catch (IOException e) {
            return PubSubUtils.createError("Failed to initialize publisher: " + e.getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during publisher initialization: " +
                    e.getMessage(), e);
        }
    }

    /**
     * Publishes a message to the topic.
     *
     * @param environment Ballerina runtime environment
     * @param publisherObject Publisher object from Ballerina
     * @param message Message to publish
     * @return Error if publishing fails, null otherwise
     */
    public static Object publish(Environment environment, BObject publisherObject, BMap<BString, Object> message) {
        try {
            Publisher publisher = (Publisher) publisherObject.getNativeData(PubSubConstants.NATIVE_PUBLISHER);
            PubsubMessage pubsubMessage = buildPubsubMessage(message);

            ApiFuture<String> future = publisher.publish(pubsubMessage);
            future.get(); // Wait for publish to complete

            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Publishing was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            return PubSubUtils.createError("Failed to publish message: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during publish: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes a message and returns metadata.
     *
     * @param environment Ballerina runtime environment
     * @param publisherObject Publisher object from Ballerina
     * @param message Message to publish
     * @return PublishMetadata record or Error
     */
    public static Object publishWithMetadata(Environment environment, BObject publisherObject,
            BMap<BString, Object> message) {
        try {
            Publisher publisher = (Publisher) publisherObject.getNativeData(PubSubConstants.NATIVE_PUBLISHER);
            PubsubMessage pubsubMessage = buildPubsubMessage(message);

            ApiFuture<String> future = publisher.publish(pubsubMessage);
            String messageId = future.get(); // Wait for publish to complete

            // Create metadata record
            BMap<BString, Object> metadata = ValueCreator.createRecordValue(
                    PubSubUtils.getModule(), "PublishMetadata");
            metadata.put(StringUtils.fromString("messageId"), StringUtils.fromString(messageId));

            return metadata;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Publishing was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            return PubSubUtils.createError("Failed to publish message: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during publish: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes multiple messages in a batch.
     *
     * @param environment Ballerina runtime environment
     * @param publisherObject Publisher object from Ballerina
     * @param messages Array of messages to publish
     * @return Error if publishing fails, null otherwise
     */
    public static Object publishBatch(Environment environment, BObject publisherObject, BArray messages) {
        try {
            Publisher publisher = (Publisher) publisherObject.getNativeData(PubSubConstants.NATIVE_PUBLISHER);
            List<ApiFuture<String>> futures = new ArrayList<>();

            for (int i = 0; i < messages.size(); i++) {
                BMap<BString, Object> message = (BMap<BString, Object>) messages.get(i);
                PubsubMessage pubsubMessage = buildPubsubMessage(message);
                ApiFuture<String> future = publisher.publish(pubsubMessage);
                futures.add(future);
            }

            // Wait for all publishes to complete
            ApiFutures.allAsList(futures).get();

            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Batch publishing was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            return PubSubUtils.createError("Failed to publish batch: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during batch publish: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes multiple messages and returns metadata for each.
     *
     * @param environment Ballerina runtime environment
     * @param publisherObject Publisher object from Ballerina
     * @param messages Array of messages to publish
     * @return Array of PublishMetadata records or Error
     */
    public static Object publishBatchWithMetadata(Environment environment, BObject publisherObject, BArray messages) {
        try {
            Publisher publisher = (Publisher) publisherObject.getNativeData(PubSubConstants.NATIVE_PUBLISHER);
            List<ApiFuture<String>> futures = new ArrayList<>();

            for (int i = 0; i < messages.size(); i++) {
                BMap<BString, Object> message = (BMap<BString, Object>) messages.get(i);
                PubsubMessage pubsubMessage = buildPubsubMessage(message);
                ApiFuture<String> future = publisher.publish(pubsubMessage);
                futures.add(future);
            }

            // Wait for all publishes to complete
            List<String> messageIds = ApiFutures.allAsList(futures).get();

            // Create metadata array
            ArrayType arrayType = TypeCreator.createArrayType(
                    TypeCreator.createRecordType("PublishMetadata", PubSubUtils.getModule(), 0, false, 0));
            BArray metadataArray = ValueCreator.createArrayValue(arrayType);

            for (String messageId : messageIds) {
                BMap<BString, Object> metadata = ValueCreator.createRecordValue(
                        PubSubUtils.getModule(), "PublishMetadata");
                metadata.put(StringUtils.fromString("messageId"), StringUtils.fromString(messageId));
                metadataArray.append(metadata);
            }

            return metadataArray;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Batch publishing was interrupted: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            return PubSubUtils.createError("Failed to publish batch: " + e.getCause().getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Unexpected error during batch publish: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the publisher.
     *
     * @param environment Ballerina runtime environment
     * @param publisherObject Publisher object from Ballerina
     * @return Error if closing fails, null otherwise
     */
    public static Object close(Environment environment, BObject publisherObject) {
        try {
            Publisher publisher = (Publisher) publisherObject.getNativeData(PubSubConstants.NATIVE_PUBLISHER);
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(30, TimeUnit.SECONDS);
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PubSubUtils.createError("Publisher shutdown was interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to close publisher: " + e.getMessage(), e);
        }
    }

    // Helper methods

    private static GoogleCredentials getCredentials(BMap<BString, Object> config) throws IOException {
        if (!config.containsKey(StringUtils.fromString("credentials"))) {
            return GoogleCredentials.getApplicationDefault();
        }

        BMap<BString, Object> credentials = (BMap<BString, Object>) config.get(
                StringUtils.fromString("credentials"));

        if (credentials.containsKey(StringUtils.fromString("credentialsPath"))) {
            String credentialsPath = credentials.getStringValue(
                    StringUtils.fromString("credentialsPath")).getValue();
            return ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath));
        } else if (credentials.containsKey(StringUtils.fromString("credentialsJson"))) {
            String credentialsJson = credentials.getStringValue(
                    StringUtils.fromString("credentialsJson")).getValue();
            return ServiceAccountCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)));
        }

        return GoogleCredentials.getApplicationDefault();
    }

    private static PubsubMessage buildPubsubMessage(BMap<BString, Object> message) {
        PubsubMessage.Builder builder = PubsubMessage.newBuilder();

        // Set data
        BArray data = message.getArrayValue(StringUtils.fromString("data"));
        builder.setData(ByteString.copyFrom(data.getBytes()));

        // Set attributes if present
        if (message.containsKey(StringUtils.fromString("attributes"))) {
            BMap<BString, BString> attributes = (BMap<BString, BString>) message.get(
                    StringUtils.fromString("attributes"));
            for (Map.Entry<BString, BString> entry : attributes.entrySet()) {
                builder.putAttributes(entry.getKey().getValue(), entry.getValue().getValue());
            }
        }

        // Set ordering key if present
        if (message.containsKey(StringUtils.fromString("orderingKey"))) {
            String orderingKey = message.getStringValue(StringUtils.fromString("orderingKey")).getValue();
            if (orderingKey != null && !orderingKey.isEmpty()) {
                builder.setOrderingKey(orderingKey);
            }
        }

        return builder.build();
    }
}
