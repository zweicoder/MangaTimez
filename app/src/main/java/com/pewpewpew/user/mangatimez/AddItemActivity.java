package com.pewpewpew.user.mangatimez;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);
        final ListView listView = (ListView) findViewById(R.id.listView);
        ParseQueryAdapter.QueryFactory<Manga> factory = new ParseQueryAdapter.QueryFactory<Manga>() {
            @Override
            public ParseQuery<Manga> create() {
                ParseQuery<Manga> query = ParseQuery.getQuery(Manga.class);

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
                if(!isFollowed){
                    Log.i(TAG, "Following " + ((Manga) mAdapter.getItem(position)).getReadableName() + ".");
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(identifier, true);
                    editor.commit();
                    setResult(RESULT_OK);
                    AddItemActivity.this.finish();
                }else{
                    Log.i(TAG, "Already followed");
                    Toast.makeText(AddItemActivity.this,identifier+" already followed.",Toast.LENGTH_SHORT).show();

                }

            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                Log.i(TAG, "1 "+firstVisibleItem);
//                Log.i(TAG, "2 "+visibleItemCount);
//                Log.i(TAG, "3 "+totalItemCount);
                if(firstVisibleItem + visibleItemCount >= totalItemCount - 5){
                    if(visibleItemCount != 0) {
                        mAdapter.loadNextPage();
                    }
                }
            }
        });

        listView.setAdapter(mAdapter);

    }
}
