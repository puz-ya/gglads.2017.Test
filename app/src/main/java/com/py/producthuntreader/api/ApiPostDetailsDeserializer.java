package com.py.producthuntreader.api;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.py.producthuntreader.model.PostDetails;

import java.lang.reflect.Type;

/**
 * Created by Puzino Yury on 08.03.2017.
 */

public class ApiPostDetailsDeserializer implements JsonDeserializer<PostDetails> {

    @Nullable
    public PostDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        PostDetails post = null;

        if (json.isJsonObject()) {

            Gson gson = new GsonBuilder().create();
            JsonObject array = json.getAsJsonObject();

            if (array.entrySet().size() > 0) {
                Integer id = gson.fromJson(array.get("id"), Integer.class);
                String name = gson.fromJson(array.get("name"), String.class);
                String tagline = gson.fromJson(array.get("tagline"), String.class);
                Integer votes = gson.fromJson(array.get("votes_count"), Integer.class);
                String screenshot_url = gson.fromJson(array.getAsJsonObject("screenshot_url").get("300px"), String.class);
                String redirect_url = gson.fromJson(array.get("redirect_url"), String.class);
                post = new PostDetails(id, name, tagline, votes, screenshot_url, redirect_url);
            }
        }

        return post;
    }

}
