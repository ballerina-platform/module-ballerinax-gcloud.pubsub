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

import ballerina/time;

# Configuration for the Google Cloud Pub/Sub Publisher.
public type PublisherConfiguration record {|
    # Configuration for publishing message batch
    BatchConfig batchConfig?;
    # Message compression configurations
    record {|
        # Compression threshold in bytes. Messages larger than this size will be compressed
        int threshold = 240;
    |} compression?;
    # GCP pub/sub authentication configurations
    GcpCredentialConfig auth?;
    # Enable message ordering to ensure messages are received in the order they were published
    boolean enableMessageOrdering = false;
    # Retry configuration for handling transient failures
    RetryConfig retryConfig?;
|};

# Message batching settings for the Publisher.
public type BatchConfig record {|
    # The maximum delay before sending a batch (in seconds). Default in GCP is 1 millisecond
    decimal maxDelay = 0.001;
    # The maximum number of messages to include in a single batch
    int maxMessageCount = 100;
    # The maximum total size in bytes for a single batch. Default in GCP: 1000 bytes (or 1KB)
    int maxBytes = 1000;
|};

# GCP Pub/Sub client retry configuration for handling transient failures.
public type RetryConfig record {|
    # Maximum number of retry attempts for a failed publish operation.
    # If set to 0, the logic uses `totalTimeout` to determine retries instead.
    int maxAttempts;
    # Initial delay before the first retry attempt (in seconds).
    # Subsequent retry delays are calculated using this value multiplied by `retryDelayMultiplier`.
    decimal initialRetryDelay = 0.1;
    # Initial timeout for the first RPC call (in seconds).
    # Subsequent RPC timeouts are calculated using this value multiplied by `rpcTimeoutMultiplier`.
    decimal initialRpcTimeout = 5.0;
    # Maximum delay between retry attempts (in seconds).
    # Limits how much the retry delay can increase through exponential backoff.
    decimal maxRetryDelay = 60.0;
    # Maximum timeout for any single RPC call (in seconds).
    # Limits how much the RPC timeout can increase.
    decimal maxRpcTimeout = 60.0;
    # Multiplier applied to the retry delay after each failed attempt for exponential backoff
    float retryDelayMultiplier = 4.0;
    # Multiplier applied to the RPC timeout after each failed attempt
    float rpcTimeoutMultiplier = 4.0;
    # Total timeout for all retry attempts combined (in seconds).
    # If set to 0, the logic uses `maxAttempts` to determine retries instead.
    decimal totalTimeout = 600.0;
|};

# Configuration for authenticating with Google Cloud Pub/Sub using a service account key file.
public type GcpCredentialConfig record {|
    # Path to the service account JSON key file
    string path;
|};

# Represents a message to be published to Google Cloud Pub/Sub.
#
# + messageId - Unique message identifier (Set by the broker upon publishing. Do not set manually)
# + data - Message data as bytes
# + attributes - Message attributes (key-value pairs)
# + publishTime - Time when the message was published (Set by the broker upon publishing. Do not set manually)
# + orderingKey - Ordering key for ordered delivery. Messages with the same ordering key are delivered in order.
# `enableMessageOrdering` must be set to true in publisher configuration to use this feature
public type Message record {|
    string messageId?;
    byte[] data;
    map<string> attributes?;
    time:Utc publishTime?;
    string orderingKey?;
|};

# Configuration for the Google Cloud Pub/Sub Listener.
public type ListenerConfiguration record {|
    # GCP authentication configuration using service account credentials
    GcpCredentialConfig auth?;
|};

# Service-specific configuration for a Pub/Sub subscription.
public type ServiceConfiguration record {|
    # The name of the Pub/Sub subscription to pull messages from
    string subscription;
    # Maximum period a message ack deadline will be extended (in seconds).
    # Recommended to set this to a reasonable upper bound of the subscriber time to process any message.
    # A zero duration effectively disables auto deadline extensions.
    decimal maxAckExtensionPeriod = 3600;
    # Upper bound for a single ack deadline extension period (in seconds).
    # The ack deadline will continue to be extended by up to this duration until maxAckExtensionPeriod is reached.
    # This bounds the maximum amount of time before a message re-delivery if the Subscriber fails to extend the deadline.
    decimal maxDurationPerAckExtension = 600;
    # Minimum duration for each acknowledgment deadline extension (in seconds).
    # Specifies the minimum amount of time that must pass before the redelivery of a message occurs.
    # Set this to a high value for exactly-once delivery scenarios to ensure ack IDs remain valid.
    decimal minDurationPerAckExtension = 0.0;
    # Number of StreamingPull streams to pull messages from the subscription.
    # Increasing this value allows parallel message processing and higher throughput.
    int parallelPullCount = 1;
    # Flow control settings for managing outstanding messages
    FlowControlConfig flowControlSettings?;
|};

# Configuration to control the maximum number and size of outstanding messages received from the subscriber
# but not yet acknowledged or negatively acknowledged). When limits are exceeded, the subscriber
# stops pulling more messages until outstanding messages are processed.
public type FlowControlConfig record {|
    # Maximum number of outstanding messages.
    # The subscriber will not pull more messages if this limit is reached.
    int maxOutstandingMessageCount = 1000;
    # Maximum total size of outstanding messages in bytes.
    # The subscriber will not pull more messages if this limit is reached.
    # Default is 100MB
    int maxOutstandingRequestBytes = 104857600;
|};

# The annotation to configure the Pub/Sub service.
public annotation ServiceConfiguration ServiceConfig on service;

# The Pub/Sub service type.
public type Service distinct service object {
    // remote function onMessage(Message message, Caller caller) returns error?;
};
