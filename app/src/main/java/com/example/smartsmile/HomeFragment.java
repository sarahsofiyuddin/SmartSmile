package com.example.smartsmile;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            loadTotalScansAndLastDetection(uid, view);
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

    private void loadTotalScansAndLastDetection(String uid, View view) {
        db.collection("User").document(uid).collection("Detection")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalScans = queryDocumentSnapshots.size();
                    TextView totalScansView = view.findViewById(R.id.textTotalScans);
                    totalScansView.setText(String.valueOf(totalScans));

                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot latest = queryDocumentSnapshots.getDocuments().get(0);
                        String prediction = latest.getString("prediction");
                        String treatment = latest.getString("treatment");

                        TextView lastDiseaseView = view.findViewById(R.id.textLastScanDisease);
                        TextView lastTreatmentView = view.findViewById(R.id.textLastScanTreatment);

                        lastDiseaseView.setText("Last Detected: " + (prediction != null ? prediction : "-"));
                        lastTreatmentView.setText("Recommended Treatment: " + (treatment != null ? treatment : "-"));
                    }
                });
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

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = Math.round(value);
                            if (index >= 0 && index < xLabels.size()) {
                                return xLabels.get(index);
                            } else {
                                return "";
                            }
                        }
                    });
                    xAxis.setDrawGridLines(true);
                    xAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.primary_pink));
                    xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                    YAxis yAxisLeft = lineChart.getAxisLeft();
                    yAxisLeft.setGranularity(1f); // Steps of 1
                    yAxisLeft.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.valueOf((int) value);
                        }
                    });
                    yAxisLeft.setDrawGridLines(true);
                    yAxisLeft.setGridColor(ContextCompat.getColor(requireContext(), R.color.primary_pink));
                    yAxisLeft.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

                    YAxis yAxisRight = lineChart.getAxisRight();
                    yAxisRight.setEnabled(false);

                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);
                    lineChart.animateX(1000);
                    lineChart.invalidate();
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    e.printStackTrace();
                });
    }
}