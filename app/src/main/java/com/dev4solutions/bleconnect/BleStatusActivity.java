package com.dev4solutions.bleconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleStatusActivity extends AppCompatActivity {
    private TextView textViewDevice;
    private TextView textViewStatus;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble_status);
        textViewDevice = (TextView) findViewById(R.id.textViewDevice);
        textViewDevice.setText(BleUtils.getBleDevice(this));
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        if (!BleUtils.isBleConnected(this, BleUtils.getBleDevice(this)) && BleUtils.getBluetoothAdapter(this).isEnabled()) {
            BleReconnectService.start(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleUtils.ACTION_BLE_CONFIGURING);
        intentFilter.addAction(BleUtils.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BleUtils.ACTION_BLE_DISCONNECTED);

        registerReceiver(broadcastReceiver, intentFilter);
        if (BleUtils.isBleConnected(this, BleUtils.getBleDevice(this))) {
            textViewStatus.setText("Connected");
        } else {
            textViewStatus.setText("Disconnected");

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BleUtils.ACTION_BLE_CONNECTED:
                    textViewStatus.setText("Connected");
                    break;
                case BleUtils.ACTION_BLE_CONFIGURING:
                    textViewStatus.setText("Configuring");
                    break;
                case BleUtils.ACTION_BLE_DISCONNECTED:
                    textViewStatus.setText("Disconnected");
                    break;
            }
        }
    };

}
