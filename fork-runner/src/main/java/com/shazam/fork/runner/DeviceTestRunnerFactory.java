/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner;

import com.shazam.fork.device.ScreenRecorderImpl;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Pool;
import com.shazam.fork.model.TestCaseEvent;
import com.shazam.fork.system.adb.Installer;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class DeviceTestRunnerFactory {

    private final Installer installer;
    private final TestRunFactory testRunFactory;

    public DeviceTestRunnerFactory(Installer installer, TestRunFactory testRunFactory) {
        this.installer = installer;
        this.testRunFactory = testRunFactory;
    }

    public Runnable createDeviceTestRunner(Pool pool,
                                           Queue<TestCaseEvent> testClassQueue,
                                           CountDownLatch deviceInPoolCountDownLatch,
                                           Device device,
                                           ProgressReporter progressReporter
    ) {
        return new DeviceTestRunner(
                installer,
                pool,
                device,
                testClassQueue,
                deviceInPoolCountDownLatch,
                progressReporter,
                new ScreenRecorderImpl(device),
                testRunFactory);
    }
}
