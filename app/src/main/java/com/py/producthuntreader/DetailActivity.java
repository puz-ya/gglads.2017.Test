package com.py.producthuntreader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.py.producthuntreader.api.ApiPostDetailsDeserializer;
import com.py.producthuntreader.model.PostDetails;
import com.py.producthuntreader.model.VolleySingleton;
import com.py.producthuntreader.model.CustomWebClient;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    OkHttpClient mClient = new OkHttpClient();

    private Integer mId = 1;
    public static final String EXTRA_DETAIL = "POST_ID";
    public static final String SAVE_POST = "POST_CLASS";
    PostDetails mPostDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);
        //mToolbar.setTitle(R.string.activity_toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(getString(R.string.activity_toolbar));
        }

        Intent intent = getIntent();
        if(intent != null) {
            mId = intent.getIntExtra(EXTRA_DETAIL, 1);
        }

        if(savedInstanceState != null){
            mId = savedInstanceState.getInt(EXTRA_DETAIL);
            mPostDetails = (PostDetails) savedInstanceState.getSerializable(SAVE_POST);
            updateDetailView();
        }else {
            getDetails();
        }
    }

    /** Set url and headers
     * */
    public Request createDetailsRequest(){

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.producthunt.com/v1/posts/" + mId).newBuilder();
        urlBuilder.addQueryParameter("access_token", MainActivity.TOKEN);
        String url = urlBuilder.build().toString();

        // Add headers
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "bearer " + MainActivity.TOKEN)
                .addHeader("Host", "api.producthunt.com")
                .url(url)
                .build();

        return request;
    }

    public void getDetails(){

        mProgressDialog = ProgressDialog.show(DetailActivity.this, "", "Getting Post data...");
        Request request = createDetailsRequest();

        // Get a handler that can be used to post to the main thread
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DetailActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
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
                //Log.d("Details", responseData);

                // Parsing JSON answer
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(PostDetails.class, new ApiPostDetailsDeserializer())
                        .create();

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonDetails = jsonParser.parse(responseData)
                        .getAsJsonObject().getAsJsonObject("post");

                mPostDetails = gson.fromJson(jsonDetails, PostDetails.class);

                // Run view-related code back on the main thread
                DetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDetailView();
                        mProgressDialog.hide();
                    }
                });
            }
        });
    }

    public void updateDetailView(){

        if(mPostDetails == null){
            TextView tvTitle = (TextView) findViewById(R.id.detail_title);
            tvTitle.setText(getString(R.string.detail_error));
            return;
        }

        String title = getString(R.string.detail_title) + " " + mPostDetails.getName();
        TextView tvTitle = (TextView) findViewById(R.id.detail_title);
        tvTitle.setText(title);

        String tagline = getString(R.string.detail_tagline) + " " + mPostDetails.getTagline();
        TextView tvTag = (TextView) findViewById(R.id.detail_tagline);
        tvTag.setText(tagline);

        String votes = getString(R.string.detail_votes) + " " + mPostDetails.getVotes_count();
        TextView tvVotes = (TextView) findViewById(R.id.detail_votes);
        tvVotes.setText(votes);

        NetworkImageView networkImageView = (NetworkImageView) findViewById(R.id.detail_image);
        networkImageView.setDefaultImageResId(R.drawable.image_view_small_blank);
        networkImageView.setErrorImageResId(R.drawable.image_view_small_error);
        networkImageView.setImageUrl(mPostDetails.getScreenshot_url(), VolleySingleton.getInstance().getImageLoader());

        Button button = (Button) findViewById(R.id.detail_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = ProgressDialog.show(DetailActivity.this, "", "Getting html code...");

                WebView webView = (WebView) findViewById(R.id.detail_webview);
                webView.setWebViewClient(new CustomWebClient());
                // load a web page */
                webView.loadUrl(mPostDetails.getRedirect_url());
                mProgressDialog.hide();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(EXTRA_DETAIL, mId);
        savedInstanceState.putSerializable(SAVE_POST, mPostDetails);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
