package com.kerwin.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * Created by Kerwin on 2017/3/3.
 */
@Entity
public class Channels implements Serializable {
    @Id
    private Long id;
    private String url;
    private String name;
    private boolean collection;
    public boolean getCollection() {
        return this.collection;
    }
    public void setCollection(boolean collection) {
        this.collection = collection;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1366561809)
    public Channels(Long id, String url, String name, boolean collection) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.collection = collection;
    }
    @Generated(hash = 62789264)
    public Channels() {
    }
  
}
