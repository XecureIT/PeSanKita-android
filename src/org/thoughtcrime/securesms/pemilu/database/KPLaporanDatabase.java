package org.thoughtcrime.securesms.pemilu.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.thoughtcrime.securesms.pemilu.KPLaporanBaruActivity;
import org.thoughtcrime.securesms.pemilu.model.KPLaporan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rendra on 06/04/19.
 */

public class KPLaporanDatabase {
    private static final String TAG = KPLaporanDatabase.class.getSimpleName();

    private static final String TABLE_NAME = "laporan";
    private static final String _ID = "_id";
    private static final String PROVINSI = "provinsi";
    private static final String KABUPATEN = "kabupaten";
    private static final String KECAMATAN = "kecamatan";
    private static final String KELURAHAN = "kelurahan";
    private static final String TPS = "tps";
    private static final String COUNT1 = "count1";
    private static final String COUNT2 = "count2";
    private static final String S1 = "s1";
    private static final String N1 = "n1";
    private static final String ATTACHMENT_ID = "attachmentId";
    private static final String ATTACHMENT_ID_BASE64 = "attachmentIdBase64";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";
    private static final String TYPE = "type";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PROVINSI + " TEXT," +
            KABUPATEN + " TEXT," +
            KECAMATAN + " TEXT," +
            KELURAHAN + " TEXT," +
            TPS + " INTERGER," +
            COUNT1 + " INTERGER," +
            COUNT2 + " INTERGER," +
            S1 + " INTERGER," +
            N1 + " INTERGER," +
            ATTACHMENT_ID + " TEXT ," +
            ATTACHMENT_ID_BASE64 + " TEXT ," +
            TYPE + " INTERGER," +
            CREATED_AT + " TEXT ," +
            UPDATED_AT + " TEXT );";

    protected SQLiteOpenHelper databaseHelper;

    public KPLaporanDatabase(SQLiteOpenHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public long addOrUpdateLaporan(KPLaporan laporan) {
        if (!isExistLaporan(laporan.getKelurahan(), laporan.getTps())) {
            return insertLaporan(laporan);
        } else {
            return updateLaporan(laporan);
        }
    }

    public long insertLaporan(KPLaporan laporan) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(PROVINSI, laporan.getProvinsi());
        value.put(KABUPATEN, laporan.getKabupaten());
        value.put(KECAMATAN, laporan.getKecamatan());
        value.put(KELURAHAN, laporan.getKelurahan());
        value.put(TPS, laporan.getTps());
        value.put(COUNT1, laporan.getCount1());
        value.put(COUNT2, laporan.getCount2());
        value.put(S1, laporan.getS1());
        value.put(N1, laporan.getN1());
        value.put(ATTACHMENT_ID, laporan.getAttachmentId());
        value.put(ATTACHMENT_ID_BASE64, laporan.getAttachmentIdBase64());
        value.put(TYPE, laporan.getType());
        value.put(CREATED_AT, laporan.getCreated_at());

        try {
            long result = db.insert(TABLE_NAME, null, value);
            Log.d(TAG, "insertLaporan: result:" + result);
            return result;
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public long updateLaporan(KPLaporan laporan) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(PROVINSI, laporan.getProvinsi());
        value.put(KABUPATEN, laporan.getKabupaten());
        value.put(KECAMATAN, laporan.getKecamatan());
        value.put(KELURAHAN, laporan.getKelurahan());
        value.put(TPS, laporan.getTps());
        value.put(COUNT1, laporan.getCount1());
        value.put(COUNT2, laporan.getCount2());
        value.put(S1, laporan.getS1());
        value.put(N1, laporan.getN1());
        value.put(ATTACHMENT_ID, laporan.getAttachmentId());
        value.put(ATTACHMENT_ID_BASE64, laporan.getAttachmentIdBase64());
        value.put(TYPE, laporan.getType());
        value.put(UPDATED_AT, String.valueOf(System.currentTimeMillis()));

        try {
            long result = db.update(TABLE_NAME, value, KELURAHAN + "=? AND " + TPS + "=? ", new String[]{laporan.getKelurahan(), laporan.getTps()});
            Log.d(TAG, "updateLaporan: result:" + result);
            return result;
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public int deleteLaporan(KPLaporan laporan) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        try {
            return db.delete(TABLE_NAME, KELURAHAN + "=? AND " + TPS + "=? ", new String[]{laporan.getKelurahan(), laporan.getTps()});
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public boolean isExistLaporan(String idKelurahan, String tps) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, KELURAHAN + "=? AND " + TPS + "=? ", new String[]{idKelurahan, tps
        }, null, null, null);

        try {
            if (cursor != null)
                return (cursor.getCount() > 0);
        } finally {
            if (cursor != null) cursor.close();
            if (db.isOpen()) db.close();
        }

        return false;
    }

    public long updateC1Laporan(String _id, String c1) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(ATTACHMENT_ID, c1);
        value.put(UPDATED_AT, String.valueOf(System.currentTimeMillis()));

        try {
            long result = db.update(TABLE_NAME, value, _ID + "=?", new String[]{_id});
            Log.d(TAG, "updateC1Laporan: result: " + result);

            return result;
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public long updateC1Base64Laporan(String _id, String c1base64) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(ATTACHMENT_ID_BASE64, c1base64);
        value.put(UPDATED_AT, String.valueOf(System.currentTimeMillis()));

        try {
            long result = db.update(TABLE_NAME, value, _ID + "=?", new String[]{_id});
            Log.d(TAG, "updateC1Base64Laporan: result: " + result);
            return result;
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public List<KPLaporan> getAllLaporan() {
        List<KPLaporan> listLaporan = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " ORDER BY " +
                PROVINSI + " ASC, " +
                KABUPATEN + " ASC, " +
                KECAMATAN + " ASC, " +
                KELURAHAN + " ASC, " +
                TPS + " ASC ;", null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    KPLaporan laporan = new KPLaporan();
                    laporan.setProvinsi(c.getString(c.getColumnIndex(PROVINSI)));
                    laporan.setKabupaten(c.getString(c.getColumnIndex(KABUPATEN)));
                    laporan.setKecamatan(c.getString(c.getColumnIndex(KECAMATAN)));
                    laporan.setKelurahan(c.getString(c.getColumnIndex(KELURAHAN)));
                    laporan.setTps(c.getString(c.getColumnIndex(TPS)));
                    laporan.setCount1(c.getInt(c.getColumnIndex(COUNT1)));
                    laporan.setCount2(c.getInt(c.getColumnIndex(COUNT2)));
                    laporan.setS1(c.getInt(c.getColumnIndex(S1)));
                    laporan.setN1(c.getInt(c.getColumnIndex(N1)));
                    laporan.setAttachmentId(c.getString(c.getColumnIndex(ATTACHMENT_ID)));
                    laporan.setAttachmentIdBase64(c.getString(c.getColumnIndex(ATTACHMENT_ID_BASE64)));
                    laporan.setType(c.getInt(c.getColumnIndex(TYPE)));
                    laporan.setCreated_at(c.getString(c.getColumnIndex(CREATED_AT)));
                    laporan.setUpdated_at(c.getString(c.getColumnIndex(UPDATED_AT)));

                    listLaporan.add(laporan);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
            if (db.isOpen()) db.close();
        }

        return listLaporan;
    }

    public List<KPLaporan> getRetryLaporan() {
        List<KPLaporan> listLaporan = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
                TYPE + "=" + KPLaporanBaruActivity.RETRY +
                " ORDER BY " +
                PROVINSI + " ASC, " +
                KABUPATEN + " ASC, " +
                KECAMATAN + " ASC, " +
                KELURAHAN + " ASC, " +
                TPS + " ASC ;", null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    KPLaporan laporan = new KPLaporan();
                    laporan.setProvinsi(c.getString(c.getColumnIndex(PROVINSI)));
                    laporan.setKabupaten(c.getString(c.getColumnIndex(KABUPATEN)));
                    laporan.setKecamatan(c.getString(c.getColumnIndex(KECAMATAN)));
                    laporan.setKelurahan(c.getString(c.getColumnIndex(KELURAHAN)));
                    laporan.setTps(c.getString(c.getColumnIndex(TPS)));
                    laporan.setCount1(c.getInt(c.getColumnIndex(COUNT1)));
                    laporan.setCount2(c.getInt(c.getColumnIndex(COUNT2)));
                    laporan.setS1(c.getInt(c.getColumnIndex(S1)));
                    laporan.setN1(c.getInt(c.getColumnIndex(N1)));
                    laporan.setAttachmentId(c.getString(c.getColumnIndex(ATTACHMENT_ID)));
                    laporan.setAttachmentIdBase64(c.getString(c.getColumnIndex(ATTACHMENT_ID_BASE64)));
                    laporan.setType(c.getInt(c.getColumnIndex(TYPE)));
                    laporan.setCreated_at(c.getString(c.getColumnIndex(CREATED_AT)));
                    laporan.setUpdated_at(c.getString(c.getColumnIndex(UPDATED_AT)));

                    listLaporan.add(laporan);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
            if (db.isOpen()) db.close();
        }

        return listLaporan;
    }
}
