package com.dev4solutions.bleconnect;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by MaNoJ SiNgH RaWaL on 02-Aug-16.
 */
public class BleAdapter extends BaseAdapter {
    private final Context mContext;
    private HashSet<BluetoothDevice> deviceHashSet;
    private ArrayList<BluetoothDevice> deviceArrayList;

    public BleAdapter(Context context) {
        mContext = context;
        deviceHashSet = new HashSet<>();
        deviceArrayList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return deviceArrayList.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return deviceArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_ble, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewDevice = (TextView) view.findViewById(R.id.textViewDevice);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice bluetoothDevice = deviceArrayList.get(position);

        viewHolder.textViewDevice.setText(bluetoothDevice.getName() + " : " + bluetoothDevice.getAddress());

        return view;
    }

    public void add(BluetoothDevice bluetoothDevice) {
        deviceHashSet.add(bluetoothDevice);
        deviceArrayList.clear();
        deviceArrayList.addAll(deviceHashSet);
        notifyDataSetChanged();
    }

    public void clear() {
        deviceHashSet.clear();
        deviceArrayList.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView textViewDevice;
    }
}
