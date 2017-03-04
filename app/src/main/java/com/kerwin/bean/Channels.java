package com.kerwin.bean;

import java.io.Serializable;

/**
 * Created by Kerwin on 2017/3/3.
 */

public class Channels implements Serializable {

    private String url;
    private String name;

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
