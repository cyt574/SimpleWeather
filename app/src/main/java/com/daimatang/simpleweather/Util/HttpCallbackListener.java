package com.daimatang.simpleweather.Util;

/**
 * Created by 陈益堂 on 2016/5/4.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
