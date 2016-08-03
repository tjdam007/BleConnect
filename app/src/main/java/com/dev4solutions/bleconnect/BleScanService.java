package com.dev4solutions.bleconnect;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleScanService extends Service {

    private static final String TAG = "BleScanService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ScanCallback scanCallback;
    private ScanSettings scanSetting;
    private ArrayList<ScanFilter> filterList;
    private boolean isScanning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanning();
                stopSelf();
            }
        }, BleUtils.SCANNING_TIME);

        bluetoothAdapter = BleUtils.getBluetoothAdapter(BleScanService.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                synchronized public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.d("BleScanService", result.getDevice().getAddress());
                    if ( isScanning && !TextUtils.isEmpty(result.getDevice().getName()) && result.getDevice().getAddress().equalsIgnoreCase(BleUtils.getBleDevice(BleScanService.this)) && result.getDevice().getName().equals("SAFER")) {
                        stopScanning();
                        BleConnectService.Start(BleScanService.this);
                    }
                }
            };
            scanSetting = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filterList = new ArrayList<>();
        } else {
            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                synchronized public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                    Log.d("BleScanService", bluetoothDevice.getAddress());
                    if (isScanning && !TextUtils.isEmpty(bluetoothDevice.getName()) && bluetoothDevice.getAddress().equalsIgnoreCase(BleUtils.getBleDevice(BleScanService.this)) && bluetoothDevice.getName().equals("SAFER")) {
                        stopScanning();
                        BleConnectService.Start(BleScanService.this);

                    }
                }
            };
        }

        startScanning();
        return START_NOT_STICKY;
    }

    public static void start(Context context) {
        if (!TextUtils.isEmpty(BleUtils.getBleDevice(context))) {
            context.startService(new Intent(context, BleScanService.class));
        } else {
            context.sendBroadcast(new Intent(BleUtils.ACTION_STOP_RECONNECT_SERVICE));
        }
    }


    private void startScanning() {
        if (bluetoothAdapter.isEnabled() && !isScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.getBluetoothLeScanner().startScan(filterList, scanSetting, scanCallback);
            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
            isScanning = true;
        }
    }

    private void stopScanning() {
        if (bluetoothAdapter.isEnabled() && isScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            } else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
            isScanning = false;
        }
    }


    public static void stop(Context context) {
        context.stopService(new Intent(context, BleScanService.class));
    }
}
