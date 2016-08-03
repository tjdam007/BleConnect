package com.dev4solutions.bleconnect;

import java.util.UUID;

/**
 * Created by MaNoJ SiNgH RaWaL on 8/26/2015.
 */
public class BleGattService {
    public static class Battery{
        public static UUID serviceUUID= UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
        public static UUID characteristicUUID= UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
        public static UUID descriptorUUID= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static class TxPower{
        public static UUID serviceUUID= UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
        public static UUID characteristicUUID= UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
        public static UUID descriptorUUID= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static class PressNotification {
        public static UUID serviceUUID= UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
        public static UUID characteristicUUID= UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        public static UUID descriptorUUID= UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    public static class ImmediateAlert{
        public static UUID serviceUUID= UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
        public static UUID characteristicUUID= UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
        public static byte[] NO_ALERT={0x00};
        public static byte[] MID_ALERT={0x01};
        public static byte[] HIGH_ALERT={0x02};
    }

    public static class AntiLostAlert {
        public static UUID serviceUUID= UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
        public static UUID characteristicUUID= UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
        public static byte[] NO_ALERT={0x00};
        public static byte[] MID_ALERT={0x01};
        public static byte[] HIGH_ALERT={0x02};
    }
}
