package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KPLaporan {

    private String provinsi;
    private String kabupaten;
    private String kecamatan;
    @SerializedName("idkel")
    @Expose
    private String kelurahan;
    @SerializedName("notps")
    @Expose
    private String tps;
    @SerializedName("count1")
    @Expose
    private int count1;
    @SerializedName("count2")
    @Expose
    private int count2;
    @SerializedName("s1")
    @Expose
    private int s1;
    @SerializedName("n1")
    @Expose
    private int n1;
    @SerializedName("attachmentId")
    @Expose
    private String attachmentId;
    @SerializedName("c1")
    @Expose
    private String c1;
    private String attachmentIdBase64;
    private String created_at;
    private String updated_at;
    private int type;

    public KPLaporan() {

    }

    public KPLaporan(String attachmentId, int count1, int count2, int s1, int n1, String idkel, String notps) {
        this.attachmentId = attachmentId;
        this.count1 = count1;
        this.count2 = count2;
        this.s1 = s1;
        this.n1 = n1;
        this.kelurahan = idkel;
        this.tps = notps;
    }

    public String getProvinsi() {
        return provinsi;
    }

    public void setProvinsi(String provinsi) {
        this.provinsi = provinsi;
    }

    public String getKabupaten() {
        return kabupaten;
    }

    public void setKabupaten(String kabupaten) {
        this.kabupaten = kabupaten;
    }

    public String getKecamatan() {
        return kecamatan;
    }

    public void setKecamatan(String kecamatan) {
        this.kecamatan = kecamatan;
    }

    public String getKelurahan() {
        return kelurahan;
    }

    public void setKelurahan(String kelurahan) {
        this.kelurahan = kelurahan;
    }

    public String getTps() {
        return tps;
    }

    public void setTps(String tps) {
        this.tps = tps;
    }

    public int getCount1() {
        return count1;
    }

    public void setCount1(int count1) {
        this.count1 = count1;
    }

    public int getCount2() {
        return count2;
    }

    public void setCount2(int count2) {
        this.count2 = count2;
    }

    public int getS1() {
        return s1;
    }

    public void setS1(int s1) {
        this.s1 = s1;
    }

    public int getN1() {
        return n1;
    }

    public void setN1(int n1) {
        this.n1 = n1;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public String getAttachmentIdBase64() {
        return attachmentIdBase64;
    }

    public void setAttachmentIdBase64(String attachmentIdBase64) {
        this.attachmentIdBase64 = attachmentIdBase64;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
