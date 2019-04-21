package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KPC1Url extends KPStatus {

    @SerializedName("url")
    @Expose
    private String url;

    KPC1Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
