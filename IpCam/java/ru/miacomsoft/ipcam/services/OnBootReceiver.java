package ru.miacomsoft.ipcam.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;

import ru.miacomsoft.ipcam.Lib.Sys;



public class OnBootReceiver extends BroadcastReceiver {
    private HashMap<String, String> Setup = new HashMap<String, String>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            HashMap<String, String> Setup = Sys.readFile(context, "conf.ini");
            if (Setup != null) {
                if (Setup.get("run").equals("1")) {
                    Intent serviceLauncher = new Intent(context, ServiceExample.class);
                    context.startService(serviceLauncher);
                }
            }
        }
    }
}
