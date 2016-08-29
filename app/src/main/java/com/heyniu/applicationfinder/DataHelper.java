package com.heyniu.applicationfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper{

    private static String DB_NAME = "app.db";
    public static final String TABLE_NAME = "applicationInfo";
    public static final String ID = "id";
    public static final String CREATE_TIME = "time";
    public static final String APP_NAME = "name";
    public static final String APP_PACKAGE = "package";
    public static final String APP_PATH = "path";
    public static final String APP_ICON = "icon";
    public static final String APP_LAUNCHER_COUNT = "count";
    private static final int VERSION = 1;

    public DataHelper (Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s BLOB, %s BLOB)", TABLE_NAME,
                ID, CREATE_TIME, APP_NAME, APP_PACKAGE, APP_PATH, APP_ICON, APP_LAUNCHER_COUNT);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
