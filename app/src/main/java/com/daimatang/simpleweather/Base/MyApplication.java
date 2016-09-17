package com.daimatang.simpleweather.Base;

import android.app.Application;
import android.content.Context;

/**
 * Created by 陈益堂 on 2016/5/12.
 */
public class MyApplication extends Application{
    private  static Context context;

    @Override
    public void onCreate() {
        context =getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
