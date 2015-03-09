package com.pewpewpew.user.mangatimez;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// It's MangaTime! App to inform user when a new manga is available (check various sources in future)
// Stop refreshing webpages for those week/month long updates, download to get notifications ASAP!

// Checks Mangareader, Mangapark, naver? (overtoon?)
/**
 * Main Activity.
 * - Contains a ListView which contains info on Manga and latest updated/chapter. Update this ListView
 *   if it is user's first time adding a new Manga to it
 * - Contains a '+' action button to follow another Manga
 */
public class MainActivity extends ActionBarActivity {

    private static final String FOLLOWED_MANGAS = "followed_mangas_shared_preferences";
    private static final int REQUEST_ADD_ITEM = 111;
    SharedPreferences preferences;

    private static final String TAG = "MainActivity_debug";
    private ParseQueryAdapter mAdapter;
    static ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Button button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // call updating function here
//                // use AsyncTask instead to refresh a ListView or something
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
////                        sendPushNotification();
////                        checkUpdateFromWebsite();
//                        callAlarmService();
//                    }
//                }).start();
//
//            }
//        });



        preferences = getSharedPreferences(FOLLOWED_MANGAS, 0);
        boolean hasItems = false;
//        Log.i(TAG, preferences.getAll().toString());
        for(String key:preferences.getAll().keySet()){
//            Log.i(TAG, key);
            if(preferences.getBoolean(key,false)){
                hasItems = true; break;
            }
        }
        if(!hasItems){
            Log.i(TAG, "User has not followed anything, displaying placeholder textview");
        }else{
            showListView();
        }
    }

    /**
     * Hides the placeholder, displays the ListView then creates an Adapter and links them both together.
     */
    private void showListView(){
        ((TextView)findViewById(R.id.placeholder)).setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.listView_main);
        mAdapter = createParseAdapter();
        listView.setAdapter(mAdapter);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ADD_ITEM){
            if(resultCode == RESULT_OK)refreshAdapter();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendPushNotification() {
        ParsePush push = new ParsePush();
        push.setChannel("");
//        push.setMessage("ayy lmao");
        ArrayList<String> asd = new ArrayList<String>();
        asd.add("Tower of God");
//        asd.add("dsa");
        JSONArray jsonArray = new JSONArray(asd);
        JSONObject data = new JSONObject();
        try {
            data.put("updated", jsonArray);
            push.setData(data);
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if(e== null){
                        Log.i(TAG, "sent push");
                    }else{
                        Log.i(TAG, "Error: " + e.getCode());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private ParseQueryAdapter createParseAdapter() {

        ParseQueryAdapter.QueryFactory<Manga> factory = new ParseQueryAdapter.QueryFactory<Manga>() {
            @Override
            public ParseQuery<Manga> create() {
                preferences = getSharedPreferences(FOLLOWED_MANGAS, 0);
                // Use more updated data
                final ArrayList<String> items = new ArrayList<String>();
                for(String key:preferences.getAll().keySet()){
                    if(preferences.getBoolean(key,false)){
                        items.add(key);
                    }
                }
                ParseQuery<Manga> query = ParseQuery.getQuery(Manga.class);
                query.whereContainedIn("readableName", items);
//                query.whereEqualTo("name",)
                query.orderByAscending("readableName");
                return query;
            }
        };
       return new ParseQueryAdapter<Manga>(this, factory){
            @Override
            public View getItemView(final Manga manga, View v, ViewGroup parent) {
                if (v == null){
                    v= getLayoutInflater().inflate(R.layout.list_item_main, parent, false);
                }

                final String title = manga.getReadableName();
                String latestChapter = "Chapter: "+manga.getMangaChapter();
                ((TextView) v.findViewById(R.id.title)).setText(trim(title, 18)); // for those long ass titles
                ((TextView) v.findViewById(R.id.latestChapter)).setText(latestChapter);
                ((ImageButton) v.findViewById(R.id.button_unfollow)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Removing Manga from followed list");
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(manga.getReadableName(), false);
                        editor.commit();
                        refreshAdapter();
                    }
                });
                return v;
            }
        };
    }

    /**
     * Refreshes the adapter if there is one, else calls showListView to manage the proper logic
     */
    private void refreshAdapter() {
        Log.i(TAG, "Refreshing Adapter");
        if(mAdapter == null){
//            mAdapter = createParseAdapter();
//            listView.setAdapter(mAdapter);
            showListView();
        }
        mAdapter.notifyDataSetChanged();
        mAdapter.loadObjects();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_add_item){
            System.out.println("Adding new item");
            startActivityForResult(new Intent(this, AddItemActivity.class), REQUEST_ADD_ITEM);
            return true;
        }else if(id == R.id.action_refresh){
            refreshAdapter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private void callAlarmService(){
        final PendingIntent alarmIntent = PendingIntent.getService(
                MainActivity.this, 0, new Intent(MainActivity.this, AlarmService.class), 0);
        final AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        am.cancel(alarmIntent);
        // Schedule the alarm
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),5*60*1000, alarmIntent);
    }

    private static String trim(String text, int i) {
        if(text.length() >i){
            return text.substring(0,i)+"..";
        }else{
            return text;
        }
    }

    private static void print(String s, Object... args) {
        Log.i(TAG, String.format(s, args));
    }


    private void sendPushNotification(ArrayList<String> updatedManga) {
        ParsePush push = new ParsePush();
        push.setChannel("");
        JSONArray jsonArray = new JSONArray(updatedManga);
        JSONObject data = new JSONObject();
        try {
            data.put("updated", jsonArray);
            push.setData(data);
            push.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if(e== null){
                        Log.i(TAG, "sent push");
                    }else{
                        Log.i(TAG, "Error: " + e.getCode());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
