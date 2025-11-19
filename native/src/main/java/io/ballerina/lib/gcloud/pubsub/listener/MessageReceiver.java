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

package io.ballerina.lib.gcloud.pubsub.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

// todo: implement this class properly

/**
 * A {MessageReceiver} receives messages from GCP Pub/Sub and dispatches the messages to the Ballerina GCP Pub/SUb
 * service using the message dispatcher.
 */
public class MessageReceiver {
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean closed = new AtomicBoolean(false);
}
