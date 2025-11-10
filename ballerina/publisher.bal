// Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Represents a Google Cloud Pub/Sub publisher endpoint.
#
# + publisherConfig - Stores configurations related to the publisher
public client isolated class Publisher {

    final PublisherConfiguration & readonly publisherConfig;
    private final string topicName;

    # Creates a new `gcloud.pubsub:Publisher`.
    #
    # + topicName - The name of the topic to publish to (format: projects/{project-id}/topics/{topic-name})
    # + config - Configurations related to initializing the publisher
    # + return - A `gcloud.pubsub:Error` if initialization fails or else '()'
    public isolated function init(string topicName, *PublisherConfiguration config) returns Error? {
        self.topicName = topicName;
        self.publisherConfig = config.cloneReadOnly();
        check self.publisherInit();
    }

    private isolated function publisherInit() returns Error? =
    @java:Method {
        name: "init",
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;

    # Publishes a message to the topic.
    # ```ballerina
    # gcloud.pubsub:Error? result = publisher->publish({data: "Hello World".toBytes()});
    # ```
    #
    # + message - Message to be published
    # + return - A `gcloud.pubsub:Error` if the publish operation fails or else '()'
    isolated remote function publish(PubsubMessage message) returns Error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;

    # Publishes a message to the topic and returns metadata.
    # ```ballerina
    # gcloud.pubsub:PublishMetadata metadata = check publisher->publishWithMetadata({data: "Hello World".toBytes()});
    # ```
    #
    # + message - Message to be published
    # + return - A `gcloud.pubsub:PublishMetadata` containing the message ID and publish time or else a `gcloud.pubsub:Error`
    isolated remote function publishWithMetadata(PubsubMessage message) returns PublishMetadata|Error =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;

    # Publishes multiple messages to the topic.
    # ```ballerina
    # gcloud.pubsub:Error? result = publisher->publishBatch([{data: "Message 1".toBytes()}, {data: "Message 2".toBytes()}]);
    # ```
    #
    # + messages - Array of messages to be published
    # + return - A `gcloud.pubsub:Error` if the publish operation fails or else '()'
    isolated remote function publishBatch(PubsubMessage[] messages) returns Error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;

    # Publishes multiple messages to the topic and returns metadata for each.
    # ```ballerina
    # gcloud.pubsub:PublishMetadata[] metadata = check publisher->publishBatchWithMetadata([{data: "Message 1".toBytes()}, {data: "Message 2".toBytes()}]);
    # ```
    #
    # + messages - Array of messages to be published
    # + return - An array of `gcloud.pubsub:PublishMetadata` containing message IDs and publish times or else a `gcloud.pubsub:Error`
    isolated remote function publishBatchWithMetadata(PubsubMessage[] messages) returns PublishMetadata[]|Error =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;

    # Closes the publisher and releases all resources.
    # ```ballerina
    # gcloud.pubsub:Error? result = publisher->close();
    # ```
    #
    # + return - A `gcloud.pubsub:Error` if closing the publisher fails or else '()'
    isolated remote function close() returns Error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.publisher.PublisherActions"
    } external;
}
