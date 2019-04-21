package org.thoughtcrime.securesms.pemilu.service;

import org.thoughtcrime.securesms.pemilu.model.KPC1Url;
import org.thoughtcrime.securesms.pemilu.model.KPDaerah;
import org.thoughtcrime.securesms.pemilu.model.KPLaporan;
import org.thoughtcrime.securesms.pemilu.model.KPListLaporan;
import org.thoughtcrime.securesms.pemilu.model.KPLoginStatus;
import org.thoughtcrime.securesms.pemilu.model.KPRegistrationBody;
import org.thoughtcrime.securesms.pemilu.model.KPStatus;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by rendra on 17/03/19.
 */

public interface KPRestInterface {

    @Headers("Content-Type: application/json")
    @POST("/v1/login")
    Call<KPLoginStatus> userLogin(@Header("Authorization") String auth);

    @Headers("Content-Type: application/json")
    @POST("/v1/registration/")
    Call<KPStatus> userRegistration(@Body KPRegistrationBody registration);

    @Headers("Content-Type: application/json")
    @POST("/v1/submission/")
    Call<KPStatus> postLaporan(@Body KPLaporan laporan);

    @Headers("Content-Type: application/json")
    @GET("/v1/submission/")
    Call<KPListLaporan> getLaporan();

    @Headers("Content-Type: application/json")
    @GET("/v1/{type}/")
    Call<KPDaerah> getDaerah(@Path("type") String type, @Query(value = "p") String p);

    @Headers("Content-Type: application/json")
    @GET("/v1/submission/c1/url")
    Call<KPC1Url> getAttachmentUrl(@Query(value = "attachmentId") String attachmentId, @Query(value = "ctype") String ctype);
}
