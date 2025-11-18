## Overview

[Google Cloud Pub/Sub](https://cloud.google.com/pubsub) is a fully managed, real-time messaging service that enables 
asynchronous communication between independent applications. It provides a scalable, durable message ingestion and 
delivery system that decouples services, allowing them to communicate reliably at scale. 
Pub/Sub supports multiple messaging patterns including publish/subscribe, push and pull delivery, and dead-letter queues, 
making it ideal for event-driven architectures, streaming analytics, and data integration pipelines.

The ballerinax/googleapis.pubsub package provides APIs to interact with Google Cloud Pub/Sub. 
It allows developers to programmatically publish messages to topics, create and manage subscriptions, configure message delivery policies, 
and implement robust, cloud-native event-driven solutions that leverage Google Cloud's globally distributed infrastructure 
and automatic scaling capabilities within Ballerina applications.

## Setup guide

To use the Ballerina Google Cloud Pub/Sub connector, you need to have a Google Cloud account and a project with the Pub/Sub API enabled.

### Create a Google Cloud project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Click on the project dropdown and select **New Project**.
3. Enter a project name and click **Create**.
4. Note your project ID, as you'll need it for configuration.

### Enable the Pub/Sub API

1. In the Google Cloud Console, navigate to **APIs & Services** > **Library**.
2. Search for "Cloud Pub/Sub API".
3. Click on it and then click **Enable**.

### Create a service account and download credentials

1. Navigate to **IAM & Admin** > **Service Accounts** in the Google Cloud Console.
2. Click **Create Service Account**.
3. Enter a service account name and description, then click **Create and Continue**.
4. Grant the service account the **Pub/Sub Publisher** and **Pub/Sub Subscriber** roles.
5. Click **Done**.
6. Click on the created service account, go to the **Keys** tab.
7. Click **Add Key** > **Create new key**.
8. Select **JSON** and click **Create**.
9. Save the downloaded JSON file securely - you'll need the path to this file for authentication.

### Create a topic

1. In the Google Cloud Console, navigate to **Pub/Sub** > **Topics**.
2. Click **Create Topic**.
3. Enter the topic ID - `my-topic`.
4. Click **Create**.

### Create a subscription

1. In the Google Cloud Console, navigate to **Pub/Sub** > **Subscriptions**.
2. Click **Create Subscription**.
3. Enter the subscription ID - `my-subscription`.
4. Select the topic you created earlier (`my-topic`) from the dropdown.
5. Choose the delivery type (Pull is recommended for the Ballerina listener).
6. Click **Create**.

## Quickstart

To use the Google Cloud Pub/Sub connector in your Ballerina application, modify the .bal file as follows:

### Step 1: Import the connector

Import the `ballerinax/gcloud.pubsub` module into your Ballerina project.

```ballerina
import ballerinax/gcloud.pubsub;
```

### Step 2: Instantiate a new publisher

Create a `pubsub:Publisher` instance with your Google Cloud Pub/Sub configuration.

```ballerina
configurable string project = ?; // GCP Project ID
configurable string topic = ?; // Pub/Sub Topic Name
configurable string gcpCredentialsFilePath = ?; // Path to Service Account JSON file

pubsub:Publisher publisher = check new (
    project,
    topic,
    auth = {
        path: gcpCredentialsFilePath
    }
);
```

### Step 3: Publish messages

Now, utilize the available publisher operations to publish messages.

#### Publish a simple message

```ballerina
string messageId = check publisher->publish({
    data: "Hello, Google Pub/Sub!".toBytes()
});
```

#### Publish a message with attributes

```ballerina
string messageId = check publisher->publish({
    data: "Hello, Google Pub/Sub!".toBytes(),
    attributes: {
        "source": "ballerina-app",
        "version": "1.0"
    }
});
```

#### Publish a message with ordering key

```ballerina
check publisher->publish({
    data: "Message 1".toBytes(),
    orderingKey: "customer-123"
});
```

### Step 4: Clean up resources

When done, close the publisher to release resources.

```ballerina
check publisher->close();
```

### Step 5: Set up a listener

Create a `pubsub:Listener` instance to consume messages from a subscription.

```ballerina
configurable string subscriptionName = ?;

listener pubsub:Listener pubsubListener = check new (
    subscriptionName,
    projectId = projectId,
    credentials = {
        credentialsPath: credentialsPath
    }
);
```

### Step 6: Implement a service to process messages

Attach a service to the listener to process incoming messages.

```ballerina
import ballerina/io;

service on pubsubListener {
    remote function onMessage(pubsub:PubSubMessage message, pubsub:Caller caller) returns error? {
        // Process the message
        io:println("Received message: ", check string:fromBytes(message.data));

        // Print attributes if present
        if message.attributes is map<string> {
            io:println("Attributes: ", message.attributes);
        }

        // Acknowledge the message
        check caller->ack();
    }
}
```

#### Handle errors and negative acknowledgements

```ballerina
service on pubsubListener {
    remote function onMessage(pubsub:PubSubMessage message, pubsub:Caller caller) returns error? {
        // Process the message
        error? result = processMessage(message);

        if result is error {
            // If processing fails, nack the message so it can be redelivered
            io:println("Error processing message: ", result.message());
            check caller->nack();
        } else {
            // If processing succeeds, acknowledge the message
            check caller->ack();
        }
    }
}

function processMessage(pubsub:PubSubMessage message) returns error? {
    // Your message processing logic here
    io:println("Processing message: ", check string:fromBytes(<byte[]>message.data));
}
```

### Step 7: Run the Ballerina application

```bash
bal run
```
