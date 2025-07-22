package com.android.inputmethod.pinyin;

import android.app.Application;

import com.android.inputmethod.pinyin.excepiton.AppUncaughtExceptionHandler;

public class IMEApplication extends Application {

    @Override public void onCreate() {
        super.onCreate();
        // 捕捉异常
        AppUncaughtExceptionHandler.getInstance().init(getApplicationContext());
    }

}
