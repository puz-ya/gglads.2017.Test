package com.py.producthuntreader.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.py.producthuntreader.MainActivity;
import com.py.producthuntreader.api.ApiPostDeserializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Puzino Yury on 08.03.2017
 */

/** Singletone with post list */
public class PostLab {
    private static PostLab mPostLab;
    private List<Post> mPosts;

    public static PostLab get(String string){
        if(mPostLab == null){
            mPostLab = new PostLab(string);
        }
        return mPostLab;
    }

    /** closed constructor */
    private PostLab(String string){
        mPosts = new ArrayList<>();
        //
    }

    /** get all posts */
    public List<Post> getPosts(){
        return mPosts;
    }

    /** get one Post from list */
    public Post getPost(String name){
        for(Post post : mPosts){
            if(post.getName().equals(name)){
                return post;
            }
        }
        return null;
    }
}
