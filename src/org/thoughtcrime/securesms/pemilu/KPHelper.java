package org.thoughtcrime.securesms.pemilu;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.spongycastle.crypto.DerivationFunction;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.MGF1BytesGenerator;
import org.spongycastle.crypto.params.MGFParameters;
import org.spongycastle.util.encoders.Hex;
import org.thoughtcrime.securesms.BuildConfig;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rendra on 17/03/19.
 */

public class KPHelper {

    private static final String TAG = KPHelper.class.getSimpleName();

    private static final String SIMULASI_END = "17/04/2019 12:00:00 +0700";
    private static final String PELAPORAN_START = "17/04/2019 13:00:00 +0700";

    public static boolean hasCredentials(final Context context) {
        return TextSecurePreferences.kpGetMsisdn(context) != null &&
                TextSecurePreferences.kpGetPassword(context) != null;
    }

    public static void clearCredentials(final Context context) {
        TextSecurePreferences.kpSetMsisdn(context, null);
        TextSecurePreferences.kpSetPassword(context, null);
        TextSecurePreferences.kpSetLoggedIn(context, false);
    }

    public static boolean isFirstTime(final Context context) {
        return TextSecurePreferences.kpGetFirstTime(context);
    }

    public static boolean isLoggedIn(final Context context) {
        return hasCredentials(context) && TextSecurePreferences.kpIsLoggedIn(context);
    }

    /***
     *
     * @param seed input for generate pass
     * @param len number of char password to be generated
     * @return password in string format
     */
    public static String generatePassword(final String seed, final int len) {
        byte[] pass = new byte[len];
        DerivationFunction kdf = new MGF1BytesGenerator(new SHA256Digest());
        kdf.init(new MGFParameters(seed.getBytes()));
        kdf.generateBytes(pass, 0, pass.length);
        return new String(Hex.encode(pass)).substring(0, len);
    }

    public static String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return format.format(calendar.getTime());
    }

    public static String getAttachmentId(String tps, String idkel) {
        return "C1_" + tps + "_" + idkel + "_" + getTimestamp();
    }

    public static File createImageFile(final Context context) throws IOException {
        String imageFileName = "JPEG_" + getTimestamp() + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public static File createTempFile(final Context context) throws IOException {
        File file = File.createTempFile(getTimestamp(), "tmp", context.getCacheDir());
        file.deleteOnExit();

        return file;
    }

    public static File getImageCompressed(Context context, String srcPath) throws IOException {
        return KPImageHelper.getCompressed(context, srcPath, createTempFile(context).getAbsolutePath());
    }

    public static String getCType(String path) {
        String extension = path.substring(path.lastIndexOf("."));
        String mimeTypeMap = MimeTypeMap.getFileExtensionFromUrl(extension);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeTypeMap);
    }

    public static void darkenStatusBar(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(
                    darkenColor(
                            ContextCompat.getColor(activity, color)));
        }
    }

    private static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static void uploadAttachment(String method, URL url, String ctype, InputStream data, long dataSize) throws IOException {
        Log.d(TAG, "uploadAttachment: start");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        connection.setFixedLengthStreamingMode(dataSize);

        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", ctype);
        connection.setRequestProperty("Connection", "close");
        connection.connect();

        try {
            OutputStream out = connection.getOutputStream();
            byte[] buffer = new byte[4096];
            int read = 0;

            while ((read = data.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            data.close();
            out.flush();
            out.close();

            if (connection.getResponseCode() != 200) {
                throw new IOException("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

        } finally {
            connection.disconnect();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;

        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            Log.d(TAG, "Network Connected " + networkInfo.getTypeName());
        } else {
            Log.d(TAG, "Network Not Connected: ");
        }

        return isConnected;
    }

    public static boolean isSimulasiEnded() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z").parse(SIMULASI_END).before(new Date());
        } catch (ParseException e) {
        }
        return false;
    }

    public static long simulasiEndMillis() {
        try {
            long now = new Date().getTime();
            long end = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z").parse(SIMULASI_END).getTime();
            if (now < end) return end - now;
        } catch (ParseException e) {
        }
        return 0;
    }

    public static boolean isPelaporanStarted() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z").parse(PELAPORAN_START).before(new Date());
        } catch (ParseException e) {
        }
        return false;
    }

    public static long pelaporanStartMillis() {
        try {
            long now = new Date().getTime();
            long end = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z").parse(PELAPORAN_START).getTime();
            if (now < end) return end - now;
        } catch (ParseException e) {
        }
        return 0;
    }

    public static boolean isLaporanSimulasi(String pattern, String timestamp) {
        try {
            long ts = new SimpleDateFormat(pattern).parse(timestamp).getTime();
            long end = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z").parse(SIMULASI_END).getTime();
            return ts < end;
        } catch (Exception e) {
        }
        return true;
    }

    public static String getC1Url(String attachmentId) {
        return (isSimulasiEnded() ? BuildConfig.KP_C1_URL : BuildConfig.KP_C1_URL_DEV) + "/" + attachmentId;
    }

}
