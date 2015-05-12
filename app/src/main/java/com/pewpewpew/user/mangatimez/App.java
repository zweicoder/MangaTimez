package com.pewpewpew.user.mangatimez;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.w3c.dom.Comment;

/**
 * Created by User on 24/2/15.
 */
public class App extends Application {
    private static final String TAG = "App_debug";
    static String PARSE_APPLICATION_ID = "l7YBkwX1OkJ52LzC3MJbMLkGVNupRSgzVYkx6X7U";
    static String PARSE_CLIENT_KEY = "9P1BW1fm5FiJCg5M9Smd35Y056OTRCMRnWv79uS7";
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Manga.class);
        Parse.initialize(this, PARSE_APPLICATION_ID, PARSE_CLIENT_KEY);

        // Channel "" is the default broadcast channel
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });
    }
}
