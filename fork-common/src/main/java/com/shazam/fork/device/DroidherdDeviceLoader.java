/*
 * Copyright 2019 Apple Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.device;

import com.android.ddmlib.IDevice;
import com.shazam.fork.model.Device;
import com.shazam.fork.model.Devices;
import com.shazam.fork.model.DisplayGeometry;
import com.shazam.fork.system.adb.AdbInterface;
import ru.tinkoff.testops.droidherd.DroidherdClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.shazam.fork.model.Device.Builder.aDevice;
import static com.shazam.fork.model.Devices.Builder.devices;

public class DroidherdDeviceLoader implements DeviceLoaderInterface {
    private final AdbInterface adb;
    private final DeviceGeometryRetriever deviceGeometryRetriever;
    private final DroidherdClient droidherdClient;

    public DroidherdDeviceLoader(AdbInterface adb, DeviceGeometryRetriever deviceGeometryRetriever, DroidherdClient droidherdClient) {
        this.adb = adb;
        this.deviceGeometryRetriever = deviceGeometryRetriever;
        this.droidherdClient = droidherdClient;
    }

    public Devices loadDevices() {
        Devices.Builder devicesBuilder = devices();
        List<IDevice> iDevices = getDevicesWithRetry();
        for (IDevice iDevice : iDevices) {
            devicesBuilder.putDevice(iDevice.getSerialNumber(), loadDeviceCharacteristics(iDevice));
        }

        return devicesBuilder.build();
    }

    /*
     * Sometimes if emulators farm connect devices it doesn't pick-ed up by ADB
     * Need to get some time to refresh devices list
     * */
    private List<IDevice> getDevicesWithRetry() {
        List<IDevice> iDevices = loadAllDevices();
        if (droidherdClient != null) {
            for (int i = 0; i <= 5; ++i) {
                if (iDevices.size() >= droidherdClient.connectedEmulatorsCount()) {
                    break;
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                adb.restart();
                iDevices = loadAllDevices();
            }
        }

        return iDevices;
    }

    private List<IDevice> loadAllDevices() {
        return new ArrayList<>(adb.getDevices());
    }

    private DisplayGeometry detectGeometryWithRetry(IDevice device) {
        DisplayGeometry geometry = deviceGeometryRetriever.detectGeometry(device);
        // additionally retry if emulators farm present - in rare cases emulator not ready
        // and need a little bit more time to be able
        for (int i = 0; i < 8; ++i) {
            if (geometry != null) {
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            geometry = deviceGeometryRetriever.detectGeometry(device);
        }
        return geometry;
    }

    private Device loadDeviceCharacteristics(IDevice device) {
        DisplayGeometry geometry = detectGeometryWithRetry(device);
        return aDevice()
                .withSerial(device.getSerialNumber())
                .withManufacturer(device.getProperty("ro.product.manufacturer"))
                .withModel(device.getProperty("ro.product.model"))
                .withApiLevel(device.getProperty("ro.build.version.sdk"))
                .withDeviceInterface(device)
                .withTabletCharacteristic(device.getProperty("ro.build.characteristics"))
                .withDisplayGeometry(geometry).build();
    }
}
