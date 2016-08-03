package com.dev4solutions.bleconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class BleSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private BleAdapter bleAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private ScanCallback scanCallback;
    private ScanSettings scanSetting;
    private ArrayList<ScanFilter> filterList;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(BleUtils.getBleDevice(this))) {
            startActivity(new Intent(this, BleStatusActivity.class));
            finish();
        }
        setContentView(R.layout.activity_search);
        listView = (ListView) findViewById(R.id.listView);
        bleAdapter = new BleAdapter(this);
        listView.setAdapter(bleAdapter);
        listView.setOnItemClickListener(this);
        bluetoothAdapter = BleUtils.getBluetoothAdapter(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                synchronized public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    bleAdapter.add(result.getDevice());
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
                    bleAdapter.add(bluetoothDevice);
                }
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startScanning();
    }

    private void startScanning() {
        if (bluetoothAdapter.isEnabled() && !isScanning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothAdapter.getBluetoothLeScanner().startScan(filterList, scanSetting, scanCallback);
            } else {
                bluetoothAdapter.startLeScan(leScanCallback);
            }
            isScanning = true;
        } else {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter = null;
        bleAdapter = null;
        leScanCallback = null;
        scanCallback = null;
        filterList = null;
    }


    MenuItem refreshItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        refreshItem = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "REFRESH");
        refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == refreshItem) {
            bleAdapter.clear();
            stopScanning();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startScanning();
                }
            }, 1000);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        stopScanning();
        BleUtils.setBleDevice(this, bleAdapter.getItem(position).getAddress());
        startActivity(new Intent(this, BleStatusActivity.class));
        finish();
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
}
