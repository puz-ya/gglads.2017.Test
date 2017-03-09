package com.py.producthuntreader;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NewPostService extends Service {

    private static final String LOG_TAG = "Service: ";

    public NewPostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean isServiceRunning(Context context) {
        Log.d(LOG_TAG, "isServiceRunning");

        final Class<?> serviceClass = NewPostService.class;

        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.d(LOG_TAG, "serviceRunningTRUE");
                return true;
            }
        }
        Log.d(LOG_TAG, "serviceRunningFALSE");
        return false;
    }

}
