package com.py.producthuntreader;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.py.producthuntreader.api.ApiPostDeserializer;
import com.py.producthuntreader.model.Category;
import com.py.producthuntreader.model.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    PostFragment.OnListFragmentInteractionListener{

    private String TOKEN = "";

    private static final String LOG_TAG = "MainActivity: ";
    Toolbar mToolbar;
    DrawerLayout mDrawer;
    NavigationView mNavigationView;
    private ProgressBar mSpinner;

    OkHttpClient mClient = new OkHttpClient();

    Category[] mCategories = null;
    public Post[] mPosts = null;

    private boolean mFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mSpinner = (ProgressBar)findViewById(R.id.progressBarMain);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.START);
            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        getSiteCategories();
        getSitePosts("", true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // will start new SettingsActivity for Service stop\start
            return true;
        } else if (id == R.id.action_close){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mSpinner.setVisibility(View.VISIBLE);

        String title = item.getTitle().toString();
        getSitePosts(title, true);
        mToolbar.setTitle(title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onListFragmentInteraction(Post post) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
        Intent intent = new Intent();
    }

    /** Get list of categories from site and insert into Drawer
     * */
    public void getSiteCategories(){

        mSpinner.setVisibility(View.VISIBLE);
        Request request = createCategoriesRequest();

        // Get a handler that can be used to post to the main thread
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Read data on the worker thread
                final String responseData = response.body().string();
                //Log.d(LOG_TAG, responseData);

                // Parsing JSON answer
                Gson gson = new GsonBuilder().create();

                JsonParser jsonParser = new JsonParser();
                JsonArray jsonCategories = jsonParser.parse(responseData)
                        .getAsJsonObject().getAsJsonArray("categories");

                mCategories = gson.fromJson(jsonCategories, Category[].class);

                // Run view-related code back on the main thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDrawer();
                        mSpinner.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    /** Set url and headers
     * */
    public Request createCategoriesRequest(){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.producthunt.com/v1/categories").newBuilder();
        urlBuilder.addQueryParameter("access_token", TOKEN);
        String url = urlBuilder.build().toString();

        // Add headers
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "bearer " + TOKEN)
                .addHeader("Host", "api.producthunt.com")
                .url(url)
                .build();

        return request;
    }

    /** Updating Drawer elements and adapter
     * */
    public void updateDrawer(){

        if(mCategories.length <= 0){
            return;
        }

        Menu menu = mNavigationView.getMenu();
        menu.clear();
        for (Category category : mCategories) {
            menu.add(category.toString());
        }

        for (int i = 0, count = mNavigationView.getChildCount(); i < count; i++) {
            final View child = mNavigationView.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }

    /** get all topics in default [0] category (usually tech)
     * @param categoryName - name of the category to retrieve posts from
     * @param isUpdate - true if we want to update FrameLayout too, false - update Post Array only
     * */
    public void getSitePosts(String categoryName, final boolean isUpdate){

        mFlag = false;

        if(categoryName == null || categoryName.isEmpty()){
            categoryName = "tech";
        }

        mSpinner.setVisibility(View.VISIBLE);
        Request request = createPostListRequest(categoryName.toLowerCase());

        // Get a handler that can be used to post to the main thread
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // Read data on the worker thread (only 50 posts)
                final String responseData = response.body().string();
                //Log.d(LOG_TAG, responseData);

                // Parsing JSON answer
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Post.class, new ApiPostDeserializer())
                        .create();

                JsonParser jsonParser = new JsonParser();
                JsonArray jsonCategories = jsonParser.parse(responseData)
                        .getAsJsonObject().getAsJsonArray("posts");

                mPosts = gson.fromJson(jsonCategories, Post[].class);
                mFlag = true;

                // Run view-related code back on the main thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isUpdate){
                            updateMainFrame();
                        }
                        mSpinner.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    public Request createPostListRequest(String categoryName){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.producthunt.com/v1/posts/all").newBuilder();
        urlBuilder.addQueryParameter("search[category]", categoryName);
        urlBuilder.addQueryParameter("access_token", TOKEN);
        String url = urlBuilder.build().toString();

        // Add headers
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "bearer " + TOKEN)
                .addHeader("Host", "api.producthunt.com")
                .url(url)
                .build();

        return request;
    }

    public void updateMainFrame(){

        String title = "tech";
        if(mCategories != null && mCategories.length > 0){
            title = mCategories[0].getName();
        }

        Bundle bundle = new Bundle();
        bundle.putString("cat_name",title);

        PostFragment fragment = new PostFragment();
        fragment.setArguments(bundle);
        fragment.setPosts(mPosts);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }
}
