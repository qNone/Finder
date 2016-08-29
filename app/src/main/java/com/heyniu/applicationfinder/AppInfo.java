package com.heyniu.applicationfinder;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private Drawable drawable;
    private String name;
    private String pkg;
    private String apkPath;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getDrawable() {

        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }
}
