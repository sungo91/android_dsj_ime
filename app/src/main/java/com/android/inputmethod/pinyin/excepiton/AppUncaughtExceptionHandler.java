package com.android.inputmethod.pinyin.excepiton;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final String TAG = "AppUncaughtException";
    private final String LogDir = "/mnt/sdcard/dsj/ime/logs/";

    private static AppUncaughtExceptionHandler instance = new AppUncaughtExceptionHandler();
    private Context applicationContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private boolean crashing;
    private DateFormat mFormatter;

    public static AppUncaughtExceptionHandler getInstance() {
        return instance;
    }

    /**
     * 初始化捕获类
     *
     * @param context
     */
    public void init(Context context) {
        applicationContext = context.getApplicationContext();
        crashing = false;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        mFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

        Thread createLogDirThread = new Thread("ime_log_dir") {
            @Override
            public void run() {
                File dir = new File(LogDir);
                if (!dir.exists()) {
                    boolean f = dir.mkdirs();
                    Log.i(TAG, "ime_log_dir create =" + f);
                } else {
                    Log.i(TAG, "ime_log_dir exists() = true");
                }
            }
        };
        createLogDirThread.start();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (crashing) {
            return;
        }
        crashing = true;

        // 打印异常信息
        ex.printStackTrace();
        // 我们没有处理异常 并且默认异常处理不为空 则交给系统处理
        if (!handlelException(ex) && mDefaultHandler != null) {
            // 系统处理
            mDefaultHandler.uncaughtException(thread, ex);
        }
        quitApp();
    }

    private boolean handlelException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        try {
            // 异常信息
            String crashReport = getCrashReport(ex);
            // 保存到sd卡
            saveExceptionToSdcard(crashReport);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取异常信息
     * @param ex
     * @return
     */
    private String getCrashReport(Throwable ex) {
        StringBuffer exceptionStr = new StringBuffer();
        if (ex != null) {
            String errorStr = ex.getLocalizedMessage();
            if (TextUtils.isEmpty(errorStr)) {
                errorStr = ex.getMessage();
            }
            if (TextUtils.isEmpty(errorStr)) {
                errorStr = ex.toString();
            }
            exceptionStr.append("Exception: " + errorStr + "\n");
            StackTraceElement[] elements = ex.getStackTrace();
            if (elements != null) {
                for (int i = 0; i < elements.length; i++) {
                    exceptionStr.append(elements[i].toString() + "\n");
                }
            }
        } else {
            exceptionStr.append("no exception. Throwable is null\n");
        }
        return exceptionStr.toString();
    }

    /**
     * 保存错误报告到sd卡
     * @param errorReason
     */
    private void saveExceptionToSdcard(String errorReason) {
        try {
            Log.e(TAG, "AppUncaughtExceptionHandler run");
            String time = mFormatter.format(new Date());
            String fileName = "ime-crash-" + time + ".log";
            FileOutputStream fos = new FileOutputStream(LogDir + fileName);
            fos.write(errorReason.getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file..." + e.getMessage());
        }
    }

    private void quitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
