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

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.ArrayList;

/**
 * Native class for the Ballerina GCP Pub/Sub Listener.
 */
public final class Listener {
    private static final String GCP_PROJECT_ID = "gcp.project";
    private static final String LISTENER_CONFIG = "listener.config";

    private static final String NATIVE_CONNECTION = "native.connection";
    private static final String NATIVE_SERVICE_LIST = "native.service.list";
    private static final String NATIVE_SERVICE = "native.service";
    private static final String NATIVE_RECEIVER = "native.receiver";
    private static final String LISTENER_STARTED = "listener.started";

    private Listener() {}

    public static Object init(BObject bListener, BString project, BMap<BString, Object> config) {
        ListenerConfiguration listenerConfig = new ListenerConfiguration(config);
        bListener.addNativeData(GCP_PROJECT_ID, project.getValue());
        bListener.addNativeData(LISTENER_CONFIG, listenerConfig);
        bListener.addNativeData(NATIVE_SERVICE_LIST, new ArrayList<BObject>());
        return null;
    }
}
