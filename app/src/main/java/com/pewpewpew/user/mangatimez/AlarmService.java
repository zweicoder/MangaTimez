package com.pewpewpew.user.mangatimez;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.ParseQuery;
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

/**
 * Created by User on 01/3/15.
 */
public class AlarmService extends Service {
    private static final String TAG = "AlarmService_Debug";
    private static final String FOLLOWED_MANGAS = "followed_mangas_shared_preferences";
    final ArrayList<String> updatedManga = new ArrayList<String>(); // to parse as JSONArray for Push notification

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Checking for updates");
                checkUpdateFromWebsite();
            }
        }).start();



        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "rip service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    private void checkUpdateFromWebsite(){
//        final SharedPreferences preferences = getSharedPreferences(FOLLOWED_MANGAS, 0);

        String url = "http://www.mangareader.net";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR FROM PARSING WEBSITE");
        }
        if(doc==null){
            Log.i(TAG, "Probably no network connection.");
            return;
        }
        Elements updates = doc.select("table.updates");

        Elements links = updates.select("a[href][class=chaptersrec]");

        Pattern p = Pattern.compile(".+\\/([\\w-]+)\\/(\\d+)");
        ArrayList<String> checked = new ArrayList<String>(); // to avoid duplicates

        for (final Element link : links) {
            String data = link.attr("abs:href");
//            print(" * a: <%s>  (%s)", data, trim(link.text(), 20));
            Matcher m = p.matcher(data);
            if(m.matches()) {
                final String name = m.group(1);
                final int chapter = Integer.valueOf(m.group(2));
                if (!checked.contains(name)) {
                    checked.add(name);
                    // check Parse for each manga
                    ParseQuery<Manga> query = ParseQuery.getQuery(Manga.class);
                    query.whereEqualTo("name", name);
                    query.orderByDescending("latestChapter"); // in case of duplicates, should not be necessary
                    query.getFirstInBackground(new GetCallback<Manga>() {
                        @Override
                        public void done(Manga manga, ParseException e) {
                            if (manga != null) {
                                // if manga exists, check chapter
                                if (manga.getMangaChapter() < chapter) {
                                    Log.i(TAG, "New chapter available! Updating database...");
                                    manga.setMangaChapter(chapter);
                                    manga.saveInBackground();

                                    // Use identifier in the JSONArray, as it will be passed to check in shared preferences
                                    updatedManga.add(manga.getReadableName());
                                }
                                // TODO - else count ++, if count > 5, stop wasting data ??

                            } else {
                                // else add new manga onto parse
                                //update manga with the readable name here as well
                                Pattern p2 = Pattern.compile("(.+)\\s\\d+");
                                Matcher matcher = p2.matcher(link.text());
                                String readableName = "";

                                if (matcher.matches()) {
                                    readableName = matcher.group(1);
                                }
                               createMangaInDatabase(name, chapter, readableName);

                            }
                        }
                    });
                }
            }else {
                Log.i(TAG, "Regex Problem");
            }
        }

//
        try {
            // This is to make sure the main thread here receives the updates from the worker thread,
            // before sending the proper push notifications

            // Reasoning behind this is because I can't get a handle to the querying thread, and cannot properly wait for them to end
            Thread.sleep(10000l);
            // End of for loop, send push notification for all the updated objects
            if(updatedManga.size() > 0) {
                Log.i(TAG, "Updated Manga: " + updatedManga.toString());
                sendPushNotification(updatedManga);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    private void createMangaInDatabase(final String name, final int chapter, final String readableName) {
        Log.i(TAG, "Manga does not exist in database. Updating database... ");
        Manga newManga = new Manga();
        newManga.setMangaName(name);
        newManga.setMangaChapter(chapter);
        newManga.setReadableName(readableName);
        newManga.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "New manga saved.");
                    print("ID: %s | Chapter: %s | Name: %s", name, chapter, readableName);
                } else {
                    Log.i(TAG, "Error in saving: " + e.getCode());
                }
            }
        });
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
    private static void print(String s, Object... args) {
        Log.i(TAG, String.format(s, args));
    }
}
