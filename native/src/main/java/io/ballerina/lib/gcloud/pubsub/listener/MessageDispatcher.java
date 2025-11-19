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

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import io.ballerina.lib.gcloud.pubsub.ModuleUtils;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.RemoteMethodType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import java.io.PrintStream;
import java.util.Optional;

/**
 * A {MessageDispatcher} dispatches GCP Pub/Sub messages into the Ballerina GCP Pub/Sub service.
 */
public class MessageDispatcher {
    private static final PrintStream ERR_OUT = System.err;
    private static final String ON_ERROR_METHOD = "onError";
    private static final String ON_MESSAGE_METHOD = "onMessage";
    private static final String BCALLER_NAME = "Caller";
    private static final String NATIVE_CONSUMER = "native.consumer";

    private final Runtime ballerinaRuntime;
    private final Service nativeService;
    private final OnErrorCallback onErrorCallback = new OnErrorCallback();

    MessageDispatcher(Runtime ballerinaRuntime, Service nativeService) {
        this.ballerinaRuntime = ballerinaRuntime;
        this.nativeService = nativeService;
    }

    public void onMessage(PubsubMessage message, AckReplyConsumer consumer, OnMsgCallback onMsgCallback) {
        Thread.startVirtualThread(() -> {
            try {
                boolean isConcurrentSafe = nativeService.isOnMessageMethodIsolated();
                StrandMetadata metadata = new StrandMetadata(isConcurrentSafe, null);
                Object[] params = getOnMessageParams(message, consumer);
                Object result = ballerinaRuntime.callMethod(
                        nativeService.getConsumerService(), ON_MESSAGE_METHOD, metadata, params);
                if (!nativeService.isCallerPresentInOnMsgMethod()) {
                    consumer.ack();
                }
                onMsgCallback.notifySuccess(result);
            } catch (BError e) {
                consumer.nack();
                onMsgCallback.notifyFailure(e);
            } catch (Exception e) {
                consumer.nack();
                BError bError = PubSubUtils.createError(
                        "Unexpected error occurred while receiving messages from the broker: " +
                                e.getMessage(), e);
                onErrorCallback.notifyFailure(bError);
            }
        });
    }

    private Object[] getOnMessageParams(PubsubMessage message, AckReplyConsumer consumer) {
        Parameter[] parameters = this.nativeService.getOnMessageMethod().getParameters();
        Object[] args = new Object[parameters.length];
        int idx = 0;
        for (Parameter param : parameters) {
            Type referredType = TypeUtils.getReferredType(param.type);
            switch (referredType.getTag()) {
                case TypeTags.OBJECT_TYPE_TAG:
                    args[idx++] = getCaller(consumer);
                    break;
                case TypeTags.RECORD_TYPE_TAG:
                    args[idx++] = MessageConverter.toBallerinaMessage(message);
                    break;
            }
        }
        return args;
    }

    private BObject getCaller(AckReplyConsumer consumer) {
        BObject caller = ValueCreator.createObjectValue(ModuleUtils.getModule(), BCALLER_NAME);
        caller.addNativeData(NATIVE_CONSUMER, consumer);
        return caller;
    }

    public void onError(Throwable t) {
        Thread.startVirtualThread(() -> {
            try {
                ERR_OUT.println("Unexpected error occurred while message processing: " + t.getMessage());
                Optional<RemoteMethodType> onError = nativeService.getOnError();
                if (onError.isEmpty()) {
                    t.printStackTrace();
                    return;
                }
                BError error = PubSubUtils.createError("Failed to fetch the message", t);
                boolean isConcurrentSafe = nativeService.isOnErrorMethodIsolated();
                StrandMetadata metadata = new StrandMetadata(isConcurrentSafe, null);
                Object result = ballerinaRuntime.callMethod(
                        nativeService.getConsumerService(), ON_ERROR_METHOD, metadata, error);
                onErrorCallback.notifySuccess(result);
            } catch (BError err) {
                onErrorCallback.notifyFailure(err);
            }
        });
    }
}
