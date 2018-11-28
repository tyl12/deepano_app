package com.deepano.dpnandroidsample;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
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

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DeepanoApi";
    final String ACTION_USB_PERMISSION = "Deepano_USB_PERMISSION";

    private UsbManager usbManager = null;
    private PendingIntent mPermissionIntent = null;

    private int fd = -1;


//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE" };
//    public static void verifyStoragePermissions(Activity activity)
//    {
//        try {
//            //检测是否有写的权
//            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
//         if (permission != PackageManager.PERMISSION_GRANTED) {
//            // 没有写的权限，去申请写的权限，会弹出对话框
//            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE); } }
//            catch (Exception e) {
//                e.printStackTrace();
//            }
//    }
//    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate start");
        setContentView(R.layout.activity_main);

        isStoragePermissionGranted();

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        Log.d(TAG, "onCreate stop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver start");
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
                                    Log.e(TAG,  "##@@## => " + usbDevice.getDeviceName() + " " + Integer.toHexString(usbDevice.getVendorId()) +
                                            " " + Integer.toHexString(usbDevice.getProductId()));
                                    Toast.makeText(context, "ACTION_USB_PERMISSION: grant permission for:" + Integer.toHexString(usbDevice.getVendorId()) + " : " + Integer.toHexString(usbDevice.getProductId()),
                                            Toast.LENGTH_LONG).show();

                                    if (usbDevice.getVendorId() != 1038 || usbDevice.getProductId() != 63035){
                                        Log.e(TAG, "##@@## error: permission granted for unmatched device, skip");
                                        return;
                                    }
                                    final UsbDeviceConnection connection = usbManager.openDevice(usbDevice);

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (connection != null) {
                                                String path =
                                                        Environment.getExternalStorageDirectory().getPath() + "/SSD_MobileNet_object.blob";

                                                fd = connection.getFileDescriptor();
                                                DeepanoApiFactory.initDevice(fd);
                                                //DeepanoApiFactory.startCamera();
                                                DeepanoApiFactory.netProc(path);
                                            }
                                            else
                                                Log.e(TAG, "UsbManager openDevice failed");
                                        }
                                    }).start();
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
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        Toast.makeText(context, "ACTION_USB_DEVICE_ATTACHED", Toast.LENGTH_LONG).show();
                        Log.e(TAG,"ACTION_USB_DEVICE_ATTACHED");
                        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
                        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                        while (deviceIterator.hasNext()) {
                            UsbDevice device = deviceIterator.next();
                            Log.e(TAG, "##@@##" + device.getDeviceName() + " " + Integer.toHexString(device.getVendorId()) +
                                    " " + Integer.toHexString(device.getProductId()));

                            if (device.getVendorId() != 1038 || device.getProductId() != 63035){
                                Log.e(TAG, "##@@## unmatched device, skip");
                                continue;
                            }
                            Log.e(TAG, "##@@## found matched device, request permission");
                            usbManager.requestPermission(device, mPermissionIntent);
                        }

                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        Toast.makeText(context, "ACTION_USB_DEVICE_DETACHED", Toast.LENGTH_LONG).show();
                        Log.e(TAG,"ACTION_USB_DEVICE_DETACHED");
                        break;
                    case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                        Toast.makeText(context, "ACTION_USB_ACCESSORY_ATTACHED", Toast.LENGTH_LONG).show();
                        Log.e(TAG,"ACTION_USB_ACCESSORY_ATTACHED");
                        break;
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                        Toast.makeText(context, "ACTION_USB_ACCESSORY_DETACHED", Toast.LENGTH_LONG).show();
                        Log.e(TAG,"ACTION_USB_ACCESSORY_DETACHED");
                        break;
                    default:
                        break;
                }
            }
        }
    };

}
