package com.dev4solutions.bleconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleConnectService extends Service {
    private static final String TAG = "BleConnectService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattCallbackImpl bluetoothGattCallback;
    private ScheduledExecutorService poolExecutor = null;
    private HashSet<BluetoothGatt> gattHashSet;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gattHashSet = new HashSet<>();
        bluetoothAdapter = BleUtils.getBluetoothAdapter(this);
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(BleUtils.getBleDevice(this));
        if (bluetoothDevice != null) {
            bluetoothGattCallback = new BluetoothGattCallbackImpl();
            bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        if (poolExecutor != null) {
            poolExecutor.shutdown();
            poolExecutor = null;
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, BleConnectService.class));
    }

    public static void Start(Context context) {
        if (!TextUtils.isEmpty(BleUtils.getBleDevice(context))) {
            context.startService(new Intent(context, BleConnectService.class));
        }
    }


    private final class BluetoothGattCallbackImpl extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        gatt.discoverServices();
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                sendBroadcast(new Intent(BleUtils.ACTION_BLE_CONFIGURING));
                            }
                        });
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                sendBroadcast(new Intent(BleUtils.ACTION_BLE_DISCONNECTED));
                            }
                        });
                        break;
                }
            } else {
                //TODO : DO SOMETHING HERE
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        serviceDiscoveryComplete();
                        gattHashSet.add(gatt);
                    }
                });
            }
        }
    }


    private void serviceDiscoveryComplete() {
        startForeground(BleUtils.FOREGROUND_NOTIFICATION_ID, BleUtils.getForegroundNotification(BleConnectService.this));
        sendBroadcast(new Intent(BleUtils.ACTION_BLE_CONNECTED));
        poolExecutor = Executors.newSingleThreadScheduledExecutor();
        poolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                /*CHECKING CONNECTION*/
                Log.d(TAG, "Scheduler Checking Connection");
                if (!BleUtils.isBleConnected(BleConnectService.this, BleUtils.getBleDevice(BleConnectService.this))) {
                    poolExecutor.shutdown();
                    onDisconnect();
                    if (BleUtils.getBluetoothAdapter(BleConnectService.this).isEnabled())
                        distanceDisconnected();
                }
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
    }

    private void distanceDisconnected() {
        stopForeground(true);
        stopSelf();
    }

    private void onDisconnect() {
        try {
            if (BleUtils.getBluetoothAdapter(BleConnectService.this).isEnabled() && !gattHashSet.isEmpty()) {
                Iterator iterator = gattHashSet.iterator();
                while (iterator.hasNext()) {
                    BluetoothGatt bluetoothGatt = (BluetoothGatt) iterator.next();
                    if (bluetoothGatt != null) {
                        bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                        BleUtils.refreshDeviceCache(bluetoothGatt);

                    }
                }
                gattHashSet.clear();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
