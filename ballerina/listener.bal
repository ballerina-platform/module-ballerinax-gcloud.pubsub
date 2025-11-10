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
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

# Ballerina Google Cloud Pub/Sub Listener.
# Provides a listener to consume messages asynchronously from a Google Cloud Pub/Sub subscription.
public isolated class Listener {

    # Initializes a Listener object with the given subscription and configuration.
    # ```ballerina
    # pubsub:Listener pubsubListener = check new("my-subscription", projectId = "my-project");
    # ```
    #
    # + subscriptionName - The subscription name or full path
    # + listenerConfig - The listener configuration
    public isolated function init(string subscriptionName, *ListenerConfiguration listenerConfig) returns Error? {
        return self.listenerInit(subscriptionName, listenerConfig);
    }

    private isolated function listenerInit(string subscriptionName, ListenerConfiguration config) returns Error? =
    @java:Method {
        name: "init",
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;

    # Attaches the service to the `pubsub:Listener` endpoint.
    # ```ballerina
    # check pubsubListener.attach(service);
    # ```
    #
    # + s - The service to attach
    # + name - Optional service name
    # + return - `()` or else a `pubsub:Error` upon failure to register the service
    public isolated function attach(Service s, string[]|string? name = ()) returns error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;

    # Starts consuming messages on the attached service.
    # ```ballerina
    # check pubsubListener.'start();
    # ```
    #
    # + return - `()` or else a `pubsub:Error` upon failure to start
    public isolated function 'start() returns error? =
    @java:Method {
        name: "start",
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;

    # Detaches a service from the `pubsub:Listener` endpoint.
    # ```ballerina
    # check pubsubListener.detach(service);
    # ```
    #
    # + s - The service to detach
    # + return - `()` or else a `pubsub:Error` upon failure to detach the service
    public isolated function detach(Service s) returns error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;

    # Stops consuming messages and gracefully shuts down the subscriber.
    # ```ballerina
    # check pubsubListener.gracefulStop();
    # ```
    #
    # + return - `()` or else a `pubsub:Error` upon failure
    public isolated function gracefulStop() returns error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;

    # Immediately stops consuming messages and terminates the subscriber.
    # ```ballerina
    # check pubsubListener.immediateStop();
    # ```
    #
    # + return - `()` or else a `pubsub:Error` upon failure
    public isolated function immediateStop() returns error? =
    @java:Method {
        'class: "io.ballerina.stdlib.gcloud.pubsub.nativeimpl.subscriber.ListenerActions"
    } external;
}

# Configurations for the Pub/Sub service.
#
# + subscriptionName - The subscription name
public type PubSubServiceConfig record {|
    string subscriptionName;
|};

# The annotation to configure the Pub/Sub service.
public annotation PubSubServiceConfig ServiceConfig on service, class;

# The Pub/Sub service type.
public type Service distinct service object {
    // remote function onMessage(ReceivedMessage message, Caller caller) returns error?;
};
