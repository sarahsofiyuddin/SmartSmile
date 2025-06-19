package com.example.smartsmile;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private LinearLayout reportContainer;
    private AutoCompleteTextView childNameFilter;
    private FirebaseFirestore db;
    private String uid;

    private List<String> childNames = new ArrayList<>();
    private List<DocumentSnapshot> allReports = new ArrayList<>();

    public ReportFragment() {}

    public static ReportFragment newInstance(String param1, String param2) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle params if needed
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        reportContainer = view.findViewById(R.id.reportContainer);
        childNameFilter = view.findViewById(R.id.childNameFilter);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChildNames();
        loadDetectionReports();

        childNameFilter.setOnItemClickListener((parent, v, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            filterReportsByChild(selectedName);
        });

        return view;
    }

    private void loadChildNames() {
        db.collection("User").document(uid).collection("Child")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) {
                            childNames.add(name);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, childNames);
                    childNameFilter.setAdapter(adapter);

                    childNameFilter.setOnClickListener(v -> {
                        if (!childNames.isEmpty()) {
                            childNameFilter.showDropDown();
                        }
                    });
                });
    }

    private void loadDetectionReports() {
        db.collection("User").document(uid).collection("Detection")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allReports = queryDocumentSnapshots.getDocuments();
                    displayReports(allReports);
                });
    }

    private void filterReportsByChild(String childName) {
        List<DocumentSnapshot> filtered = new ArrayList<>();
        for (DocumentSnapshot doc : allReports) {
            if (childName.equals(doc.getString("childName"))) {
                filtered.add(doc);
            }
        }
        displayReports(filtered);
    }

    private void displayReports(List<DocumentSnapshot> reports) {
        reportContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (DocumentSnapshot doc : reports) {
            View reportView = inflater.inflate(R.layout.item_report_card, reportContainer, false);
            TextView reportDate = reportView.findViewById(R.id.reportDate);
            TextView childName = reportView.findViewById(R.id.childName);
            ImageView resultImage = reportView.findViewById(R.id.resultImage);
            TextView diseaseResult = reportView.findViewById(R.id.diseaseResult);
            TextView treatmentRecommendation = reportView.findViewById(R.id.treatmentRecommendation);
            View deleteButton = reportView.findViewById(R.id.deleteReportButton);

            Timestamp timestamp = doc.getTimestamp("timestamp");
            String formattedDate = timestamp != null ?
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp.toDate()) : "Unknown date";
            reportDate.setText("Date: " + formattedDate);

            String childNameText = doc.getString("childName");
            childName.setText("Child: " + (childNameText != null ? childNameText : "Unknown"));

            diseaseResult.setText("Disease: " + doc.getString("prediction"));
            treatmentRecommendation.setText("Recommendation: " + doc.getString("treatment"));

            String imageUrl = doc.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(Uri.parse(imageUrl)).into(resultImage);
            }

            deleteButton.setOnClickListener(v -> showDeleteDialog(doc, childNameText, reports));

            reportContainer.addView(reportView);
        }
    }

    private void showDeleteDialog(DocumentSnapshot doc, String childNameText, List<DocumentSnapshot> reports) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_delete_report, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.buttonCancelDelete);
        Button btnConfirm = dialogView.findViewById(R.id.buttonConfirmDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            doc.getReference().delete()
                    .addOnSuccessListener(unused -> {
                        db.collection("User").document(uid)
                                .collection("Detection")
                                .whereEqualTo("childName", childNameText)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (querySnapshot.isEmpty()) {
                                        db.collection("User").document(uid)
                                                .collection("Child").document(childNameText)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    childNames.remove(childNameText);
                                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                                            android.R.layout.simple_dropdown_item_1line, childNames);
                                                    childNameFilter.setAdapter(adapter);
                                                });
                                    }
                                });

                        reports.remove(doc);
                        displayReports(reports);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        android.widget.Toast.makeText(getContext(), "Failed to delete report.", android.widget.Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }
}