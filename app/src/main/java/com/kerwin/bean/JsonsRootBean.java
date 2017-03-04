package com.kerwin.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kerwin on 2017/3/4.
 */

public class JsonsRootBean implements Serializable {
    private List<Channels> channels;

    public void setChannels(List<Channels> channels) {
        this.channels = channels;
    }

    public List<Channels> getChannels() {
        return channels;
    }
}
