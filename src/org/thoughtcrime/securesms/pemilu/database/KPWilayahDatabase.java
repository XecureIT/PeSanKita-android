package org.thoughtcrime.securesms.pemilu.database;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.thoughtcrime.securesms.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rendra on 06/04/19.
 */

public class KPWilayahDatabase extends KPDaerahDatabase {
    private static final String TAG = KPWilayahDatabase.class.getSimpleName();

    private SQLiteDatabase db;

    private static final String DATABASE_NAME = "kp_wilayah.db";
    private static final String ASSET_PATH    = "databases" + File.separator + DATABASE_NAME;

    private static KPWilayahDatabase instance = null;

    public synchronized static KPWilayahDatabase getInstance(Context context) throws IOException {
        if (instance == null) instance = new KPWilayahDatabase(context.getApplicationContext());
        return instance;
    }

    public KPWilayahDatabase(final Context context) throws IOException {
        File dbFile = context.getDatabasePath(DATABASE_NAME);

        if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdir()) {
            throw new IOException("couldn't make databases directory");
        }

        Util.copy(context.getAssets().open(ASSET_PATH, AssetManager.ACCESS_STREAMING),
                new FileOutputStream(dbFile));

        try {
            this.db = SQLiteDatabase.openDatabase(context.getDatabasePath(DATABASE_NAME).getPath(),
                    null,
                    SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (SQLiteException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected SQLiteDatabase getReadableDatabase() {
        return this.db;
    }

    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return this.db;
    }
}
