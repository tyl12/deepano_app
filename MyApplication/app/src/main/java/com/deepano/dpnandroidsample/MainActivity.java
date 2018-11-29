package com.deepano.dpnandroidsample;

import android.Manifest;
import android.app.PendingIntent;
import android.companion.DeviceFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.deepano.dpnandroidsample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DeepanoApi";
    final String ACTION_USB_PERMISSION = "Deepano_USB_PERMISSION";

    private UsbManager usbManager = null;
    private PendingIntent mPermissionIntent = null;
    List<UsbDevice> mDevices;
    private UsbDeviceConnection mConnection = null;

    private int fd = -1;

    private void scanDevices() {
        Log.d(TAG, "scanDevices E");
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d(TAG, "scanDevices : " + device.getVendorId() + ", " + device.getProductId());

            if (device.getVendorId() != 1038 || device.getProductId() != 63035){
                Log.d(TAG, "unmatched device, skip");
                continue;
            }
            Log.d(TAG, "found matched device, request permission");
            usbManager.requestPermission(device, mPermissionIntent);
        }
        Log.d(TAG, "scanDevices X");
    }

    private void deviceAttached(UsbDevice device) {
        Log.d(TAG, "deviceAttached E");
        if (device.getVendorId() != 1038 || device.getProductId() != 63035){
            Log.d(TAG, "unmatched device, skip");
            return;
        }
        Log.d(TAG, "found matched device, request permission");
        usbManager.requestPermission(device, mPermissionIntent);
        Log.d(TAG, "deviceAttached X");
    }

    private void deviceDetached(UsbDevice device) {
        Log.d(TAG, "deviceDetached E");
        if (device.getVendorId() != 1038 || device.getProductId() != 63035) {
            Log.d(TAG, "unmatched device, skip");
            return;
        }
        Log.d(TAG, "found matched device, remove it");
        UsbDevice dev = mDevices.get(0);
        if (dev.equals(device)){
            Log.e(TAG, "FOUND MATCHING DEV");
        }
        mConnection.close();
        mDevices.clear();

        Log.d(TAG, "deviceDetached X");
    }

    private void processDevice(UsbDevice usbDevice){

        boolean forceClaim = true;
        UsbInterface intf = usbDevice.getInterface(0);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        mConnection = usbManager.openDevice(usbDevice);
        mConnection.claimInterface(intf, forceClaim);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/SSD_MobileNet_object.blob";

                    fd = mConnection.getFileDescriptor();
                    DeepanoApiFactory.initDevice(fd);
                    //DeepanoApiFactory.startCamera();
                    DeepanoApiFactory.netProc(path);
                } else
                    Log.e(TAG, "UsbManager openDevice failed");
            }
        }).start();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"storage Permission is granted");
                return true;
            } else {
                Log.v(TAG,"storage Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"storage Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate start");
        setContentView(R.layout.activity_main);

        isStoragePermissionGranted();

        mDevices = new ArrayList<UsbDevice>();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);

        scanDevices(); //manual scan

        Log.d(TAG, "onCreate stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver onReceive");
            String action = intent.getAction();
            if(action == null){
                Log.e(TAG,"BroadcastReceiver action is null");
            }else{
                switch (action){
                    case ACTION_USB_PERMISSION:
                        Toast.makeText(context, "ACTION_USB_PERMISSION", Toast.LENGTH_LONG).show();

                        Log.e(TAG,"ACTION_USB_PERMISSION");
                        synchronized (this){
                            UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                if (usbDevice != null) {
//                                    Log.e(TAG,  "##@@## => " + usbDevice.getDeviceName() + " " + Integer.toHexString(usbDevice.getVendorId()) +
//                                            " " + Integer.toHexString(usbDevice.getProductId()) + " " + usbDevice.getDeviceClass() + " " + usbDevice.getDeviceSubclass() );
                                    Toast.makeText(context, "ACTION_USB_PERMISSION: grant permission for:" + Integer.toHexString(usbDevice.getVendorId()) + " : " + Integer.toHexString(usbDevice.getProductId()),
                                            Toast.LENGTH_LONG).show();

                                    if (usbDevice.getVendorId() != 1038 || usbDevice.getProductId() != 63035){
                                        Log.e(TAG, "##@@## error: permission granted for unmatched device, skip");
                                        return;
                                    }
                                    Log.d(TAG, "add device to mDevices list");
                                    Log.d(TAG, usbDevice.toString());

                                    mDevices.add(usbDevice);
                                    processDevice(usbDevice);
                                }
                            } else {
                                Log.e(TAG, "permission is denied");
                                Toast.makeText(context, "用户不允许USB访问设备，程序退出！", Toast.LENGTH_LONG).show();
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                finish();
                            }
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED: {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        Log.d(TAG, "ACTION_USB_DEVICE_ATTACHED : " + usbDevice.getVendorId() + ", " + usbDevice.getProductId());
                        Toast.makeText(context, "ACTION_USB_DEVICE_ATTACHED", Toast.LENGTH_LONG).show();
                        deviceAttached(usbDevice);

                        break;
                    }
                    case UsbManager.ACTION_USB_DEVICE_DETACHED: {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        Log.d(TAG, "ACTION_USB_DEVICE_ATTACHED : " + usbDevice.getVendorId() + ", " + usbDevice.getProductId());
                        Toast.makeText(context, "ACTION_USB_DEVICE_DETACHED", Toast.LENGTH_LONG).show();
                        deviceDetached(usbDevice);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    };

}
