package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class KPImageHelper {

    private static final String TAG = KPImageHelper.class.getSimpleName();

    private static final int MAX_COMPRESSION_QUALITY          = 100;
    private static final int MIN_COMPRESSION_QUALITY          = 30;
    private static final int MAX_COMPRESSION_ATTEMPTS         = 5;
    private static final int MIN_COMPRESSION_QUALITY_DECREASE = 5;
    private static final int KB                               = 1024;
    private static final int MB                               = 1024 * KB;
    private static final int MAX_IMAGE_WIDTH                  = 800;
    private static final int MAX_IMAGE_HEIGHT                 = 800;
    private static final int MAX_IMAGE_SIZE                   = 200 * KB;

    /***
     * compress the file/photo from @param <b>path</b> to a private location on the current device and return the compressed file.
     *
     * @param context
     * @param srcPath
     * @param destPath
     * @return
     * @throws IOException
     */
    public static File getCompressed(Context context, String srcPath, String destPath) throws IOException {

        int    quality  = MAX_COMPRESSION_QUALITY;
        int    attempts = 0;
        byte[] bytes;

        //decode and resize the original bitmap from @param path.
        Bitmap bitmap = decodeImageFromFiles(srcPath, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

        //create placeholder for the compressed image file
        File compressed = new File(destPath);

        //convert the decoded bitmap to stream
        ByteArrayOutputStream byteArrayOutputStream;

        try {
            do {
                byteArrayOutputStream = new ByteArrayOutputStream();
                /*compress bitmap into byteArrayOutputStream
                Bitmap.compress(Format, Quality, OutputStream)

                Where Quality ranges from 1 - 100.
                */
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();

                Log.w(TAG, "iteration with quality " + quality + " size " + (bytes.length / KB) + "kb");
                if (quality == MIN_COMPRESSION_QUALITY) break;

                int nextQuality = (int)Math.floor(quality * Math.sqrt((double) MAX_IMAGE_SIZE / bytes.length));
                if (quality - nextQuality < MIN_COMPRESSION_QUALITY_DECREASE) {
                    nextQuality = quality - MIN_COMPRESSION_QUALITY_DECREASE;
                }
                quality = Math.max(nextQuality, MIN_COMPRESSION_QUALITY);
            }
            while (bytes.length > MAX_IMAGE_SIZE && attempts++ < MAX_COMPRESSION_ATTEMPTS);
        } finally {
            if (bitmap != null) bitmap.recycle();
        }

        /*
        Right now, we have our bitmap inside byteArrayOutputStream Object, all we need next is to write it to the compressed file we created earlier,
        java.io.FileOutputStream can help us do just That!

         */
        FileOutputStream fileOutputStream = new FileOutputStream(compressed);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();

        fileOutputStream.close();

        ExifInterface srcexif = new ExifInterface(srcPath);
        ExifInterface destexif = new ExifInterface(destPath);

        int build = Build.VERSION.SDK_INT;

        // From API 11
        if (build >= 11) {
            if (srcexif.getAttribute("FNumber") != null) {
                destexif.setAttribute("FNumber",
                        srcexif.getAttribute("FNumber"));
            }
            if (srcexif.getAttribute("ExposureTime") != null) {
                destexif.setAttribute("ExposureTime",
                        srcexif.getAttribute("ExposureTime"));
            }
            if (srcexif.getAttribute("ISOSpeedRatings") != null) {
                destexif.setAttribute("ISOSpeedRatings",
                        srcexif.getAttribute("ISOSpeedRatings"));
            }
        }
        // From API 9
        if (build >= 9) {
            if (srcexif.getAttribute("GPSAltitude") != null) {
                destexif.setAttribute("GPSAltitude",
                        srcexif.getAttribute("GPSAltitude"));
            }
            if (srcexif.getAttribute("GPSAltitudeRef") != null) {
                destexif.setAttribute("GPSAltitudeRef",
                        srcexif.getAttribute("GPSAltitudeRef"));
            }
        }
        // From API 8
        if (build >= 8) {
            if (srcexif.getAttribute("FocalLength") != null) {
                destexif.setAttribute("FocalLength",
                        srcexif.getAttribute("FocalLength"));
            }
            if (srcexif.getAttribute("GPSDateStamp") != null) {
                destexif.setAttribute("GPSDateStamp",
                        srcexif.getAttribute("GPSDateStamp"));
            }
            if (srcexif.getAttribute("GPSProcessingMethod") != null) {
                destexif.setAttribute(
                        "GPSProcessingMethod",
                        srcexif.getAttribute("GPSProcessingMethod"));
            }
            if (srcexif.getAttribute("GPSTimeStamp") != null) {
                destexif.setAttribute("GPSTimeStamp", ""
                        + srcexif.getAttribute("GPSTimeStamp"));
            }
        }
        if (srcexif.getAttribute("DateTime") != null) {
            destexif.setAttribute("DateTime",
                    srcexif.getAttribute("DateTime"));
        }
        if (srcexif.getAttribute("Flash") != null) {
            destexif.setAttribute("Flash",
                    srcexif.getAttribute("Flash"));
        }
        if (srcexif.getAttribute("GPSLatitude") != null) {
            destexif.setAttribute("GPSLatitude",
                    srcexif.getAttribute("GPSLatitude"));
        }
        if (srcexif.getAttribute("GPSLatitudeRef") != null) {
            destexif.setAttribute("GPSLatitudeRef",
                    srcexif.getAttribute("GPSLatitudeRef"));
        }
        if (srcexif.getAttribute("GPSLongitude") != null) {
            destexif.setAttribute("GPSLongitude",
                    srcexif.getAttribute("GPSLongitude"));
        }
        if (srcexif.getAttribute("GPSLatitudeRef") != null) {
            destexif.setAttribute("GPSLongitudeRef",
                    srcexif.getAttribute("GPSLongitudeRef"));
        }
        //Need to update it, with your new height width
        destexif.setAttribute("ImageLength",
                "200");
        destexif.setAttribute("ImageWidth",
                "200");

        if (srcexif.getAttribute("Make") != null) {
            destexif.setAttribute("Make",
                    srcexif.getAttribute("Make"));
        }
        if (srcexif.getAttribute("Model") != null) {
            destexif.setAttribute("Model",
                    srcexif.getAttribute("Model"));
        }
        if (srcexif.getAttribute("Orientation") != null) {
            destexif.setAttribute("Orientation",
                    srcexif.getAttribute("Orientation"));
        }
        if (srcexif.getAttribute("WhiteBalance") != null) {
            destexif.setAttribute("WhiteBalance",
                    srcexif.getAttribute("WhiteBalance"));
        }

        destexif.saveAttributes();

        //File written, return to the caller. Done!
        return compressed;
    }

    private static Bitmap decodeImageFromFiles(String path, int width, int height) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2;
        }
        // decode with the sample size
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, outOptions);
    }
}
