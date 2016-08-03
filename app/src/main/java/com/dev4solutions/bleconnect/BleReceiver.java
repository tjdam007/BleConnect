package com.dev4solutions.bleconnect;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleReceiver extends BroadcastReceiver {
    private static final String TAG = "BleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, intent.getAction());
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    BleReconnectService.start(context);
                    BleUtils.cancelNotification(context);
                } else if (state == BluetoothAdapter.STATE_OFF && !TextUtils.isEmpty(BleUtils.getBleDevice(context))) {
                    BleReconnectService.stop(context);
                    BleScanService.stop(context);
                    BleConnectService.stop(context);
                    BleUtils.showBluetoothNotification(context);

                }
                break;

            case "android.intent.action.BOOT_COMPLETED":
            case "android.intent.action.QUICKBOOT_POWERON":
            case "com.htc.intent.action.BOOT_COMPLETED":
            case "com.htc.intent.action.QUICKBOOT_POWERON":
                if (!TextUtils.isEmpty(BleUtils.getBleDevice(context))) {
                    if (!BleUtils.getBluetoothAdapter(context).enable()) {
                        BleUtils.showBluetoothNotification(context);
                    } else {
                        BleReconnectService.start(context);
                    }
                }
                break;

            case BleUtils.ACTION_BLE_CONNECTED:
                BleUtils.cancelNotification(context);
                break;
            case BleUtils.ACTION_BLE_DISCONNECTED:
                BleUtils.showBleDisconnectedNotification(context);
                BleConnectService.stop(context);
                if (BleUtils.getBluetoothAdapter(context).isEnabled()) {
                    BleReconnectService.start(context);
                }

                break;
        }
    }
}
