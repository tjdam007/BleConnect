package com.dev4solutions.bleconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleStatusActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textViewDevice;
    private TextView textViewStatus;
    private ToggleButton toggleButton;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ble_status);
        textViewDevice = (TextView) findViewById(R.id.textViewDevice);
        textViewDevice.setText(BleUtils.getBleDevice(this));
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        toggleButton = (ToggleButton) findViewById(R.id.sendAlert);
        toggleButton.setOnClickListener(this);
        button = (Button) findViewById(R.id.buttonRemove);
        if (!BleUtils.isBleConnected(this, BleUtils.getBleDevice(this)) && BleUtils.getBluetoothAdapter(this).isEnabled()) {
            BleReconnectService.start(this);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent(BleUtils.ACTION_BLE_REMOVE));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleUtils.ACTION_BLE_CONFIGURING);
        intentFilter.addAction(BleUtils.ACTION_BLE_CONNECTED);
        intentFilter.addAction(BleUtils.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(BleUtils.ACTION_BLE_REMOVED_COMPLETED);

        registerReceiver(broadcastReceiver, intentFilter);
        if (BleUtils.isBleConnected(this, BleUtils.getBleDevice(this))) {
            textViewStatus.setText("Connected");
            toggleButton.setEnabled(true);
        } else {
            textViewStatus.setText("Disconnected");
            toggleButton.setEnabled(false);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        sendBroadcast(new Intent(BleUtils.ACTION_BLE_STOP_ALERT));
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BleUtils.ACTION_BLE_CONNECTED:
                    textViewStatus.setText("Connected");
                    toggleButton.setEnabled(true);
                    break;
                case BleUtils.ACTION_BLE_CONFIGURING:
                    textViewStatus.setText("Configuring");
                    break;
                case BleUtils.ACTION_BLE_DISCONNECTED:
                    textViewStatus.setText("Disconnected");
                    toggleButton.setEnabled(false);
                    break;
                case BleUtils.ACTION_BLE_REMOVED_COMPLETED:
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (toggleButton.isChecked()) {
            sendBroadcast(new Intent(BleUtils.ACTION_BLE_START_ALERT));
        } else {
            sendBroadcast(new Intent(BleUtils.ACTION_BLE_STOP_ALERT));
        }
    }
}
