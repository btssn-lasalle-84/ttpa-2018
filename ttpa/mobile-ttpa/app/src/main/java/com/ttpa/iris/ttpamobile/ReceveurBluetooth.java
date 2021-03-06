package com.ttpa.iris.ttpamobile;

/**
 * Created by smaniotto on 04/04/18.
 */

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReceveurBluetooth extends BroadcastReceiver
{
    public ReceveurBluetooth()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(context, "Nouveau périphérique : " + device.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}