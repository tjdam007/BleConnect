package com.dev4solutions.bleconnect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleReconnectService extends Service {


    private static final String TAG = "BleReconnectService";
    private ScheduledExecutorService threadPoolExecutor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleUtils.ACTION_STOP_RECONNECT_SERVICE);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (threadPoolExecutor == null) {
            threadPoolExecutor = Executors.newSingleThreadScheduledExecutor();
            threadPoolExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "ScheduledExecutorService Continuous Scanning");
                    Runtime.getRuntime().gc();
                    if (!BleUtils.isBleConnected(BleReconnectService.this, BleUtils.getBleDevice(BleReconnectService.this))) {
                        BleScanService.start(BleReconnectService.this);
                    } else {
                        if (threadPoolExecutor != null) {
                            threadPoolExecutor.shutdown();
                            threadPoolExecutor = null;
                        }
                        stopSelf();
                    }
                }
            }, BleUtils.DELAY_INTERVAL, BleUtils.SCANNING_TIME + BleUtils.INTERVAL, TimeUnit.MILLISECONDS);
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
            threadPoolExecutor = null;
        }
        unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onDestroy");
    }

    public static void start(Context context) {
        if (!TextUtils.isEmpty(BleUtils.getBleDevice(context))) {
            context.startService(new Intent(context, BleReconnectService.class));
        }
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    };

    public static void stop(Context context) {
        context.stopService(new Intent(context, BleReconnectService.class));
    }
}
