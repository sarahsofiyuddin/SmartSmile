package com.example.smartsmile;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.example.smartsmile.R;

import java.util.List;

public class CustomMarkerView extends MarkerView {

    private final TextView tvDate;
    private final TextView tvCount;
    private final List<String> xLabels;

    public CustomMarkerView(Context context, List<String> xLabels) {
        super(context, R.layout.custom_marker_view);
        this.xLabels = xLabels;
        tvDate = findViewById(R.id.tvDate);
        tvCount = findViewById(R.id.tvCount);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int xIndex = (int) e.getX();
        String date = (xIndex >= 0 && xIndex < xLabels.size()) ? xLabels.get(xIndex) : "Unknown";
        tvDate.setText("Date: " + date);
        tvCount.setText("Scans: " + (int) e.getY());

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // Center the marker horizontally and place it above the point
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}