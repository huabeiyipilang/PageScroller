package xyz.penghaonan.pagescrollersample.simulation.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SampleListViewAdapter extends BaseAdapter {

    private static final int COUNT = 100;

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ListItemView(parent.getContext());
        }
        ((ListItemView) convertView).setIndex(position);
        return convertView;
    }
}
