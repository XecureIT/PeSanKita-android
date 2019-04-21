package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class KPDaerah extends KPStatus {
    @SerializedName("data")
    @Expose
    private List<ItemDaerah> data = null;

    public List<ItemDaerah> getData() {
        return data;
    }

    public void setData(List<ItemDaerah> data) {
        this.data = data;
    }

    public static class ItemDaerah {
        private int _id;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("nama")
        @Expose
        private String nama;
        @SerializedName("jmltps")
        @Expose
        private Integer jmltps;
        private String parent;

        public int get_id() {
            return _id;
        }

        public void set_id(int _id) {
            this._id = _id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public Integer getJmltps() {
            return jmltps;
        }

        public void setJmltps(Integer jmltps) {
            this.jmltps = jmltps;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }
    }
}
