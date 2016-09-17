package com.daimatang.simpleweather.Model;

import android.text.TextUtils;

import com.daimatang.simpleweather.DB.SimpleWeatherDB;
import com.daimatang.simpleweather.Service.AutoUpdateService;

/**
 * Created by 陈益堂 on 2016/5/4.
 */
public class Utility {
    /*
 * 解析和处理服务器返回的省级数据
 */
    public synchronized static boolean handleProvincesResponse(SimpleWeatherDB simpleweatherDB,String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if (allProvinces != null&&allProvinces.length >0) {
                for (String c: allProvinces){
                    String[] array = c.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    simpleweatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    /*
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleCitiesResponse(SimpleWeatherDB simpleweatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null&&allCities.length >0) {
                for (String c: allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    simpleweatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    public static boolean handleCountiesResponse(SimpleWeatherDB simpleWeatherDB, String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null&&allCounties.length >0) {
                for (String c: allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    simpleWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

}
