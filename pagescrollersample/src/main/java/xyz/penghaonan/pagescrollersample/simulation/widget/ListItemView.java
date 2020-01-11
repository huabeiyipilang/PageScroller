package xyz.penghaonan.pagescrollersample.simulation.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import xyz.penghaonan.pagescrollersample.R;

public class ListItemView extends LinearLayout {

    private TextView indexTextView;

    public ListItemView(Context context) {
        this(context, null);
    }

    public ListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.item_view_list, this);
        indexTextView = findViewById(R.id.indexTextView);
    }

    public void setIndex(int index) {
        indexTextView.setText(String.valueOf(index));
    }
}
