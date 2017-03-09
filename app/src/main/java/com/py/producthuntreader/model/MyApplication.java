package com.py.producthuntreader.model;

import android.app.Application;
import android.content.Context;

/**
 * Created by Puzino Yury on 09.03.2017.
 */

public class MyApplication extends Application {
    private MyApplication mInstance;
    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        this.setAppContext(getApplicationContext());
    }

    public MyApplication getInstance() {
        return mInstance;
    }
    public static Context getAppContext() {
        return mAppContext;
    }
    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }
}
