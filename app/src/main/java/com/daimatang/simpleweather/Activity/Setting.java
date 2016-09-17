package com.daimatang.simpleweather.Activity;

import android.content.Context;
import android.content.SharedPreferences;

import com.daimatang.simpleweather.Base.MyApplication;

/**
 * Created by 陈益堂 on 2016/9/16.
 */
public class Setting {
    private static Setting sInstance;
    public static final String CHANGE_ICONS = "change_icons";//切换图标
    public static final String CLEAR_CACHE = "clear_cache";//清空缓存
    public static final String AUTO_UPDATE = "change_update_time"; //自动更新时长
    public static final String CITY_NAME = "城市";//选择城市
    public static final String HOUR = "current_hour";//当前小时
    public static final String NOTIFICATION_MODEL = "notification_model";
    private SharedPreferences mPrefs;
    public static Setting getInstance() {
        if (sInstance == null) {
            sInstance = new Setting(MyApplication.getContext());
        }
        return sInstance;
    }

    private Setting(Context context) {
        mPrefs = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        //mPrefs.edit().putInt(CHANGE_ICONS, 1).apply();
    }

    public int getInt(String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    public Setting putInt(String key, int value) {
        mPrefs.edit().putInt(key, value).apply();
        return this;
    }


    // 图标种类相关
    public void setIconType(int type) {
        mPrefs.edit().putInt(CHANGE_ICONS, type).apply();
    }

    public int getIconType() {
        return mPrefs.getInt(CHANGE_ICONS, 0);
    }

    // 设置当前小时
    public void setCurrentHour(int h){
        mPrefs.edit().putInt(HOUR,h).apply();
    }
    public int getCurrentHour(){
        return mPrefs.getInt(HOUR,0);
    }
    //当前城市
    public void setCityName(String name) {
        mPrefs.edit().putString(CITY_NAME, name).apply();
    }
}
