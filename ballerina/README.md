# Ballerina Google Cloud Pub/Sub Connector

The Google Cloud Pub/Sub connector allows you to interact with Google Cloud Pub/Sub messaging service from Ballerina.

## Features

- **Publisher**: Publish messages to Google Cloud Pub/Sub topics
- **Listener**: Process messages asynchronously with automatic message handling
- **Message Ordering**: Support for ordered message delivery
- **Batch Publishing**: Efficiently publish multiple messages at once
- **Acknowledgement Management**: Control message acknowledgement and negative acknowledgement

## Usage

### Publisher Example

```ballerina
import ballerinax/gcloud.pubsub;

public function main() returns error? {
    pubsub:Publisher publisher = check new ("projects/my-project/topics/my-topic",
        projectId = "my-project",
        credentials = {
            credentialsPath: "/path/to/credentials.json"
        }
    );

    check publisher->publish({
        data: "Hello, Pub/Sub!".toBytes(),
        attributes: {
            "source": "ballerina-app"
        }
    });

    check publisher->close();
}
```

### Listener Example

```ballerina
import ballerinax/gcloud.pubsub;
import ballerina/io;

listener pubsub:Listener pubsubListener = check new ("my-subscription",
    projectId = "my-project",
    credentials = {
        credentialsPath: "/path/to/credentials.json"
    }
);

service pubsub:Service on pubsubListener {
    remote function onMessage(pubsub:PubSubMessage message, pubsub:Caller caller) returns error? {
        io:println("Received message: ", check string:fromBytes(message.data));

        if message.attributes is map<string> {
            io:println("Attributes: ", message.attributes);
        }

        // Acknowledge the message
        check caller->ack();
    }
}
```

## Configuration

### Authentication

The connector supports multiple authentication methods:
- Service account JSON key file (`credentialsPath`)
- Service account JSON key as string (`credentialsJson`)
- Application Default Credentials (when no credentials are specified)

### Publisher Configuration

- `projectId`: Google Cloud project ID (required)
- `credentials`: Authentication credentials (optional)
- `enableBatching`: Enable message batching (default: true)
- `batchSettings`: Batch configuration (element count, byte size, delay thresholds)
- `enableMessageOrdering`: Enable ordered message delivery (default: false)

### Listener Configuration

- `projectId`: Google Cloud project ID (required)
- `credentials`: Authentication credentials (optional)
