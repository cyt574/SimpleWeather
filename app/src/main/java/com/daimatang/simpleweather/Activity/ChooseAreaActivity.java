package com.daimatang.simpleweather.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.daimatang.simpleweather.DB.SimpleWeatherDB;
import com.daimatang.simpleweather.Model.City;
import com.daimatang.simpleweather.Model.County;
import com.daimatang.simpleweather.Model.Province;
import com.daimatang.simpleweather.R;
import com.daimatang.simpleweather.Util.HttpCallbackListener;
import com.daimatang.simpleweather.Util.HttpUtil;
import com.daimatang.simpleweather.Model.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
   // private TextView titleText;
    private ImageView imageView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private SimpleWeatherDB simpleweatherDB;
    private List<String> dataList = new ArrayList<String>();

    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */

    private int currentLevel;

    /**
     * 是否从WeatherActivity中跳转过来
     */
    private boolean isFromWeatherAcitvity;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        isFromWeatherAcitvity = getIntent().getBooleanExtra("from_weather_activity", false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("city_selected", false)&&!isFromWeatherAcitvity) {
            Intent intent = new Intent (this,MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
      //  titleText = (TextView) findViewById(R.id.title_text);
        imageView = (ImageView) findViewById(R.id.header_view);
        imageView.setImageResource(R.drawable.image_city);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        simpleweatherDB = SimpleWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?>arg0, View view, int index, long arg3){
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(index);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY) {
                    selectedCity = cityList.get(index);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY) {
                    String countyName  = countyList.get(index).getCountyName();
                    Intent intent = new Intent(ChooseAreaActivity.this,MainActivity.class);
                    intent.putExtra("county_name", countyName);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }
    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询，如果没有查询到再去服务器上查询。
     */
    private void queryProvinces(){
        provinceList = simpleweatherDB.loadProvinces();
        if(provinceList.size()>0){
            dataList.clear();
            for (Province province :provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
           // titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null,"province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        cityList = simpleweatherDB.loadCities(selectedProvince.getId());
        if(cityList.size() > 0) {
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器查询。
     */

    private void queryCounties(){
        countyList = simpleweatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0) {
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
         //   titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    /**
     * 根据传入的代号和类型从服务器上查询省市县数据。
     */
    private void queryFromServer(final String code, final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
            @Override
            public void onFinish(String response){
                boolean result = false;
                if ("province".equals(type))
                    result = Utility.handleProvincesResponse(simpleweatherDB,response);
                else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(simpleweatherDB, response, selectedProvince.getId());
                }else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(simpleweatherDB, response, selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            }else if ("city".equals(type)) {
                                queryCities();
                            }else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }

            }
            @Override
            public void onError(Exception e){
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */

    private void showProgressDialog(){
        if(progressDialog ==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }
    }
    /**
     * 捕获Back按键，根据当前的等级来判断，此时应该返回市列表、省列表、还是直接退出。
     */
    @Override
    public void onBackPressed(){
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if ( currentLevel == LEVEL_CITY) {
            queryProvinces();
        }else {
            if (isFromWeatherAcitvity) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}

