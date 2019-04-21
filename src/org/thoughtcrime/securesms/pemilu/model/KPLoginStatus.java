package org.thoughtcrime.securesms.pemilu.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by rendra on 17/03/19.
 */

public class KPLoginStatus extends KPStatus {

    @SerializedName("profile")
    @Expose
    private KPProfile profile;

    public KPProfile getProfile() {
        return profile;
    }

    public void setProfile(KPProfile profile) {
        this.profile = profile;
    }
}
