/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.lib.gcloud.pubsub.listener;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import io.ballerina.lib.gcloud.pubsub.utils.PubSubUtils;
import io.ballerina.runtime.api.values.BObject;

/**
 * Native class for the Ballerina GCP Pub/Sub Caller.
 */
public class Caller {
    static final String NATIVE_CONSUMER = "native.consumer";

    /**
     * Acknowledges the message.
     *
     * @param callerObject Caller object from Ballerina
     * @return Error if acknowledgement fails, null otherwise
     */
    public static Object ack(BObject callerObject) {
        try {
            AckReplyConsumer consumer = (AckReplyConsumer) callerObject.getNativeData(NATIVE_CONSUMER);
            if (consumer != null) {
                consumer.ack();
            }
            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to acknowledge message: " + e.getMessage(), e);
        }
    }

    /**
     * Negatively acknowledges the message.
     *
     * @param callerObject Caller object from Ballerina
     * @return Error if nack fails, null otherwise
     */
    public static Object nack(BObject callerObject) {
        try {
            AckReplyConsumer consumer = (AckReplyConsumer) callerObject.getNativeData(NATIVE_CONSUMER);
            if (consumer != null) {
                consumer.nack();
            }
            return null;
        } catch (Exception e) {
            return PubSubUtils.createError("Failed to nack message: " + e.getMessage(), e);
        }
    }
}
