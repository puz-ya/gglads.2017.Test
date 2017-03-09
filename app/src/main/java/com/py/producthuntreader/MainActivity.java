package com.py.producthuntreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    PostFragment.OnListFragmentInteractionListener {

    public static final String TOKEN = "";
    private static final int INITIAL_REQUEST_4INTERNET = 10101;
    private static final String FRAGMENT_POSTS_TAG = "POSTS_TAG";
    private static final String EXTRA_CATEG_NAME = "CATEG_NAME";

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private ProgressBar mSpinner;

    private OkHttpClient mClient = new OkHttpClient();

    private Category[] mCategories = null;
    public Post[] mPosts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mSpinner = (ProgressBar) findViewById(R.id.progressBarMain);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
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

        //get categories and posts
        getSiteCategories();

        String savedCategoryName = "";
        if (savedInstanceState != null) {
            savedCategoryName = savedInstanceState.getString(EXTRA_CATEG_NAME);
        }
        getSitePosts(savedCategoryName);
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
        } else if (id == R.id.action_close) {
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
        mToolbar.setTitle(title);
        //update all
        getSitePosts(title);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /** The user selected post from the Fragment.
     * @param post - start new Detail activity with post data
     * */
    public void onListFragmentInteraction(Post post) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_DETAIL, post.getId());
        startActivity(intent);
    }

    /** user made a swipe - update Posts[].
     * */
    public void onListSwipeUpdate() {
        getUpdatePosts(mToolbar.getTitle().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST_4INTERNET: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the task you need to do.
                    Toast.makeText(this, getString(R.string.internet_permission_granted), Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied! Disable the functionality that depends on this.
                    Toast.makeText(this, getString(R.string.internet_permission_not_granted), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // other 'case' lines to check for other permissions
        }
    }

    /** Set url and headers.
     * */
    public Request createCategoriesRequest() {

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

    /** Get list of categories from site and insert into Drawer.
     * */
    public void getSiteCategories() {

        //checking permissions (API 21+)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {Manifest.permission.INTERNET},
                    INITIAL_REQUEST_4INTERNET
            );

            Toast.makeText(this, getString(R.string.internet_enable_access), Toast.LENGTH_SHORT).show();
        } else {

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
                        }
                    });
                }
            });
        }
    }

    /** Updating Drawer elements and adapter.
     * */
    public void updateDrawer() {

        if (mCategories.length <= 0) {
            return;
        }

        //set toolbar name
        mToolbar.setTitle(mCategories[0].getName());

        //set drawer menu
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
        //hide spinner
        mSpinner.setVisibility(View.GONE);
    }

    /** Request for List of Post.
     * @param categoryName - name of the selected category (Name inside Toolbar title)
     * @return Request
     * */
    public Request createPostListRequest(String categoryName) {

        //HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.producthunt.com/v1/posts/all").newBuilder();
        //urlBuilder.addQueryParameter("search[category]", categoryName);

        String urlAPI = "https://api.producthunt.com/v1/categories/" + categoryName.toLowerCase() + "/posts";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlAPI).newBuilder();
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

    /** get all topics in default [0] category (usually tech).
     * @param categoryName - name of the category to retrieve posts from
     * */
    public void getSitePosts(String categoryName) {

        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "tech";
        }

        //checking permissions (API 21+)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {Manifest.permission.INTERNET},
                    INITIAL_REQUEST_4INTERNET
            );

            Toast.makeText(this, getString(R.string.internet_enable_access), Toast.LENGTH_SHORT).show();
        } else {

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

                    // Run view-related code back on the main thread
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMainFrame();
                        }
                    });
                }
            });
        }
    }

    public void updateMainFrame() {

        String title = mToolbar.getTitle().toString();

        Bundle bundle = new Bundle();
        bundle.putString("cat_name", title);

        PostFragment fragment = new PostFragment();
        fragment.setArguments(bundle);
        fragment.setPosts(mPosts);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, FRAGMENT_POSTS_TAG);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        mSpinner.setVisibility(View.GONE);
    }

    /** Update fragment content (bad way).
     * @param categoryName - name of the category to retrieve posts from
     * */
    public void getUpdatePosts(String categoryName) {

        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "tech";
        }

        //checking permissions (API 21+)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[] {Manifest.permission.INTERNET},
                    INITIAL_REQUEST_4INTERNET
            );

            Toast.makeText(this, getString(R.string.internet_enable_access), Toast.LENGTH_SHORT).show();
        } else {

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

                    // Run view-related code back on the main thread
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PostFragment fragment = (PostFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_POSTS_TAG);
                            fragment.setPosts(mPosts);
                            fragment.updateUI();
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_CATEG_NAME, mToolbar.getTitle().toString());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
