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

# Represents credentials for authenticating with Google Cloud Pub/Sub.
#
# + credentialsPath - Path to the service account JSON key file
# + credentialsJson - Service account JSON key as a string
public type Credentials record {|
    string credentialsPath?;
    string credentialsJson?;
|};

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

# Represents configuration for the Google Cloud Pub/Sub listener.
#
# + projectId - Google Cloud project ID
# + credentials - Authentication credentials
public type ListenerConfiguration record {|
    string projectId;
    Credentials credentials?;
|};

# Represents a message to be published to Google Cloud Pub/Sub.
#
# + messageId - Unique message identifier (Set by the broker upon publishing. Do not set manually)
# + data - Message data as bytes
# + attributes - Message attributes (key-value pairs)
# + publishTime - Time when the message was published (Set by the broker upon publishing. Do not set manually)
# + orderingKey - Ordering key for ordered delivery. Messages with the same ordering key are delivered in order.
#                 `enableMessageOrdering` must be set to true in publisher configuration to use this feature
public type Message record {|
    string messageId?;
    byte[] data;
    map<string> attributes?;
    time:Utc publishTime?;
    string orderingKey?;
|};
