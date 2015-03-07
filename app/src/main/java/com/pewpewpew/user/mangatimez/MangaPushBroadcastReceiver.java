package com.pewpewpew.user.mangatimez;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by User on 03/3/15.
 */
public class MangaPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "MangaPushBroadcastReceiver_debug";
    private static final String FOLLOWED_MANGAS = "followed_mangas_shared_preferences";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
//        Log.i(TAG, intent.toString());
        final SharedPreferences preferences = context.getSharedPreferences(FOLLOWED_MANGAS, 0);
//        Log.i(TAG, context.getSharedPreferences("followed_mangas_shared_preferences",0).getBoolean("tower-of-god",false)+"");
        if(intent.getExtras()!= null){
            String json = intent.getExtras().getString("com.parse.Data");
            Log.i(TAG, "Json: "+json);
            try {
                JSONObject data = new JSONObject(json);
                JSONArray array = data.getJSONArray("updated");
                String notificationStr = "Updates: ";
                boolean hasUpdates = false;
                if(array != null) {
                    Log.i(TAG, "Followed mangas: "+array.toString());
                    for(int i = 0; i<array.length(); i++){
                        String key = array.getString(i);

                        if(preferences.getBoolean(key,false)){
                            hasUpdates = true;
                            String formatting = i==array.length() -1? "":", ";
                            notificationStr += key+ formatting;
                        }
                    }

                    if(hasUpdates){
                        Log.i(TAG, "Notification String: " + notificationStr);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentText(notificationStr);
                        builder.setContentTitle("It's MangaTime!");
                        builder.setSmallIcon(R.drawable.ic_launcher);
                        NotificationManager mNotificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(1, builder.build());

//                        super.onPushReceive(context, intent);
                    }
                }
            } catch (JSONException e) {
                Log.i(TAG, "No json array found!");
                e.printStackTrace();
            }
        }


    }
}
