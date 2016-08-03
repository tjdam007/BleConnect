package com.dev4solutions.bleconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

    private static final String SINGLE_PRESS = "1";
    private static final String DOUBLE_PRESS = "2";
    private static final String TRIPLE_PRESS = "3";
    private static final String LONG_PRESS = "-1";
    private boolean isAlertStopped = true;
    private boolean isRegistered;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private boolean isRemover = false;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BleUtils.ACTION_BLE_START_ALERT:
                    startDeviceAlert();
                    break;
                case BleUtils.ACTION_BLE_STOP_ALERT:
                    stopDeviceAlert();
                    break;
                case BleUtils.ACTION_BLE_REMOVE:

                    isRemover = true;
                    onDisconnect();
                    onDestroy();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleUtils.ACTION_BLE_START_ALERT);
        intentFilter.addAction(BleUtils.ACTION_BLE_STOP_ALERT);
        intentFilter.addAction(BleUtils.ACTION_BLE_REMOVE);
        registerReceiver(broadcastReceiver, intentFilter);
        isRegistered = true;
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
        if (isRegistered) {
            unregisterReceiver(broadcastReceiver);
        }

        if (isRemover) {
            BleUtils.deleteDevice(BleConnectService.this);
            sendBroadcast(new Intent(BleUtils.ACTION_BLE_REMOVED_COMPLETED));
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

                List<BluetoothGattService> gattServices = gatt.getServices();

                for (int i = gattServices.size() - 1; i >= 0; i--) {
                    BluetoothGattService service = gattServices.get(i);
                    if (service.getUuid().equals(BleGattService.PressNotification.serviceUUID)) {

                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleGattService.PressNotification.characteristicUUID);
                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(BleGattService.PressNotification.descriptorUUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        gatt.writeDescriptor(descriptor);
                        Log.d(TAG, "Press Notification Enabled");
                    }
                }

                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        serviceDiscoveryComplete();
                        gattHashSet.add(gatt);
                    }
                });
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Runtime.getRuntime().gc();
            byte[] bytes = characteristic.getValue();
            final StringBuilder builder = new StringBuilder("");
            for (byte b : bytes) {
                builder.append(String.format("%d", b));
            }

            if (characteristic.getUuid().equals(BleGattService.PressNotification.characteristicUUID)) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        switch (builder.toString()) {
                            case SINGLE_PRESS:
                                Toast.makeText(BleConnectService.this, "SINGLE PRESS", Toast.LENGTH_SHORT).show();
                                break;
                            case DOUBLE_PRESS:
                                Toast.makeText(BleConnectService.this, "DOUBLE PRESS", Toast.LENGTH_SHORT).show();
                                break;
                            case TRIPLE_PRESS:
                                Toast.makeText(BleConnectService.this, "TRIPLE PRESS", Toast.LENGTH_SHORT).show();
                                break;
                            case LONG_PRESS:
                                Toast.makeText(BleConnectService.this, "LONG PRESS", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (descriptor.getUuid().equals(BleGattService.PressNotification.descriptorUUID)) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("onDescriptorWrite", "PressNotificationEnable");
                            Toast.makeText(BleConnectService.this, "PressNotificationEnable", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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

    synchronized public void startDeviceAlert() {
        isAlertStopped = false;
        final Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (BleUtils.isBleConnected(BleConnectService.this, BleUtils.getBleDevice(BleConnectService.this)) && !isAlertStopped) {
                    if (!gattHashSet.isEmpty()) {
                        BluetoothGatt gatt = gattHashSet.iterator().next();
                        if (gatt != null) {
                            BluetoothGattService service = gatt.getService(BleGattService.ImmediateAlert.serviceUUID);
                            BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleGattService.ImmediateAlert.characteristicUUID);
                            characteristic.setValue(BleGattService.ImmediateAlert.HIGH_ALERT);
                            gatt.writeCharacteristic(characteristic);
                        }
                    }
                    handler.postDelayed(this, 11000);
                }
            }
        });
    }

    synchronized public void stopDeviceAlert() {
        isAlertStopped = true;
        if (BleUtils.isBleConnected(BleConnectService.this, BleUtils.getBleDevice(BleConnectService.this))) {
            if (!gattHashSet.isEmpty()) {
                BluetoothGatt gatt = gattHashSet.iterator().next();
                if (gatt != null) {
                    BluetoothGattService service = gatt.getService(BleGattService.ImmediateAlert.serviceUUID);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(BleGattService.ImmediateAlert.characteristicUUID);
                    characteristic.setValue(BleGattService.ImmediateAlert.NO_ALERT);
                    gatt.writeCharacteristic(characteristic);
                }
            }
        }
    }

}
