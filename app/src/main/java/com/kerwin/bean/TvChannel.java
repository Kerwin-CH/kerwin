package com.kerwin.bean;

/**
 * Created by Kerwin on 2017/3/3.
 */

public class TvChannel {
    private String channelName;
    private String channelIcoUrl;
    private String channelUrl;
    private String channelNumber;

    public TvChannel(String channelName, String channelUrl) {
        this.channelName = channelName;
        this.channelUrl = channelUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelIcoUrl() {
        return channelIcoUrl;
    }

    public void setChannelIcoUrl(String channelIcoUrl) {
        this.channelIcoUrl = channelIcoUrl;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

    public String getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(String channelNumber) {
        this.channelNumber = channelNumber;
    }

}
