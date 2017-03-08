package com.py.producthuntreader.model;

/**
 * Created by Puzino Yury on 08.03.2017.
 */

public class Post {

    private Integer id;
    private String name;
    private String tagline;
    private Integer votes_count;
    private String thumbnail;

    public Post(Integer id, String name, String tagline, Integer votes_count, String thumbnail){
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.votes_count = votes_count;
        this.thumbnail = thumbnail;
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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
