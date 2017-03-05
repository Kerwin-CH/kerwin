package com.kerwin.bean;

import java.util.ArrayList;

/**
 * Created by Kerwin on 2017/3/4.
 */

public class JsonsRootBean {
    private ArrayList<Channels> channels;
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public void setChannels(ArrayList<Channels> channels) {
        this.channels = channels;
    }

    public ArrayList<Channels> getChannels() {
        return channels;
    }
}
