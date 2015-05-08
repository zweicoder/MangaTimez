package com.pewpewpew.user.mangatimez;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.codec.binary.StringUtils;

import java.util.List;

/**
 * Created by User on 01/3/15.
 */

/**
 * Activity to add new item to follow (using SharedPreferences).
 * Possible implementations:
 *  1) Retrieves a list of all the manga in the database (?)
 *  2) Update a file with the manga names, then use the file as the dictionary for auto correct/ google search bar
 */
public class AddItemActivity extends ActionBarActivity {
    private static final String FOLLOWED_MANGAS = "followed_mangas_shared_preferences";
    private static final String TAG = "AddItemActivity_debug";
    ParseQueryAdapter mAdapter;
    private String searchQuery = "";
    private int oldCount = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add a SearchView
        getMenuInflater().inflate(R.menu.add_item, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem item = menu.findItem(R.id.searchView);
        // App compat
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

//        SearchView searchView =
//                (SearchView) menu.findItem(R.id.searchView).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        super.onNewIntent(intent);
        if(intent.getAction().equals(Intent.ACTION_SEARCH)){
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Search Query: " + searchQuery);

//            mAdapter.notifyDataSetChanged();
            oldCount = 0;
            mAdapter.loadObjects();
        };


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        final ListView listView = (ListView) findViewById(R.id.listView);
        ParseQueryAdapter.QueryFactory<Manga> factory = new ParseQueryAdapter.QueryFactory<Manga>() {
            @Override
            public ParseQuery<Manga> create() {
                Log.i(TAG, "Creating Parse Factory");
                ParseQuery<Manga> query = ParseQuery.getQuery(Manga.class);
                // handle new search query, handle cancel search as well
                query.whereContains("readableName",searchQuery);
                query.orderByAscending("readableName");

                return query;
            }
        };
        mAdapter = new ParseQueryAdapter<Manga>(this, factory){
            @Override
            public View getItemView(Manga manga, View v, ViewGroup parent) {
                if (v == null){
                    v= getLayoutInflater().inflate(R.layout.list_item_add, parent, false);
//                    v= View.inflate(getContext(),R.layout.list_item_add, null);
                }

                String title = manga.getReadableName();
                ((TextView) v.findViewById(R.id.title)).setText(title);
                return v;
            }
        };
        mAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener() {
            @Override
            public void onLoading() {
                if (!searchQuery.equals("")) {
//                    Toast.makeText(AddItemActivity.this, "Searching...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoaded(List list, Exception e) {
                if (mAdapter.isEmpty()) {
                    Toast.makeText(AddItemActivity.this, "No results found.", Toast.LENGTH_SHORT).show();
                }

            }
        });
//        mAdapter.setAutoload(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // add to shared preferences and then return
                SharedPreferences preferences = getSharedPreferences(FOLLOWED_MANGAS, 0);

                // Using mangaName as identifier cause it's supposed to be more unique, and we have that field anyway so might as well
                // SCREW THAT WE NEED READABLE NAME FOR PRETTY NOTIFICATIONS
                String identifier = ((Manga) mAdapter.getItem(position)).getReadableName();
                boolean isFollowed = preferences.getBoolean(identifier, false);
                if (!isFollowed) {
                    Log.i(TAG, "Following " + ((Manga) mAdapter.getItem(position)).getReadableName() + ".");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(identifier, true);
                    editor.commit();
                    setResult(RESULT_OK);
                    AddItemActivity.this.finish();
                } else {
                    Log.i(TAG, "Already followed");
                    Toast.makeText(AddItemActivity.this, identifier + " already followed.", Toast.LENGTH_SHORT).show();

                }

            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount >= totalItemCount - 5 && totalItemCount > oldCount) {
                    if (visibleItemCount != 0) {
                        oldCount = totalItemCount;
                        mAdapter.loadNextPage();
                    }
                }
            }
        });

        listView.setAdapter(mAdapter);


    }
}
