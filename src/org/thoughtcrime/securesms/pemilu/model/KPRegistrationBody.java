package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by rendra on 17/03/19.
 */

public class KPRegistrationBody {

    @SerializedName("msisdn")
    @Expose
    private String msisdn;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("nik")
    @Expose
    private String nik;
    @SerializedName("referral")
    @Expose
    private String referral;
    @SerializedName("password")
    @Expose
    private String password;

    public KPRegistrationBody(String msisdn, String name, String email, String nik, String referral, String password) {
        this.msisdn = msisdn;
        this.name = name;
        this.email = email;
        this.nik = nik;
        this.referral = referral;
        this.password = password;
    }

    public KPRegistrationBody(String msisdn, String name, String referral, String password) {
        this.msisdn = msisdn;
        this.name = name;
        this.referral = referral;
        this.password = password;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = referral;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
