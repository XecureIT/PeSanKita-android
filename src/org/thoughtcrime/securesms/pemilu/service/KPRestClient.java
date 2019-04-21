package org.thoughtcrime.securesms.pemilu.service;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.pemilu.KPHelper;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rendra on 17/03/19.
 */

public class KPRestClient {
    private static Context context;
    private static final String TAG = KPRestClient.class.getSimpleName();
    private static final Interceptor REWRITE_RESPONSE_INTERCEPTOR = chain -> {
        Response originalResponse = chain.proceed(chain.request());
        String cacheControl = originalResponse.header("Cache-Control");

        if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") ||
                cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + 10)
                    .build();
        } else {
            return originalResponse;
        }
    };
    private static final Interceptor OFFLINE_INTERCEPTOR = chain -> {
        Request request = chain.request();

        if (!KPHelper.isNetworkAvailable(KPRestClient.context)) {
            Log.d(TAG, "rewriting request");

            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }

        return chain.proceed(request);
    };

    public static Retrofit getClient(final Context context) {
        KPRestClient.context = context;
        File httpCacheDirectory = new File(context.getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient.Builder client = new OkHttpClient.Builder();

        if (KPHelper.isLoggedIn(context)) {
            client.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request.Builder builder = chain.request().newBuilder();
                    builder.addHeader("Authorization", Credentials.basic(
                            TextSecurePreferences.kpGetMsisdn(context),
                            TextSecurePreferences.kpGetPassword(context)));

                    return chain.proceed(builder.build());
                }
            });
        }

        client.addNetworkInterceptor(REWRITE_RESPONSE_INTERCEPTOR)
                .addInterceptor(OFFLINE_INTERCEPTOR)
                .cache(cache);

//        CertificatePinner certificatePinner = new CertificatePinner.Builder()
//                .add(BuildConfig.KP_HOSTNAME, "sha256/UGLmW4TpNNydr5ibTab3/rIsYoeMh5fhNNHZcoidk/Q=")
//                .add(BuildConfig.KP_HOSTNAME, "sha256/MlmjTjuCKsSkmAeBedx+MXDrgAzlOTNfJ048dXc0vCw=")
//                .build();
//
//        client.certificatePinner(certificatePinner);

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.KP_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();
    }
}
