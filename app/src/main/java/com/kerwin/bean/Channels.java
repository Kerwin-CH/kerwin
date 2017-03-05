package com.kerwin.bean;

import io.realm.RealmObject;

/**
 * Created by Kerwin on 2017/3/3.
 */

public class Channels extends RealmObject {

    private String url;
    private String name;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
