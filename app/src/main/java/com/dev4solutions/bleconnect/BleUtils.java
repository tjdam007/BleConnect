package com.dev4solutions.bleconnect;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleUtils {

    public static final int INTERVAL = 10000;
    public static final long DELAY_INTERVAL = 1000;
    public static final int SCANNING_TIME = 15000;

    public static final String ACTION_BLE_CONNECTED = "com.dev4solutions.bleconnect.CONNECTED";
    public static final String ACTION_BLE_DISCONNECTED = "com.dev4solutions.bleconnect.DISCONNECTED";

    private static final String BLE_PREF = "BleConnect_blePrefFile";
    private static final String BLE_DEVICE = "BleConnect_bleAddress";
    public static final String ACTION_STOP_RECONNECT_SERVICE = "com.dev4solutions.bleconnect.STOP_RECONNECT_SERVICE";
    public static final String ACTION_BLE_CONFIGURING = "com.dev4solutions.bleconnect.CONFIGURING";
    private static final int BLUETOOTH_NOTIFICATION_ID = 20160802;
    public static final int FOREGROUND_NOTIFICATION_ID = 20160803;
    private static final int SAFER_DISCONNECTED_NOTIFICATION_ID = 20160804;

    synchronized public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothAdapter mBluetoothAdapter = null;
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        return mBluetoothAdapter;
    }


    synchronized public static boolean isBleConnected(Context context, String bleAddress) {
        try {
            if (bleAddress.equals("")) return false;
            BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null && getBluetoothAdapter(context) != null && getBluetoothAdapter(context).isEnabled()) {
                BluetoothDevice device = getBluetoothAdapter(context).getRemoteDevice(bleAddress);
                if (mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothAdapter.STATE_CONNECTED)
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setBleDevice(Context context, String address) {
        context.getSharedPreferences(BLE_PREF, Context.MODE_PRIVATE).edit()
                .putString(BLE_DEVICE, address)
                .commit();
    }

    public static String getBleDevice(Context context) {
        String s=context.getSharedPreferences(BLE_PREF, Context.MODE_PRIVATE).getString(BLE_DEVICE, "");
        return s;
    }


    public static void showBluetoothNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("BLUETOOTH IS TURNED OFF")
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .build();

        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.notify(BLUETOOTH_NOTIFICATION_ID, notification);

    }

    public static void showBleDisconnectedNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("BLE DISCONNECTED")
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .build();

        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.notify(SAFER_DISCONNECTED_NOTIFICATION_ID, notification);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.cancelAll();
    }

    public static Notification getForegroundNotification(Context context) {
        Intent intent = new Intent(context, BleStatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("BLE CONNECTED")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                .build();
        return notification;
    }

    public static boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.e("refreshDeviceCache", "An exception occured while refreshing device");
        }
        return false;
    }
}
