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

# Provides functionality to acknowledge or reject messages received by the listener service.
public isolated client class Caller {

    # Acknowledges the received message.
    # ```ballerina
    # check caller->ack();
    # ```
    #
    # + return - A `pubsub:Error` if acknowledgement fails or else '()'
    isolated remote function ack() returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.gcloud.pubsub.listener.Caller"
    } external;

    # Rejects the received message and requests re-delivery.
    # ```ballerina
    # check caller->nack();
    # ```
    #
    # + return - A `pubsub:Error` if operation fails or else '()'
    isolated remote function nack() returns Error? =
    @java:Method {
        'class: "io.ballerina.lib.gcloud.pubsub.listener.Caller"
    } external;
}
