/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.system.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * @see "com.android.builder.testing.ConnectedDeviceProvider"
 */
public class Adb implements AdbInterface {
    private final AndroidDebugBridge bridge;

    public Adb(File sdk) {
        AndroidDebugBridge.initIfNeeded(false /*clientSupport*/);
        File adbPath = FileUtils.getFile(sdk, "platform-tools", "adb");
        bridge = AndroidDebugBridge.createBridge(adbPath.getAbsolutePath(), true /*forceNewBridge*/);
        long timeOut = 30000; // 30 sec
        int sleepTime = 1000;
        while (!bridge.hasInitialDeviceList() && timeOut > 0) {
            sleep(sleepTime);
            timeOut -= sleepTime;
        }

        if (timeOut <= 0 && !bridge.hasInitialDeviceList()) {
            throw new RuntimeException("Timeout getting device list.", null);
        }
    }

    @Override
    public Collection<IDevice> getDevices() {
        return Arrays.asList(bridge.getDevices());
    }

    private void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void restart() {
        bridge.restart();
    }
}
