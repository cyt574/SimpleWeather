package com.daimatang.simpleweather.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.daimatang.simpleweather.Service.AutoUpdateService;

/**
 * Created by 陈益堂 on 2016/5/4.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Intent i = new Intent (context,AutoUpdateService.class);
        context.startService(i);
    }
}
