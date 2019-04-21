package org.thoughtcrime.securesms.pemilu.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.thoughtcrime.securesms.pemilu.model.KPDaerah;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rendra on 06/04/19.
 */

public class KPDaerahDatabase {
    private static final String TAG = KPDaerahDatabase.class.getSimpleName();

    private static final String TABLE_NAME = "daerah";
    private static final String _ID = "_id";
    private static final String ID = "id";
    private static final String PARENT = "parent";
    public static final String[] CREATE_INDEXES = {
            "CREATE INDEX IF NOT EXISTS daerah_id_index ON " + TABLE_NAME + " (" + ID + ");",
            "CREATE INDEX IF NOT EXISTS daerah_parent_index ON " + TABLE_NAME + " (" + PARENT + ");",
    };
    private static final String NAME = "name";
    private static final String TPS = "tps";
    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ID + " TEXT UNIQUE," +
            PARENT + " TEXT," +
            NAME + " TEXT ," +
            TPS + " INTEGER );";
    protected SQLiteOpenHelper databaseHelper;

    public KPDaerahDatabase() {
        // default constructor
    }

    public KPDaerahDatabase(SQLiteOpenHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    protected SQLiteDatabase getReadableDatabase() {
        return databaseHelper.getReadableDatabase();
    }

    protected SQLiteDatabase getWritableDatabase() {
        return databaseHelper.getWritableDatabase();
    }

    public long insertDaerah(KPDaerah.ItemDaerah item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID, item.getId());
        values.put(PARENT, item.getParent());
        values.put(NAME, item.getNama());
        values.put(TPS, item.getJmltps());

        try {
            long result = db.insert(TABLE_NAME, null, values);
            Log.d(TAG, "insertDaerah: result:" + result);
            return result;
        } finally {
            if (db.isOpen()) db.close();
        }
    }

    public void insertDaerah(List<KPDaerah.ItemDaerah> itemsDaerah, String parent) {
        for (KPDaerah.ItemDaerah item : itemsDaerah) {
            item.setParent(parent);
            addOrUpdateDaerah(item);
        }
    }

    private long updateDaerah(KPDaerah.ItemDaerah item) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PARENT, item.getParent());
        values.put(NAME, item.getNama());
        values.put(TPS, item.getJmltps());

        try {
            long result = db.update(TABLE_NAME, values, ID + "=?", new String[]{item.getId()});
            Log.d(TAG, "updateDaerah: result:" + result);
            return result;
        } finally {
//            if (db.isOpen()) db.close();
        }
    }

    public long addOrUpdateDaerah(KPDaerah.ItemDaerah item) {
        long result;

        if (!isExistDaerah(item.getId())) {
            Log.d(TAG, "addOrUpdateDaerah: add start");
            result = insertDaerah(item);
            Log.d(TAG, "addOrUpdateDaerah: add finish");
        } else {
            Log.d(TAG, "addOrUpdateDaerah: update start");
            result = updateDaerah(item);
            Log.d(TAG, "addOrUpdateDaerah: update finish");
        }

        return result;
    }

    public KPDaerah.ItemDaerah getDaerah(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = " + id + ";", null);

        try {
            if (c != null && c.moveToNext()) {
                KPDaerah.ItemDaerah daerah = new KPDaerah.ItemDaerah();
                daerah.setId(id);
                daerah.setParent(c.getString(c.getColumnIndex(PARENT)));
                daerah.setNama(c.getString(c.getColumnIndex(NAME)));
                daerah.setJmltps(c.getInt(c.getColumnIndex(TPS)));

                return daerah;
            }
        } finally {
            if (c != null) c.close();
//            if (db.isOpen()) db.close();
        }

        return null;
    }

    public List<KPDaerah.ItemDaerah> getAllDaerah() {
        List<KPDaerah.ItemDaerah> listDaerah = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + ";", null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    KPDaerah.ItemDaerah daerah = new KPDaerah.ItemDaerah();
                    daerah.setId(c.getString(c.getColumnIndex(ID)));
                    daerah.setParent(c.getString(c.getColumnIndex(PARENT)));
                    daerah.setNama(c.getString(c.getColumnIndex(NAME)));
                    daerah.setJmltps(c.getInt(c.getColumnIndex(TPS)));
                    listDaerah.add(daerah);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
//            if (db.isOpen()) db.close();
        }

        return listDaerah;
    }

    public List<KPDaerah.ItemDaerah> getProvinsiDb() {
        List<KPDaerah.ItemDaerah> listDaerah = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + PARENT + " IS NULL;", null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    KPDaerah.ItemDaerah daerah = new KPDaerah.ItemDaerah();
                    daerah.setId(c.getString(c.getColumnIndex(ID)));
                    daerah.setParent(c.getString(c.getColumnIndex(PARENT)));
                    daerah.setNama(c.getString(c.getColumnIndex(NAME)));
                    daerah.setJmltps(c.getInt(c.getColumnIndex(TPS)));
                    listDaerah.add(daerah);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
//            if (db.isOpen()) db.close();
        }

        return listDaerah;
    }

    public List<KPDaerah.ItemDaerah> getKabupatenDb(String idProvinsi) {
        return getChildDaerah(idProvinsi);
    }

    public List<KPDaerah.ItemDaerah> getKecamatanDb(String idKabupaten) {
        return getChildDaerah(idKabupaten);
    }

    public List<KPDaerah.ItemDaerah> getKelurahanDb(String idKecamatan) {
        return getChildDaerah(idKecamatan);
    }

    public List<KPDaerah.ItemDaerah> getChildDaerah(String parentId) {
        List<KPDaerah.ItemDaerah> listDaerah = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + PARENT + " = " + parentId + ";", null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    KPDaerah.ItemDaerah daerah = new KPDaerah.ItemDaerah();
                    daerah.setId(c.getString(c.getColumnIndex(ID)));
                    daerah.setParent(c.getString(c.getColumnIndex(PARENT)));
                    daerah.setNama(c.getString(c.getColumnIndex(NAME)));
                    daerah.setJmltps(c.getInt(c.getColumnIndex(TPS)));
                    listDaerah.add(daerah);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
//            if (db.isOpen()) db.close();
        }

        return listDaerah;
    }

    public KPDaerah.ItemDaerah getParentDaerah(String id) {
        KPDaerah.ItemDaerah child = getDaerah(id);
        return getDaerah(child.getParent());
    }

    public String getSectionString(String id) {
        KPDaerah.ItemDaerah kelurahan = getDaerah(id);
        KPDaerah.ItemDaerah kecamatan = getDaerah(kelurahan.getParent());
        KPDaerah.ItemDaerah kabupaten = getDaerah(kecamatan.getParent());
        KPDaerah.ItemDaerah provinsi = getDaerah(kabupaten.getParent());

        return provinsi.getNama() + " > " + kabupaten.getNama() + " > " + kecamatan.getNama() + " > " + kelurahan.getNama();
    }

    public boolean isExistDaerah(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "ID=?", new String[]{id}, null, null, null);

        try {
            if (c != null)
                return (c.getCount() > 0);
        } finally {
            if (c != null) c.close();
        }

        return false;
    }
}
