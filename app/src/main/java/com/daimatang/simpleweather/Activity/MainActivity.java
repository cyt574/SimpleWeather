package com.daimatang.simpleweather.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.daimatang.simpleweather.Base.MyActivity;
import com.daimatang.simpleweather.Base.MyApplication;
import com.daimatang.simpleweather.R;
import com.daimatang.simpleweather.Service.AutoUpdateService;
import com.daimatang.simpleweather.Util.HefengResult;
import com.daimatang.simpleweather.Util.HefengWeather;
import com.daimatang.simpleweather.Util.ImageLoader;
import com.daimatang.simpleweather.Util.WeatherInfoToShow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends MyActivity
        implements BDLocationListener,NavigationView.OnNavigationItemSelectedListener,SwipeRefreshLayout.OnRefreshListener {

    // baidu location
    public LocationClient mLocationClient;
    public BDLocationListener myListener;


    private RelativeLayout weatherInfoLayout;
    /**
     * 用于显示城市名 private TextView cityNameText;
     */
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温
     */
    private TextView tempText;
    /**
     *用于显示天气的图像
     */
    private ImageView imageView;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 切换城市按钮
     */
    private Button switchCity;
    /**
     * 更新天气按钮 private Button refreshWeather;
     */
    private HefengResult mWeather;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView forecastDate2,forecastDate3,forecastDate4,forecastDate5,forecastDate6;
    private TextView forecastTxt,forecastTxt1,forecastTxt2,forecastTxt3,forecastTxt4,forecastTxt5,forecastTxt6;
    private TextView forecastTemp,forecastTemp1,forecastTemp2,forecastTemp3,forecastTemp4,forecastTemp5,forecastTemp6;
    private ImageView forecastImage,forecastImage1,forecastImage2,forecastImage3,forecastImage4,forecastImage5,forecastImage6;
    public static boolean isDay;
    public static String countyName;
    public static final int SHOW_WEATHER = 1;
    private static final int REFRESH_COMPLETE = 0X110;
    private long exitTime = 0; ////记录第一次点击的时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_main);
        // 初始化各控件
        init();
        initView();
        initIcon();
        if (!TextUtils.isEmpty(countyName)) {
            // 有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getTotalData(countyName);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            showWeather();
        }
        mSwipeLayout.setOnRefreshListener(this);

    }


    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SHOW_WEATHER: {
                    showWeather();
                    //  Toast.makeText(MainActivity.this,"刷新成功",Toast.LENGTH_SHORT).show();
                }
                case REFRESH_COMPLETE:
                    mSwipeLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };





    @Override
    public void onRefresh() {
        publishText.setText("同步中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                try {
                    getTotalData(prefs.getString("city_name",""));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.d("hehe",prefs.getString("city_name",""));
            }
        }).start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showWeather();
            }
        });
        Message message  = new Message();
        message.what = REFRESH_COMPLETE;
        handler.sendMessage(message);
    }



    public void getTotalData(String countyName) throws ParseException {
        HefengResult hefengResult= HefengWeather.requestData(countyName,true);

        if (hefengResult == null) {
            Toast.makeText(MainActivity.this,"无信息返回",Toast.LENGTH_SHORT).show() ;
        } else  {
            WeatherInfoToShow info = HefengWeather.getWeatherInfoToShow(this,hefengResult,isDay);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
            editor.putBoolean("city_selected", true);
            editor.putString("city_name",countyName);
            editor.putString("temp",info.tempNow+"℃");
            editor.putString("weather_Code", info.aqiLevel);
            editor.putString("weather_desp", info.weatherNow);
            editor.putString("publish_time", info.refreshTime);
            editor.putString("weatherKind",info.weatherKindNow);
            editor.putString("forecastTxt",info.weather[0]+"，"+info.windDir[0]+"，"+info.windLevel[0]);
            editor.putString("forecastTxt1",info.weather[1]+"，"+info.windDir[1]+"，"+info.windLevel[1]);
            editor.putString("forecastTxt2",info.weather[2]+"，"+info.windDir[2]+"，"+info.windLevel[2]);
            editor.putString("forecastTxt3",info.weather[3]+"，"+info.windDir[3]+"，"+info.windLevel[3]);
            editor.putString("forecastTxt4",info.weather[4]+"，"+info.windDir[4]+"，"+info.windLevel[4]);
            editor.putString("forecastTxt5",info.weather[5]+"，"+info.windDir[5]+"，"+info.windLevel[5]);
            editor.putString("forecastTxt6",info.weather[6]+"，"+info.windDir[6]+"，"+info.windLevel[6]);
            editor.putString("forecastTemp",info.miniTemp[0]+"°"+info.maxiTemp[0]+"°");
            editor.putString("forecastTemp1",info.miniTemp[1]+"°"+info.maxiTemp[1]+"°");
            editor.putString("forecastTemp2",info.miniTemp[2]+"°"+info.maxiTemp[2]+"°");
            editor.putString("forecastTemp3",info.miniTemp[3]+"°"+info.maxiTemp[3]+"°");
            editor.putString("forecastTemp4",info.miniTemp[4]+"°"+info.maxiTemp[4]+"°");
            editor.putString("forecastTemp5",info.miniTemp[5]+"°"+info.maxiTemp[5]+"°");
            editor.putString("forecastTemp6",info.miniTemp[6]+"°"+info.maxiTemp[6]+"°");
            editor.putString("forecastDate2",info.week[2]);
            editor.putString("forecastDate3",info.week[3]);
            editor.putString("forecastDate4",info.week[4]);
            editor.putString("forecastDate5",info.week[5]);
            editor.putString("forecastDate6",info.week[6]);
            editor.putString("forecastImage",info.weather[0]);
            editor.putString("forecastImage1",info.weather[1]);
            editor.putString("forecastImage2",info.weather[2]);
            editor.putString("forecastImage3",info.weather[3]);
            editor.putString("forecastImage4",info.weather[4]);
            editor.putString("forecastImage5",info.weather[5]);
            editor.putString("forecastImage6",info.weather[6]);
            editor.putString("forecastImageNow",info.weatherNow);
            // String[] dateSplit = info.date.split("-");//dateSplit[0]+"年"+dateSplit[1]+"月"+dateSplit[2]+"日"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(info.date);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年M月d日");
            String currentDate = sdf2.format(date);
            editor.putString("current_date",currentDate);
            editor.commit();
            Message message  = new Message();
            message.what = SHOW_WEATHER;
            handler.sendMessage(message);

        }
    }

    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        tempText.setText(prefs.getString("temp",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText( prefs.getString("publish_time","") + "发布");
        currentDateText.setText(prefs.getString("current_date",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        collapsingToolbarLayout.setTitle(prefs.getString("city_name", ""));
        forecastDate2.setText(prefs.getString("forecastDate2",""));
        forecastDate3.setText(prefs.getString("forecastDate3",""));
        forecastDate4.setText(prefs.getString("forecastDate4",""));
        forecastDate5.setText(prefs.getString("forecastDate5",""));
        forecastDate6.setText(prefs.getString("forecastDate6",""));
        forecastTemp.setText(prefs.getString("forecastTemp",""));
        forecastTemp1.setText(prefs.getString("forecastTemp1",""));
        forecastTemp2.setText(prefs.getString("forecastTemp2",""));
        forecastTemp3.setText(prefs.getString("forecastTemp3",""));
        forecastTemp4.setText(prefs.getString("forecastTemp4",""));
        forecastTemp5.setText(prefs.getString("forecastTemp5",""));
        forecastTemp6.setText(prefs.getString("forecastTemp6",""));
        forecastTxt.setText(prefs.getString("forecastTxt",""));
        forecastTxt1.setText(prefs.getString("forecastTxt1",""));
        forecastTxt2.setText(prefs.getString("forecastTxt2",""));
        forecastTxt3.setText(prefs.getString("forecastTxt3",""));
        forecastTxt4.setText(prefs.getString("forecastTxt4",""));
        forecastTxt5.setText(prefs.getString("forecastTxt5",""));
        forecastTxt6.setText(prefs.getString("forecastTxt6",""));
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImageNow",""),R.drawable.none),imageView);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage",""),R.drawable.none),forecastImage);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage1",""),R.drawable.none),forecastImage1);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage2",""),R.drawable.none),forecastImage2);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage3",""),R.drawable.none),forecastImage3);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage4",""),R.drawable.none),forecastImage4);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage5",""),R.drawable.none),forecastImage5);
        ImageLoader.load(getApplicationContext(),mSetting.getInt(prefs.getString("forecastImage6",""),R.drawable.none),forecastImage6);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
    public void init(){
        weatherInfoLayout = (RelativeLayout) findViewById(R.id.weather_info_layout);;
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        tempText = (TextView) findViewById(R.id.temp);
        currentDateText = (TextView) findViewById(R.id.current_date);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        countyName = getIntent().getStringExtra("county_name");
        imageView = (ImageView) findViewById(R.id.imageView1);
        forecastTemp = (TextView)findViewById(R.id.forecast_temp);
        forecastTemp1 = (TextView)findViewById(R.id.forecast_temp1);
        forecastTemp2 = (TextView)findViewById(R.id.forecast_temp2);
        forecastTemp3 = (TextView)findViewById(R.id.forecast_temp3);
        forecastTemp4 = (TextView)findViewById(R.id.forecast_temp4);
        forecastTemp5 = (TextView)findViewById(R.id.forecast_temp5);
        forecastTemp6 = (TextView)findViewById(R.id.forecast_temp6);
        forecastTxt = (TextView)findViewById(R.id.forecast_txt);
        forecastTxt1 = (TextView)findViewById(R.id.forecast_txt1);
        forecastTxt2 = (TextView)findViewById(R.id.forecast_txt2);
        forecastTxt3 = (TextView)findViewById(R.id.forecast_txt3);
        forecastTxt4 = (TextView)findViewById(R.id.forecast_txt4);
        forecastTxt5 = (TextView)findViewById(R.id.forecast_txt5);
        forecastTxt6 = (TextView)findViewById(R.id.forecast_txt6);
        forecastDate2 = (TextView) findViewById(R.id.forecast_date2);
        forecastDate3 = (TextView) findViewById(R.id.forecast_date3);
        forecastDate4 = (TextView) findViewById(R.id.forecast_date4);
        forecastDate5 = (TextView) findViewById(R.id.forecast_date5);
        forecastDate6 = (TextView) findViewById(R.id.forecast_date6);
        forecastImage = (ImageView) findViewById(R.id.forecast_icon);
        forecastImage1 = (ImageView) findViewById(R.id.forecast_icon1);
        forecastImage2 = (ImageView) findViewById(R.id.forecast_icon2);
        forecastImage3 = (ImageView) findViewById(R.id.forecast_icon3);
        forecastImage4 = (ImageView) findViewById(R.id.forecast_icon4);
        forecastImage5 = (ImageView) findViewById(R.id.forecast_icon5);
        forecastImage6 = (ImageView) findViewById(R.id.forecast_icon6);
    }
    // baidu location
/*
*
* private void initBaiduLocation(){
        mLocationClient = new LocationClient(this);     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=0;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

*
* */


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        //Receive Location
        String location = null;

        StringBuilder sb = new StringBuilder(256);
        sb.append("time : ");
        sb.append(bdLocation.getTime());
        sb.append("\nerror code : ");
        sb.append(bdLocation.getLocType());
        sb.append("\nlatitude : ");
        sb.append(bdLocation.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(bdLocation.getLongitude());
        sb.append("\nradius : ");
        sb.append(bdLocation.getRadius());
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
            location = bdLocation.getCity();
        } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }

        if(location == null) {
            Toast.makeText(this,
                    getString(R.string.get_location_failed),
                    Toast.LENGTH_SHORT).show();
        }

        sb.append("\nlocationdescribe : ");
        sb.append(bdLocation.getLocationDescribe());// 位置语义化信息
        List<Poi> list = bdLocation.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId()).
                        append(" ").
                        append(p.getName()).
                        append(" ").
                        append(p.getRank());
            }
        }
        Log.i("BaiduLocationApiDem", sb.toString());
        mLocationClient.stop();
    }



    /**
     * 初始化Icon
     */
    public void initIcon() {
        mSetting = Setting.getInstance();
        mSetting.setIconType(0);
        if (mSetting.getIconType() == 0) {
            mSetting.putInt("未知", R.drawable.none);
            mSetting.putInt("晴", R.drawable.type_one_sunny);
            mSetting.putInt("阴", R.drawable.type_one_cloudy);
            mSetting.putInt("多云", R.drawable.type_one_cloudy);
            mSetting.putInt("少云", R.drawable.type_one_cloudy);
            mSetting.putInt("晴间多云", R.drawable.type_one_cloudytosunny);
            mSetting.putInt("小雨", R.drawable.type_one_light_rain);
            mSetting.putInt("中雨", R.drawable.type_one_light_rain);
            mSetting.putInt("大雨", R.drawable.type_one_heavy_rain);
            mSetting.putInt("阵雨", R.drawable.type_one_thunderstorm);
            mSetting.putInt("雷阵雨", R.drawable.type_one_thunder_rain);
            mSetting.putInt("霾", R.drawable.type_one_fog);
            mSetting.putInt("雾", R.drawable.type_one_fog);
        } else {
            mSetting.putInt("未知", R.drawable.none);
            mSetting.putInt("晴", R.drawable.type_two_sunny);
            mSetting.putInt("阴", R.drawable.type_two_cloudy);
            mSetting.putInt("多云", R.drawable.type_two_cloudy);
            mSetting.putInt("少云", R.drawable.type_two_cloudy);
            mSetting.putInt("晴间多云", R.drawable.type_two_cloudytosunny);
            mSetting.putInt("小雨", R.drawable.type_two_light_rain);
            mSetting.putInt("中雨", R.drawable.type_two_rain);
            mSetting.putInt("大雨", R.drawable.type_two_rain);
            mSetting.putInt("阵雨", R.drawable.type_two_rain);
            mSetting.putInt("雷阵雨", R.drawable.type_two_thunderstorm);
            mSetting.putInt("霾", R.drawable.type_two_haze);
            mSetting.putInt("雾", R.drawable.type_two_fog);
            mSetting.putInt("雨夹雪", R.drawable.type_two_snowrain);
        }
    }

    public  void initView(){
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        // http://stackoverflow.com/questions/30655939/programmatically-collapse-or-expand-collapsingtoolbarlayout
        if (appBarLayout != null) {
            //控制是否展开
            appBarLayout.setExpanded(false);
        }
        /**
         * Toolbar and DrawerLayout
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.tool_bar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(" ");
        }

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_SHORT);
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_location) {
            Intent intent = new Intent(this, ChooseAreaActivity.class);
            intent.putExtra("from_weather_activity", true);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static boolean needChangeTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return 5 < hour && hour < 19 && !MainActivity.isDay || (hour < 6 || hour > 18) && MainActivity.isDay;
    }


    private void initWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void setStatusBarTransParent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}