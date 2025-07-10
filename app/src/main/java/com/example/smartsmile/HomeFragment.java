package com.example.smartsmile;

import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.mikephil.charting.formatter.ValueFormatter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LineChart lineChart;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        lineChart = view.findViewById(R.id.lineChartScans);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            loadScanData(uid);
            loadTotalChildren(uid, view);
            RecyclerView recyclerView = view.findViewById(R.id.recyclerChildren);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            loadChildrenList(uid, recyclerView);

            SwitchCompat switchGraph = view.findViewById(R.id.switchGraph);
            TextView toggleLabel = view.findViewById(R.id.toggleLabel);
            ImageView toggleIcon = view.findViewById(R.id.toggleIcon);
            LineChart lineChart = view.findViewById(R.id.lineChartScans);
            LinearLayout simpleListContainer = view.findViewById(R.id.simpleListContainer);

            switchGraph.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    toggleLabel.setText("Graph");
                    toggleIcon.setImageResource(R.drawable.graph_icon);
                    lineChart.setVisibility(View.VISIBLE);
                    simpleListContainer.setVisibility(View.GONE);
                    lineChart.setVisibility(View.VISIBLE);
                    simpleListContainer.setVisibility(View.GONE);
                } else {
                    toggleLabel.setText("List");
                    toggleIcon.setImageResource(R.drawable.list_icon);
                    lineChart.setVisibility(View.GONE);
                    simpleListContainer.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    simpleListContainer.setVisibility(View.VISIBLE);
                    loadSimpleScanSummary(uid, simpleListContainer);
                }
            });
        }

        return view;
    }

    private void loadTotalChildren(String uid, View view) {
        db.collection("User").document(uid).collection("Child")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalChildren = queryDocumentSnapshots.size();
                    TextView totalChildrenView = view.findViewById(R.id.textTotalChildren);
                    totalChildrenView.setText(String.valueOf(totalChildren));
                });
    }

    private void loadChildrenList(String uid, RecyclerView recyclerView) {
        db.collection("User").document(uid).collection("Child")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Child> childList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        int age = 0;
                        Object ageObj = doc.get("age");

                        if (ageObj instanceof Number) {
                            age = ((Number) ageObj).intValue();
                        } else if (ageObj instanceof String) {
                            try {
                                age = Integer.parseInt((String) ageObj);
                            } catch (NumberFormatException ignored) {}
                        }

                        childList.add(new Child(name, age));
                    }

                    ChildAdapter adapter = new ChildAdapter(requireContext(), childList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void loadScanData(String uid) {
        db.collection("User")
                .document(uid)
                .collection("Detection")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> dateCountMap = new LinkedHashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            String dateStr = sdf.format(timestamp.toDate());
                            int count = dateCountMap.getOrDefault(dateStr, 0);
                            dateCountMap.put(dateStr, count + 1);
                        }
                    }

                    int totalScans = queryDocumentSnapshots.size();
                    TextView totalScansView = getView().findViewById(R.id.textTotalScans);
                    if (totalScansView != null) {
                        totalScansView.setText(" " + totalScans);
                    }

                    List<Entry> entries = new ArrayList<>();
                    List<String> xLabels = new ArrayList<>();
                    int index = 0;
                    for (Map.Entry<String, Integer> entry : dateCountMap.entrySet()) {
                        entries.add(new Entry(index, entry.getValue()));
                        xLabels.add(entry.getKey());
                        index++;
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Scans per Date");
                    dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.primary_yellow));
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.primary_yellow));
                    dataSet.setCircleRadius(5f);
                    dataSet.setValueTextSize(10f);
                    dataSet.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.valueOf((int) value);
                        }
                    });

                    LineData lineData = new LineData(dataSet);
                    lineChart.setData(lineData);

                    lineChart.setVisibleXRangeMaximum(5); // Show 5 data points at once
                    lineChart.moveViewToX(entries.size()); // Move view to latest

                    CustomMarkerView markerView = new CustomMarkerView(requireContext(), xLabels);
                    markerView.setChartView(lineChart);
                    lineChart.setMarker(markerView);

                    lineChart.getDescription().setEnabled(false);
                    lineChart.animateX(1000);
                    lineChart.invalidate();

                    // Enable full zooming & panning
                    lineChart.setTouchEnabled(true);
                    lineChart.setDragEnabled(true);
                    lineChart.setScaleEnabled(true);           // Enable zooming on both axes
                    lineChart.setScaleXEnabled(true);
                    lineChart.setScaleYEnabled(true);
                    lineChart.setPinchZoom(true);              // Enable pinch to zoom
                    lineChart.setDoubleTapToZoomEnabled(true); // Optional: double-tap zoom

                    // X-Axis (labels)
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(xLabels.size(), true);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int idx = Math.round(value);
                            return (idx >= 0 && idx < xLabels.size()) ? xLabels.get(idx) : "";
                        }
                    });
                    xAxis.setDrawGridLines(true);
                    xAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.primary_pink));
                    xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

                    // Y-Axis Left
                    YAxis yAxisLeft = lineChart.getAxisLeft();
                    yAxisLeft.setGranularity(1f);
                    yAxisLeft.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.valueOf((int) value);
                        }
                    });
                    yAxisLeft.setDrawGridLines(true);
                    yAxisLeft.setGridColor(ContextCompat.getColor(requireContext(), R.color.primary_pink));
                    yAxisLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

                    // Disable right Y-axis
                    lineChart.getAxisRight().setEnabled(false);

                    // Chart appearance
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);

                    // Animate chart on entry
                    lineChart.animateX(1000);  // animate left to right
                    lineChart.animateY(1000);  // animate bottom to top

                    lineChart.setData(lineData);
                    lineChart.invalidate(); // Refresh chart
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void loadSimpleScanSummary(String uid, LinearLayout container) {
        db.collection("User")
                .document(uid)
                .collection("Detection")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Map<String, Integer> scanCounts = new LinkedHashMap<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    for (DocumentSnapshot doc : querySnapshots) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            String dateStr = sdf.format(timestamp.toDate());
                            scanCounts.put(dateStr, scanCounts.getOrDefault(dateStr, 0) + 1);
                        }
                    }

                    container.removeAllViews(); // Clear previous items

                    for (Map.Entry<String, Integer> entry : scanCounts.entrySet()) {
                        View itemView = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_scan_summary, container, false);

                        TextView dateText = itemView.findViewById(R.id.textScanDate);
                        TextView countText = itemView.findViewById(R.id.textScanCount);

                        dateText.setText("Date: " + entry.getKey());
                        countText.setText("Total Scans: " + entry.getValue());

                        container.addView(itemView);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

}