package com.pewpewpew.user.mangatimez;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by User on 04/3/15.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver_debug";

    @Override
    public void onReceive(Context context, Intent intent) {
//        ParsePush.subscribeInBackground("", new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
//                } else {
//                    Log.e("com.parse.push", "failed to subscribe for push", e);
//                }
//            }
//        });
        Log.i(TAG, "Starting AlarmService");
        callAlarmService(context);
    }

    private void callAlarmService(Context context){
        final PendingIntent alarmIntent = PendingIntent.getService(
                context, 0, new Intent(context, AlarmService.class), 0);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent);
        // Schedule the alarm
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),AlarmManager.INTERVAL_HALF_DAY, alarmIntent);
    }
}
