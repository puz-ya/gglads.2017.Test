package com.py.producthuntreader.model;

/**
 * Created by Puzino Yury on 08.03.2017.
 */

public class PostDetails implements java.io.Serializable {

    private Integer id;
    private String name;
    private String tagline;
    private Integer votes_count;
    private String screenshot_url;
    private String redirect_url;

    public PostDetails(Integer id, String name, String tagline, Integer votes_count, String screenshot, String redirect_url){
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.votes_count = votes_count;
        this.screenshot_url = screenshot;
        this.redirect_url = redirect_url;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public Integer getVotes_count() {
        return votes_count;
    }

    public void setVotes_count(Integer votes_count) {
        this.votes_count = votes_count;
    }

    public String getScreenshot_url() {
        return screenshot_url;
    }

    public void setScreenshot_url(String screenshot_url) {
        this.screenshot_url = screenshot_url;
    }

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }
}
