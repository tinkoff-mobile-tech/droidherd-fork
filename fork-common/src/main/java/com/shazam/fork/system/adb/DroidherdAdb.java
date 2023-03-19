package com.shazam.fork.system.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.ClientTracker;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.internal.ClientImpl;
import com.android.ddmlib.internal.DeviceImpl;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
* Implementation of loadDevices method without Adb of ddmlib.
* Use native process to get and parse devices list.
* But ADB initialization require anyway to proper work.
* */

public class DroidherdAdb implements AdbInterface {

    private final File adbPath;
    // useless tracker for us because fork anyway can't figure out any disconnects
    private static final ClientTracker dummyClientTracker = new ClientTracker() {
        @Override
        public void trackDisconnectedClient(ClientImpl client) {
        }

        @Override
        public void trackClientToDropAndReopen(ClientImpl client) {
        }

        @Override
        public void trackDeviceToDropAndReopen(DeviceImpl device) {
        }
    };

    public DroidherdAdb(File sdk) {
        adbPath = FileUtils.getFile(sdk, "platform-tools", "adb");

        AndroidDebugBridge.initIfNeeded(false /*clientSupport*/);
        AndroidDebugBridge.createBridge(adbPath.getAbsolutePath(), true /*forceNewBridge*/);
    }

    @Override
    public Collection<IDevice> getDevices() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(adbPath.getAbsolutePath(), "devices");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                Map<String, IDevice.DeviceState> stateByDevices = parseDeviceListResponse(
                        reader.lines().collect(Collectors.toList()));
                return stateByDevices.entrySet().stream()
                        .map(entry -> new DeviceImpl(dummyClientTracker, entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void restart() {
    }

    private Map<String, IDevice.DeviceState> parseDeviceListResponse(List<String> devices) {
        Map<String, IDevice.DeviceState> deviceStateMap = Maps.newHashMap();
        for (String d : devices) {
            String[] param = d.split("\t"); //$NON-NLS-1$
            if (param.length == 2) {
                // new adb uses only serial numbers to identify devices
                deviceStateMap.put(param[0], IDevice.DeviceState.getState(param[1]));
            }
        }
        return deviceStateMap;
    }
}
