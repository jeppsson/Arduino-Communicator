/*
 * Copyright (C) 2012 Mathias Jeppsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.primavera.arduino.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ArduinoCommunicatorActivity extends ListActivity {

    private static final int ARDUINO_USB_VENDOR_ID = 0x2341;
    private static final int ARDUINO_USB_PRODUCT_ID = 0x43;

    private final static String TAG = "ArduinoCommunicatorActivity";
    private final static boolean DEBUG = false;
    
    private Boolean mIsReceiving;
    private ArrayList<ByteString> mTransferedDataList = new ArrayList<ByteString>();
    private ArrayAdapter<ByteString> mDataAdapter;

    private class ByteString {

        private byte[] byteArray = new byte[1];
        private int usedLength;
        private boolean showInAscii;

        void add(byte[] newArray) {
            // Make sure we have enough space to store byte array.
            while (usedLength + newArray.length > byteArray.length) {
                byte[] tmpArray = new byte[byteArray.length * 2];
                System.arraycopy(byteArray, 0, tmpArray, 0, usedLength);
                byteArray = tmpArray;
            }

            // Add byte array.
            System.arraycopy(newArray, 0, byteArray, usedLength, newArray.length);
            usedLength += newArray.length;
        }

        void toggleCoding() {
            showInAscii = !showInAscii;
        }

        @Override
        public String toString() {
            StringBuilder hexStr = new StringBuilder();

            if (showInAscii) {
                for (int i = 0; i < usedLength; i++) {
                    if (Character.isLetterOrDigit(byteArray[i])) {
                        hexStr.append(new String(new byte[] {byteArray[i]}));
                    } else {
                        hexStr.append('.');
                    }
                }
            } else {
                for (int i = 0; i < usedLength; i++) {
                    hexStr.append(String.format("%1$02X", byteArray[i]));
                    hexStr.append(" ");
                }
            }

            return hexStr.toString();
        }
    }

    private void findDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
        if (DEBUG) Log.d(TAG, "length: " + usbDeviceList.size());
        Iterator<UsbDevice> deviceIterator = usbDeviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            UsbDevice tempUsbDevice = deviceIterator.next();

            // Print device information. If you think your device should be able
            // to communicate with this app, add it to accepted products below.
            if (DEBUG) Log.d(TAG, "VendorId: " + tempUsbDevice.getVendorId());
            if (DEBUG) Log.d(TAG, "ProductId: " + tempUsbDevice.getProductId());
            if (DEBUG) Log.d(TAG, "DeviceName: " + tempUsbDevice.getDeviceName());
            if (DEBUG) Log.d(TAG, "DeviceId: " + tempUsbDevice.getDeviceId());
            if (DEBUG) Log.d(TAG, "DeviceClass: " + tempUsbDevice.getDeviceClass());
            if (DEBUG) Log.d(TAG, "DeviceSubclass: " + tempUsbDevice.getDeviceSubclass());
            if (DEBUG) Log.d(TAG, "InterfaceCount: " + tempUsbDevice.getInterfaceCount());
            if (DEBUG) Log.d(TAG, "DeviceProtocol: " + tempUsbDevice.getDeviceProtocol());

            if (tempUsbDevice.getVendorId() == ARDUINO_USB_VENDOR_ID && tempUsbDevice.getProductId() == ARDUINO_USB_PRODUCT_ID) {
                usbDevice = tempUsbDevice;
            }
        }

        if (usbDevice == null) {
            if (DEBUG) Log.i(TAG, "No device found!");
            Toast.makeText(getBaseContext(), getString(R.string.no_device_found), Toast.LENGTH_LONG).show();
        } else {
            if (DEBUG) Log.i(TAG, "Device found!");
            Toast.makeText(getBaseContext(), getString(R.string.device_found), Toast.LENGTH_SHORT).show();
            Intent startIntent = new Intent(getApplicationContext(), ArduinoCommunicatorService.class);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, startIntent, 0);
            usbManager.requestPermission(usbDevice, pendingIntent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "onCreate()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(ArduinoCommunicatorService.DATA_RECEIVED_INTENT);
        filter.addAction(ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT);
        registerReceiver(mReceiver, filter);

        mDataAdapter = new ArrayAdapter<ByteString>(this, android.R.layout.simple_list_item_1, mTransferedDataList);
        setListAdapter(mDataAdapter);

        findDevice();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (DEBUG) Log.i(TAG, "onListItemClick() " + position + " " + id);
        ByteString transferedData = mTransferedDataList.get(position);
        transferedData.toggleCoding();
        mTransferedDataList.set(position, transferedData);
        mDataAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
        super.onNewIntent(intent);

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.contains(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "onNewIntent() " + intent);
            findDevice();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy()");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.help:
            startActivity(new Intent(this, Help.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        private void handleTransferedData(Intent intent, boolean receiving) {
            if (mIsReceiving == null || mIsReceiving != receiving) {
                mIsReceiving = receiving;
                mTransferedDataList.add(new ByteString());
            }

            final byte[] newTransferedData = intent.getByteArrayExtra(ArduinoCommunicatorService.DATA_EXTRA);
            if (DEBUG) Log.i(TAG, "data: " + newTransferedData.length + " \"" + new String(newTransferedData) + "\"");

            ByteString transferedData = mTransferedDataList.get(mTransferedDataList.size() - 1);
            transferedData.add(newTransferedData);
            mTransferedDataList.set(mTransferedDataList.size() - 1, transferedData);
            mDataAdapter.notifyDataSetChanged();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DEBUG) Log.d(TAG, "onReceive() " + action);

            if (ArduinoCommunicatorService.DATA_RECEIVED_INTENT.equals(action)) {
                handleTransferedData(intent, true);
            } else if (ArduinoCommunicatorService.DATA_SENT_INTERNAL_INTENT.equals(action)) {
                handleTransferedData(intent, false);
            }
        }
    };
}
