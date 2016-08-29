package com.heyniu.applicationfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataBaseUtil {

    private DataHelper dataHelper;
    private Context context;

    public DataBaseUtil (Context context) {
        this.context = context;
        dataHelper = new DataHelper(context);
    }

    public void insert (AppInfo appInfo, long time, byte[] image) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataHelper.CREATE_TIME, time);
        values.put(DataHelper.APP_NAME, appInfo.getName());
        values.put(DataHelper.APP_PACKAGE, appInfo.getPkg());
        values.put(DataHelper.APP_PATH, appInfo.getApkPath());
        values.put(DataHelper.APP_ICON, image);
        values.put(DataHelper.APP_LAUNCHER_COUNT, 1);
        db.insert(DataHelper.TABLE_NAME, null, values);
        db.close();
    }

    public void update (AppInfo appInfo, String packageName, long time, byte[] image) {
        int count = queryByCount(packageName) + 1;
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataHelper.CREATE_TIME, time);
        values.put(DataHelper.APP_NAME, appInfo.getName());
        values.put(DataHelper.APP_PACKAGE, appInfo.getPkg());
        values.put(DataHelper.APP_PATH, appInfo.getApkPath());
        values.put(DataHelper.APP_ICON, image);
        values.put(DataHelper.APP_LAUNCHER_COUNT, count);
        db.update(DataHelper.TABLE_NAME, values, "package=?", new String[]{packageName});
    }

    public void delete (String pkg) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.delete(DataHelper.TABLE_NAME, "package=?", new String[]{pkg});
        db.close();
    }

    public List<AppInfo> queryByTime () {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<AppInfo> appInfos = new ArrayList<>();
        String sql = String.format("select * from %s order by %s desc limit 20", DataHelper.TABLE_NAME, DataHelper.CREATE_TIME);
        Cursor cursor = db.rawQuery(sql, null);
        queryData(appInfos, db, cursor);
        return appInfos;
    }

    public List<AppInfo> queryByCount () {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        List<AppInfo> appInfos = new ArrayList<>();
        String sql = String.format("select * from %s order by %s desc limit 20", DataHelper.TABLE_NAME, DataHelper.APP_LAUNCHER_COUNT);
        Cursor cursor = db.rawQuery(sql, null);
        queryData(appInfos, db, cursor);
        return appInfos;
    }

    public boolean queryByPackage (String packageName) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String sql = String.format("select %s, %s, %s, %s from %s where package=%s", DataHelper.APP_NAME,
                DataHelper.APP_PACKAGE, DataHelper.APP_PATH, DataHelper.APP_ICON, DataHelper.TABLE_NAME, "'" + packageName + "'");
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return true;
        }
        db.close();
        return false;
    }

    public int queryByCount (String packageName) {
        SQLiteDatabase db = dataHelper.getReadableDatabase();
        String sql = String.format("select %s, %s, %s, %s from %s where package=%s", DataHelper.APP_NAME,
                DataHelper.APP_PACKAGE, DataHelper.APP_PATH, DataHelper.APP_ICON, DataHelper.TABLE_NAME, "'" + packageName + "'");
        Cursor cursor = db.rawQuery(sql, null);
        int count = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DataHelper.APP_LAUNCHER_COUNT)));
        Log.e("1112sa", count+"");
        cursor.close();
        db.close();
        return count;
    }

    private void queryData (List<AppInfo> appInfos, SQLiteDatabase db, Cursor cursor) {
        int name = cursor.getColumnIndex(DataHelper.APP_NAME);
        int packageName = cursor.getColumnIndex(DataHelper.APP_PACKAGE);
        int path = cursor.getColumnIndex(DataHelper.APP_PATH);
        int icon = cursor.getColumnIndex(DataHelper.APP_ICON);
        while (cursor.moveToNext()) {
            AppInfo appInfo = new AppInfo();
            appInfo.setName(cursor.getString(name));
            appInfo.setPkg(cursor.getString(packageName));
            appInfo.setApkPath(cursor.getString(path));
            byte[] image = cursor.getBlob(icon);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            appInfo.setDrawable(drawable);
            appInfos.add(appInfo);
        }
        cursor.close();
        db.close();
    }

}
