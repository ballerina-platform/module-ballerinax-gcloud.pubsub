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

# Represents credentials for authenticating with Google Cloud Pub/Sub.
#
# + credentialsPath - Path to the service account JSON key file
# + credentialsJson - Service account JSON key as a string
public type Credentials record {|
    string credentialsPath?;
    string credentialsJson?;
|};

# Represents batch settings for the publisher.
#
# + elementCountThreshold - Maximum number of messages in a batch (default: 100)
# + requestByteSizeThreshold - Maximum size of a batch in bytes (default: 1000000)
# + delayThresholdMillis - Maximum delay before sending a batch in milliseconds (default: 10)
public type BatchSettings record {|
    int elementCountThreshold = 100;
    int requestByteSizeThreshold = 1000000;
    int delayThresholdMillis = 10;
|};

# Represents configuration for the Google Cloud Pub/Sub publisher.
#
# + projectId - Google Cloud project ID
# + credentials - Authentication credentials
# + enableBatching - When enabled, the publisher will batch messages before sending
# + batchSettings - Batch configuration settings
# + enableMessageOrdering - When enabled, the publisher will publish messages with ordering keys
public type PublisherConfiguration record {|
    string projectId;
    Credentials credentials?;
    boolean enableBatching = true;
    BatchSettings batchSettings?;
    boolean enableMessageOrdering = false;
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
# + messageId - Unique message identifier (Only for received messages)
# + data - Message data as bytes
# + attributes - Message attributes (key-value pairs)
# + publishTime - Time when the message was published (Only for received messages)
# + orderingKey - Ordering key for ordered delivery
public type PubSubMessage record {|
    string messageId?;
    byte[] data;
    map<string> attributes?;
    string publishTime?;
    string orderingKey?;
|};

# Represents metadata for a published message.
#
# + messageId - Unique message identifier assigned by the server
# + publishTime - Timestamp when the message was published
public type PublishMetadata record {|
    string messageId;
    string publishTime?;
|};
