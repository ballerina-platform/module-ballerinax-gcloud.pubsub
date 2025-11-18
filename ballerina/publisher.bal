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
public client isolated class Publisher {

    # Initializes a new GCP Pub/Sub Publisher instance.
    # ```ballerina
    # pubsub:Publisher publisher = check new ("<gcp-project-id>", "<topic-name>");
    # ```
    #
    # + project - The Google Cloud project ID
    # + topic - The name of the Pub/Sub topic to publish to
    # + config - GCP Pub/Sub Publisher configuration
    # + return - A `pubsub:Error` if initialization fails or else `()`
    public isolated function init(string project, string topic, *PublisherConfiguration config) returns Error? {
        return self.externInit(project, topic, config);
    }

    private isolated function externInit(string project, string topic, *PublisherConfiguration config) returns Error? =
    @java:Method {
        name: "init",
        'class: "io.ballerina.lib.gcloud.pubsub.publisher.Actions"
    } external;

    # Publishes a message to a GCp topic.
    # ```ballerina
    # string|pubsub:Error? result = publisher->publish({data: "Hello World".toBytes()});
    # ```
    #
    # + message - Message to be published
    # + return - The message ID if the operation was successful or a `pubsub:Error` if the publish operation fails
    isolated remote function publish(Message message) returns string|Error =
    @java:Method {
        'class: "io.ballerina.lib.gcloud.pubsub.publisher.Actions"
    } external;

    # Closes the publisher and releases all resources.
    # ```ballerina
    # pubsub:Error? result = publisher->close();
    # ```
    #
    # + timeout - The maximum time to wait for the publisher to terminate (in seconds)
    # + return - A `pubsub:Error` if closing the publisher fails or else '()'
    isolated remote function close(decimal timeout = 10.0) returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.gcloud.pubsub.publisher.Actions"
    } external;
}
