package com.py.producthuntreader.model;

import java.util.Locale;

/**
 * Created by Puzino Yury on 07.03.2017.
 */

public class Category {

    /** From API
     * "id" : 1,
     * "slug" : "tech",
     * "name" : "Tech",
     * "color" : "#da552f",
     * "item_name" : "product"
     * */

    private Long id;
    private String slug;
    private String name;
    private String color;
    private String item_name;

    public Category(Long id, String slug, String name, String color, String item_name){
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.color = color;
        this.item_name = item_name;
    }

    @Override
    public String toString() {
        /*
        return String.format(Locale.getDefault(),
                "[Category: id=%1$d, slug=%2$s, name=%3$s, color=%4$s, item_name=%5$s]",
                id, slug, name, color, item_name);
                */
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }
}
