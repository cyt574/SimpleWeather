package com.daimatang.simpleweather.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.daimatang.simpleweather.Base.MyApplication;
import com.daimatang.simpleweather.Receiver.AutoUpdateReceiver;
import com.daimatang.simpleweather.Util.HefengResult;
import com.daimatang.simpleweather.Util.HefengWeather;

import static com.daimatang.simpleweather.Util.HefengWeather.getWeatherInfoToShow;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpdateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);

    }

    private void updateWeather(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication
                .getContext());
                HefengResult hefengResult = HefengWeather.requestData(prefs.getString("city_name",""),true);
                getWeatherInfoToShow(MyApplication.getContext(), hefengResult,true);
            }
        }).start();
    }
}
