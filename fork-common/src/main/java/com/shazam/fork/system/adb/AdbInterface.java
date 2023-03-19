package com.shazam.fork.system.adb;

import com.android.ddmlib.IDevice;

import java.util.Collection;

public interface AdbInterface {
    enum Type {
        Fork,
        Droidherd
    }

    Collection<IDevice> getDevices();
    void restart();
}
