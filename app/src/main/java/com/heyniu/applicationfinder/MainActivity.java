package com.heyniu.applicationfinder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private List<AppInfo> appInfos = new ArrayList<>();
    private AppAdapter adapter;
    private List<AppInfo> initAppInfos = new ArrayList<>();
    private AppAdapter initAdapter;
    private List<ApplicationInfo> applicationInfos = new ArrayList<>();
    private ListView listView;
    private Handler handler;
    private Runnable runnable;
    private String key;
    private LinearLayout no_search;
    private DataBaseUtil dataBaseUtil;
    private TextView txt_no_data;
    private boolean searchFocus = false;
    private List<String> isInstallPkg = new ArrayList<>();
    private List<AppInfo> removeAppinfos = new ArrayList<>();

    private static final int launcher_mode = 0;
    private static final int share_mode = 1;
    private static final int app_info_mode = 2;
    private static final int uninstall_mode = 3;
    private static final int kill_mode = 4;
    private static final int market_mode = 5;
    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only support API 20 or later
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(getApplicationContext(), "Finder does not support the current Android version!",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                search(key);
            }
        };
    }

    @Override
    protected void onResume () {
        super.onResume();
        if (mode == uninstall_mode && !searchFocus && isInstallPkg.size() > 0) {
            uninstallUpdate(isInstallPkg.get(isInstallPkg.size() - 1), removeAppinfos.get(removeAppinfos.size() - 1));
        }
        if (initAppInfos.isEmpty() && !searchFocus) {
            txt_no_data.setVisibility(View.VISIBLE);
        } else {
            txt_no_data.setVisibility(View.GONE);
        }

    }

    private void uninstallUpdate(String pkg, AppInfo appinfo) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            clearCache();
            initAppInfos.remove(appinfo);
            appInfos.remove(appinfo);
            dataBaseUtil.delete(pkg);
            if (!initAppInfos.isEmpty() && !searchFocus) {
                initAdapter.notifyDataSetChanged();
            }
            if (initAppInfos.isEmpty()) {
                listView.setAdapter(null);
            }
        }
    }

    private void clearCache() {
        File[] files = getCacheDir().listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        if (searchFocus || mode == uninstall_mode) {
            if (isInstallPkg.size() > 0) {
                uninstallUpdate(isInstallPkg.get(isInstallPkg.size() - 1), removeAppinfos.get(removeAppinfos.size() - 1));
            }
            if (!appInfos.isEmpty() && key != null && key.trim().length() > 0) {
                adapter.notifyDataSetChanged();
                no_search.setVisibility(View.GONE);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AppInfo appInfo = appInfos.get(position);
                        writeDataForDataBase(appInfo.getPkg(), appInfo);
                        SelectMode(mode, appInfo);
                    }
                });
            } else if (searchFocus && key != null && key.trim().length() > 0) {
                no_search.setVisibility(View.VISIBLE);
            }
            return;
        }
        initListView();
    }

    private void init() {
        listView = (ListView) findViewById(R.id.listView);
        no_search = (LinearLayout) findViewById(R.id.no_search_results_item);
        txt_no_data = (TextView) findViewById(R.id.no_data);
        ImageView imageView = (ImageView) findViewById(R.id.no_search_results_imageView);
        if (imageView != null) {
            imageView.setColorFilter(Color.rgb(196, 196, 196), PorterDuff.Mode.MULTIPLY);
        }
        dataBaseUtil = new DataBaseUtil(MainActivity.this);
        initListView();

    }

    private void initListView() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                initAppInfos.clear();
                initAppInfos = dataBaseUtil.queryByTime();
                if (!initAppInfos.isEmpty()) {
                    int size = initAppInfos.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i ++) {
                            try {
                                getPackageManager().getPackageInfo(initAppInfos.get(0).getPkg(), 0);
                            } catch (PackageManager.NameNotFoundException e) {
                                initAppInfos.remove(i);
                            }
                        }
                    }
                }
                if (!initAppInfos.isEmpty()) {
                    txt_no_data.setVisibility(View.GONE);
                    initAdapter = new AppAdapter(MainActivity.this, initAppInfos, null);
                    listView.setAdapter(initAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            AppInfo appInfo = initAppInfos.get(position);
                            writeDataForDataBase(appInfo.getPkg(), appInfo);
                            SelectMode(mode, appInfo);
                        }
                    });
                } else if (!searchFocus) {
                    txt_no_data.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadData() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> applicationInfo = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo a : applicationInfo) {
            if (pm.getLaunchIntentForPackage(a.packageName) != null) {
                applicationInfos.add(a);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search1);
        // search_src_text
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                key = query;
                if (key.length() > 0) {
                    if (handler != null) {
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 200);
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                key = newText;
                if (key.length() > 0) {
                    if (handler != null) {
                        handler.removeCallbacks(runnable);
                        handler.postDelayed(runnable, 200);
                    }
                } else {
                    if (handler != null) {
                        handler.removeCallbacks(runnable);
                        listView.setAdapter(null);
                        no_search.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                initListView();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        int size = isInstallPkg.size();
                        if (size > 0) {
                            for (int i = 0; i < size; i ++) {
                                uninstallUpdate(isInstallPkg.get(i), removeAppinfos.get(i));
                            }
                        }
                    }
                });
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                searchFocus = hasFocus;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchFocus) {
                    txt_no_data.setVisibility(View.GONE);
                    listView.setAdapter(null);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            final View view = layoutInflater.inflate(R.layout.select_code_view, null);
            RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
            final RadioButton radio_launcher = (RadioButton) view.findViewById(R.id.radio_launcher);
            final RadioButton radio_share = (RadioButton) view.findViewById(R.id.radio_share);
            final RadioButton radio_appInfo = (RadioButton) view.findViewById(R.id.radio_appInfo);
            final RadioButton radio_uninstall = (RadioButton) view.findViewById(R.id.radio_uninstall);
            final RadioButton radio_kill = (RadioButton) view.findViewById(R.id.radio_kill);
            final RadioButton radio_market= (RadioButton) view.findViewById(R.id.radio_market);
            switch (mode) {
                case launcher_mode:
                    radio_launcher.setChecked(true);
                    break;
                case share_mode:
                    radio_share.setChecked(true);
                    break;
                case app_info_mode:
                    radio_appInfo.setChecked(true);
                    break;
                case uninstall_mode:
                    radio_uninstall.setChecked(true);
                    break;
                case kill_mode:
                    radio_kill.setChecked(true);
                    break;
                case market_mode:
                    radio_market.setChecked(true);
                    break;
                default:
                    break;
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == radio_launcher.getId()) {
                        mode = launcher_mode;
                        return;
                    }
                    if (checkedId == radio_share.getId()) {
                        mode = share_mode;
                        return;
                    }
                    if (checkedId == radio_appInfo.getId()) {
                        mode = app_info_mode;
                        return;
                    }
                    if (checkedId == radio_uninstall.getId()) {
                        mode = uninstall_mode;
                        return;
                    }
                    if (checkedId == radio_kill.getId()) {
                        mode = kill_mode;
                        return;
                    }
                    if (checkedId == radio_market.getId()) {
                        mode = market_mode;
                    }
                }
            });
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert1)
                    .setTitle("Select Mode")
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(view)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(String key) {
        String str = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]+";
        Pattern pattern = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
        if (!pattern.matcher(key).find()) {
            no_search.setVisibility(View.VISIBLE);
            listView.setAdapter(null);
            return;
        }
        appInfos.clear();
        Pattern p = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
        PackageManager pm = getPackageManager();
        for (ApplicationInfo info : applicationInfos) {
            if (p.matcher(info.loadLabel(pm).toString()).find()) {
                AppInfo appInfo = new AppInfo();
                appInfo.setDrawable(info.loadIcon(pm));
                appInfo.setPkg(info.packageName);
                appInfo.setName(info.loadLabel(pm).toString());
                appInfo.setApkPath(info.sourceDir);
                appInfos.add(appInfo);
            }
        }
        if (appInfos.isEmpty()) {
            for (ApplicationInfo info : applicationInfos) {
                if (p.matcher(info.packageName).find()) {
                    AppInfo appInfo = new AppInfo();
                    appInfo.setDrawable(info.loadIcon(pm));
                    appInfo.setPkg(info.packageName);
                    appInfo.setName(info.loadLabel(pm).toString());
                    appInfo.setApkPath(info.sourceDir);
                    appInfos.add(appInfo);
                }
            }
        }
        if (!appInfos.isEmpty()) {
            no_search.setVisibility(View.GONE);
            adapter = new AppAdapter(MainActivity.this, appInfos, key);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    AppInfo appInfo = appInfos.get(position);
                    writeDataForDataBase(appInfo.getPkg(), appInfo);
                    SelectMode(mode, appInfo);
                }
            });
        } else {
            no_search.setVisibility(View.VISIBLE);
            listView.setAdapter(null);
        }
    }

    private void SelectMode (int mode, AppInfo appInfo) {
        switch (mode) {
            case launcher_mode:
                launcherApp(appInfo.getPkg());
                break;
            case share_mode:
                if (appInfo.getPkg().equals(getPackageName())) {
                    Toast.makeText(MainActivity.this, "Failure, can not share yourself!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String path = appInfo.getApkPath();
                if (path != null && path.length() > 0) {
                    File apkFile = new File(path);
                    if (apkFile.exists()) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
                        startActivity(intent);
                    }
                }
                break;
            case app_info_mode:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.parse("package:" + appInfo.getPkg());
                intent.setData(uri);
                startActivity(intent);
                break;
            case uninstall_mode:
                final String pkg = appInfo.getPkg();
                if (pkg.equals(getPackageName())) {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert1)
                            .setTitle("Warning")
                            .setIcon(R.drawable.warning_orange)
                            .setMessage("Uninstall will not be able to continue to serve you confirm that you want to do this?")
                            .setPositiveButton("UNINSTALL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    uninstall(pkg);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.button_color_delete));
                    return;
                }
                uninstall(pkg);
                removeAppinfos.add(appInfo);
                break;
            case kill_mode:
                if (appInfo.getPkg().equals(getPackageName())) {
                    Toast.makeText(MainActivity.this, "Failure, can not kill yourself!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ActivityManager am = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(appInfo.getPkg());
                Toast.makeText(MainActivity.this, "Success kill " + appInfo.getName(), Toast.LENGTH_SHORT).show();
                break;
            case market_mode:
                try {
                    Intent intent_market = new Intent();
                    intent_market.setAction(Intent.ACTION_VIEW);
                    Uri uri_market = Uri.parse("market://details?id=" + appInfo.getPkg());
                    intent_market.setData(uri_market);
                    startActivity(intent_market);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Application market not found!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void uninstall(String pkg) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);
        Uri uri = Uri.parse("package:" + pkg);
        intent.setData(uri);
        startActivity(intent);
        isInstallPkg.add(pkg);
    }

    private void writeDataForDataBase(String packageName, AppInfo appInfo) {
        Bitmap bitmap = drawableToBitmap(appInfo.getDrawable());
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOut);
        byte[] image = byteOut.toByteArray();
        long time = System.currentTimeMillis();
        boolean result = dataBaseUtil.queryByPackage(packageName);
        if (result) {
            dataBaseUtil.update(appInfo, packageName, time, image);
        } else {
            dataBaseUtil.insert(appInfo, time, image);
        }
    }

    private void launcherApp(String pkg) {
        //hide SoftInput
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (searchFocus) {
            imm.hideSoftInputFromWindow(getWindow().peekDecorView().getWindowToken(), 0);
        }
        if (pkg.length() > 0) {
            if (pkg.equals(getPackageName())) {
                Toast.makeText(this, "This application can not start.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) {
                startActivity(intent);
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

}
