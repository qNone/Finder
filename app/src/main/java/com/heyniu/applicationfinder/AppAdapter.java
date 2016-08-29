package com.heyniu.applicationfinder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppAdapter extends BaseAdapter{

    private List<AppInfo> appInfos;
    private LayoutInflater layoutInflater;
    private String key;

    public AppAdapter (Context context, List<AppInfo> appInfos, String key) {
        this.appInfos = appInfos;
        layoutInflater = LayoutInflater.from(context);
        this.key = key;
    }

    @Override
    public int getCount() {
        return appInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.listview, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.pkg = (TextView) convertView.findViewById(R.id.pkg);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppInfo appInfo = (AppInfo) getItem(position);
        viewHolder.imageView.setImageDrawable(appInfo.getDrawable());
        if (key == null || key.length() == 0) {
            viewHolder.name.setText(appInfo.getName());
            viewHolder.pkg.setText(appInfo.getPkg());
        } else {
            viewHolder.name.setText(matcher(Color.RED, appInfo.getName(), key, Pattern.CASE_INSENSITIVE));
            viewHolder.pkg.setText(matcher(Color.RED, appInfo.getPkg(), key, Pattern.CASE_INSENSITIVE));
        }

        return convertView;
    }

    private SpannableString matcher (int color, String source, String keyword, int matcherMode) {
        SpannableString s = new SpannableString(source);
        Pattern p = Pattern.compile(keyword, matcherMode);
        Matcher m = p.matcher(s);
        while (m.find()) {
            s.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return s;
    }

    private static class ViewHolder extends AppCompatActivity{
        private ImageView imageView;
        private TextView name;
        private TextView pkg;
    }
}
