package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KPListLaporan extends KPStatus {
    @SerializedName("data")
    @Expose
    private List<KPLaporan> data = null;

    public List<KPLaporan> getData() {
        return data;
    }

    public void setData(List<KPLaporan> data) {
        this.data = data;
    }
}