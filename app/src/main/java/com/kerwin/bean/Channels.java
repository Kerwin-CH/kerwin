package com.kerwin.bean;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Kerwin on 2017/3/3.
 */
public class Channels extends RealmObject implements Serializable {

    private String url;
    private String name;
    private String id;
    private boolean collecton;

    public boolean isCollecton() {
        return collecton;
    }

    public void setCollecton(boolean collecton) {
        this.collecton = collecton;
    }


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

    public void removeFromRealm() {

    }
}
