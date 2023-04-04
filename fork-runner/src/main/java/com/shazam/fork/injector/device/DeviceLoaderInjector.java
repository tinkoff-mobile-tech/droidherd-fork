/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.injector.device;

import com.shazam.fork.device.DeviceLoader;
import com.shazam.fork.device.DeviceLoaderInterface;
import com.shazam.fork.device.DroidherdDeviceLoader;
import com.shazam.fork.injector.system.DroidherdClientInjector;

import static com.shazam.fork.injector.ConfigurationInjector.configuration;
import static com.shazam.fork.injector.device.DeviceGeometryRetrieverInjector.deviceGeometryReader;
import static com.shazam.fork.injector.system.AdbInjector.adb;

public class DeviceLoaderInjector {

    private DeviceLoaderInjector() {}

    public static DeviceLoaderInterface deviceLoader() {
        if (configuration().getDroidherdConfig().isConfigured()) {
            return new DroidherdDeviceLoader(adb(), deviceGeometryReader(), DroidherdClientInjector.clientInstance());
        }
        return new DeviceLoader(adb(), deviceGeometryReader(), configuration().getExcludedSerials());
    }
}
