package org.zywx.wbpalmstar.plugin.uexdevice;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;

public class DeviceUtils {
    public static boolean isGPSEnable(Context context) {
        try {
            LocationManager locationManager = (LocationManager)context.
                    getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBluetoothEnable() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }
}
