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
    private List<UsbDevice> mDevices;
    private UsbDeviceConnection mConnection = null;
    private Thread mThread = null;

    private int fd = -1;

    private DeepanoGLView deepanoGLView = null;

    private DeepanoApiFactory apiFactory = null;

    boolean verifyDevice(UsbDevice device){
        Log.d(TAG, "verifyDevice : " + device.getVendorId() + ", " + device.getProductId());
        if (device.getVendorId() != 1038 || device.getProductId() != 63035)
            return false;
        Log.d(TAG, "Found matching device");
        return true;
    }

    private void scanDevices() {
        Log.d(TAG, "scanDevices E");
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            if (!verifyDevice(device))
                continue;
            Log.d(TAG, "Start to request usb permission");
            usbManager.requestPermission(device, mPermissionIntent);
            break;//TODO: only one
        }
        Log.d(TAG, "scanDevices X");
    }

    private void deviceAttached(UsbDevice device) {
        Log.d(TAG, "deviceAttached E");
        if (!verifyDevice(device)){
            return;
        }
        Log.d(TAG, "Start to request usb permission");
        usbManager.requestPermission(device, mPermissionIntent);
        Log.d(TAG, "deviceAttached X");
    }

    private void deviceDetached(UsbDevice device) {
        Log.d(TAG, "deviceDetached E");
        if (!verifyDevice(device))
            return;

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

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mConnection != null) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/SSD_MobileNet_object.blob";

                    fd = mConnection.getFileDescriptor();
                    apiFactory.initDevice(fd);
                    //DeepanoApiFactory.startCamera();
                    apiFactory.netProc(path);
                } else
                    Log.e(TAG, "UsbManager openDevice failed");
            }
        });
        mThread.start();
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

        deepanoGLView = findViewById(R.id.deepano_gl_view);
        apiFactory = DeepanoApiFactory.getApiInstance(deepanoGLView);

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

    protected synchronized void destroyDevice(UsbDevice device){
        //TODO: status check
        fd = mConnection.getFileDescriptor();
        DeepanoApiFactory.deinitDevice(fd);
        destroyDevice(device);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
        destroyDevice(mDevices.get(0));

        if (mThread != null){
            mThread.interrupt();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, e.toString());
            }
        }
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
                                    if (!verifyDevice(usbDevice))
                                        return;
                                    Log.d(TAG, "Usb Permission Granted");
                                    Log.d(TAG, usbDevice.toString());
                                    Toast.makeText(context, "ACTION_USB_PERMISSION: grant permission for:" + Integer.toHexString(usbDevice.getVendorId()) + " : " + Integer.toHexString(usbDevice.getProductId()),
                                            Toast.LENGTH_LONG).show();
                                    mDevices.add(usbDevice);
                                    processDevice(usbDevice);
                                }
                            } else {
                                Log.e(TAG, "Usb Permission Denied");
                                Toast.makeText(context, "用户不允许USB访问设备", Toast.LENGTH_LONG).show();
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
