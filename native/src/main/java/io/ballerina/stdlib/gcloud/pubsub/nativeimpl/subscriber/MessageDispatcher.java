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

package io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.gcloud.pubsub.utils.PubSubUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Message dispatcher for Google Cloud Pub/Sub listener.
 * Receives messages from Pub/Sub and dispatches them to the Ballerina service.
 * Uses a blocking queue to ensure messages are processed in arrival order.
 */
public class MessageDispatcher {

    private static final String ON_MESSAGE = "onMessage";
    private final BObject service;
    private final Runtime runtime;
    private final BlockingQueue<MessageTask> messageQueue;
    private final ExecutorService executorService;

    /**
     * Represents a message processing task.
     */
    private static class MessageTask {
        final PubsubMessage message;
        final AckReplyConsumer consumer;

        MessageTask(PubsubMessage message, AckReplyConsumer consumer) {
            this.message = message;
            this.consumer = consumer;
        }
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "BObject is from Ballerina runtime API and " +
            "needs to be stored to invoke service methods. This is an intentional design pattern.")
    public MessageDispatcher(BObject service, Runtime runtime) {
        this.service = service;
        this.runtime = runtime;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();
        startMessageProcessor();
    }

    /**
     * Starts the message processor thread that processes messages from the queue.
     */
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED",
            justification = "Future return value not needed for background processor thread")
    private void startMessageProcessor() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    MessageTask task = messageQueue.take();
                    processMessage(task.message, task.consumer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Gets the MessageReceiver that can be used with Google Cloud Pub/Sub Subscriber.
     *
     * @return MessageReceiver instance
     */
    public MessageReceiver getMessageReceiver() {
        return (message, consumer) -> handleMessage(message, consumer);
    }

    /**
     * Handles incoming messages from Pub/Sub by adding them to the queue.
     *
     * @param message The received Pub/Sub message
     * @param consumer The ack reply consumer
     */
    private void handleMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            messageQueue.put(new MessageTask(message, consumer));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            consumer.nack();
        }
    }

    /**
     * Processes a message from the queue.
     *
     * @param message The received Pub/Sub message
     * @param consumer The ack reply consumer
     */
    private void processMessage(PubsubMessage message, AckReplyConsumer consumer) {
        // Convert Pub/Sub message to Ballerina message
        BMap<BString, Object> ballerinaMessage = convertToBallerinaMessage(message);

        // Create Caller object
        BObject caller = ValueCreator.createObjectValue(PubSubUtils.getModule(), "Caller");
        caller.addNativeData("ACK_REPLY_CONSUMER", consumer);

        // Prepare arguments for service method (message, caller)
        Object[] args = new Object[]{ballerinaMessage, caller};

        // Execute the onMessage remote function
        executeResource(ON_MESSAGE, args, consumer);
    }

    /**
     * Executes a remote function on the service.
     *
     * @param function The function name
     * @param args The function arguments
     * @param consumer The ack reply consumer for error handling
     */
    private void executeResource(String function, Object[] args, AckReplyConsumer consumer) {
        ObjectType serviceType = (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(service));
        Map<String, Object> properties = new HashMap<>();
        boolean isConcurrentSafe = serviceType.isIsolated() && serviceType.isIsolated(function);
        StrandMetadata strandMetadata = new StrandMetadata(isConcurrentSafe, properties);

        try {
            runtime.callMethod(service, function, strandMetadata, args);
        } catch (Throwable e) {
            // On error, nack the message
            consumer.nack();
            if (e instanceof BError) {
                ((BError) e).printStackTrace();
            } else {
                PubSubUtils.createError("Message processing failed: " + e.getMessage(), e).printStackTrace();
            }
        }
    }

    /**
     * Shuts down the message processor.
     */
    public void shutdown() {
        executorService.shutdownNow();
    }

    /**
     * Converts a Pub/Sub message to a Ballerina PubSubMessage record.
     *
     * @param message The Pub/Sub message
     * @return Ballerina PubSubMessage record
     */
    private BMap<BString, Object> convertToBallerinaMessage(PubsubMessage message) {
        BMap<BString, Object> ballerinaMessage = ValueCreator.createRecordValue(
                PubSubUtils.getModule(), "PubSubMessage");

        // Set message ID
        ballerinaMessage.put(StringUtils.fromString("messageId"),
                StringUtils.fromString(message.getMessageId()));

        // Set data
        byte[] data = message.getData().toByteArray();
        ballerinaMessage.put(StringUtils.fromString("data"),
                ValueCreator.createArrayValue(data));

        // Set attributes
        if (!message.getAttributesMap().isEmpty()) {
            BMap<BString, Object> attributes = ValueCreator.createMapValue();
            for (Map.Entry<String, String> entry : message.getAttributesMap().entrySet()) {
                attributes.put(StringUtils.fromString(entry.getKey()),
                        StringUtils.fromString(entry.getValue()));
            }
            ballerinaMessage.put(StringUtils.fromString("attributes"), attributes);
        }

        // Set publish time
        com.google.protobuf.Timestamp publishTime = message.getPublishTime();
        Instant instant = Instant.ofEpochSecond(publishTime.getSeconds(), publishTime.getNanos());
        ballerinaMessage.put(StringUtils.fromString("publishTime"),
                StringUtils.fromString(instant.toString()));

        // Set ordering key if present
        String orderingKey = message.getOrderingKey();
        if (orderingKey != null && !orderingKey.isEmpty()) {
            ballerinaMessage.put(StringUtils.fromString("orderingKey"),
                    StringUtils.fromString(orderingKey));
        }

        return ballerinaMessage;
    }
}
