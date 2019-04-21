package org.thoughtcrime.securesms.pemilu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.thoughtcrime.securesms.pemilu.database.KPDaerahDatabase;
import org.thoughtcrime.securesms.pemilu.database.KPLaporanDatabase;
import org.thoughtcrime.securesms.pemilu.database.KPWilayahDatabase;

import java.io.IOException;

public class KPDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = KPDatabaseHelper.class.getSimpleName();

    private static final int UNIQUE_AND_INDEXES = 2;
    private static final int DATABASE_VERSION   = 2;

    private static final String DATABASE_NAME   = "kawalpilpres.db";
    private static final Object lock            = new Object();

    private static KPDatabaseHelper instance;

    private final KPDaerahDatabase daerahDatabase;
    private final KPLaporanDatabase laporanDatabase;

    public static KPDatabaseHelper getInstance(Context context) {
        synchronized (lock) {
            if (instance == null)
                instance = new KPDatabaseHelper(context.getApplicationContext());

            return instance;
        }
    }

    private KPDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        KPDaerahDatabase daerah = null;
        try {
            daerah = KPWilayahDatabase.getInstance(context);
        } catch (IOException e) {
            Log.e(TAG, "Exception", e);
        }

        this.daerahDatabase = daerah != null ? daerah : new KPDaerahDatabase(this);
        this.laporanDatabase = new KPLaporanDatabase(this);
    }

    public static KPDaerahDatabase getDaerahDatabase(Context context) {
        return getInstance(context).daerahDatabase;
    }

    public static KPLaporanDatabase getLaporanDatabase(Context context) {
        return getInstance(context).laporanDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(KPDaerahDatabase.CREATE_TABLE);
            db.execSQL(KPLaporanDatabase.CREATE_TABLE);

            executeStatements(db, KPDaerahDatabase.CREATE_INDEXES);
        } catch (Exception er) {
            Log.e("Error", "exception");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("onUpgrade", "Upgrading from version " + oldVersion + " to " + newVersion);
        db.beginTransaction();

        if (oldVersion < UNIQUE_AND_INDEXES) {
            db.execSQL("CREATE INDEX IF NOT EXISTS daerah_id_index ON daerah (id)");
            db.execSQL("CREATE INDEX IF NOT EXISTS daerah_parent_index ON daerah (parent)");
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void executeStatements(SQLiteDatabase db, String[] statements) {
        for (String statement : statements)
            db.execSQL(statement);
    }

}
