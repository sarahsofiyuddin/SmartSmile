package com.example.smartsmile;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DetectionResultFragment extends Fragment {

    private static final String[] CLASS_NAMES = {
            "Dental Calculus",
            "Dental Caries",
            "Gingivitis",
            "Hypodontia"
    };

    private static final Map<String, String> TREATMENT_MAP = new HashMap<>();
    static {
        TREATMENT_MAP.put("Dental Calculus",
                "\n1. Professional dental cleaning and scaling by a specialist.\n" +
                        "2. Daily mechanical plaque control using toothbrush and dental floss.\n" +
                        "3. Use of therapeutic mouthwashes and interdental cleaning tools.");

        TREATMENT_MAP.put("Dental Caries",
                "\n1. Apply fluoride toothpaste and dental sealants to protect enamel.\n" +
                        "2. Practice proper oral hygiene including brushing and flossing.\n" +
                        "3. Consider minimally invasive treatments like resin infiltration and selective caries removal.");

        TREATMENT_MAP.put("Gingivitis",
                "\n1. Regular toothbrushing and use of interdental brushes/floss to control plaque.\n" +
                        "2. Use chlorhexidine mouthwash during high-risk periods or compromised hygiene.\n" +
                        "3. Professional dental cleanings and personalized oral hygiene education.");

        TREATMENT_MAP.put("Hypodontia",
                "\n1. Multidisciplinary evaluation for tailored treatment planning.\n" +
                        "2. Replace missing teeth using implants, bridges, or partial dentures.\n" +
                        "3. Orthodontic treatment to close gaps and restore bite alignment.");
    }

    private TextView textResult;
    private TextView textTips;
    private TextView textError;
    private ImageView imageResult;

    public DetectionResultFragment() {}

    public static DetectionResultFragment newInstance(String param1, String param2) {
        DetectionResultFragment fragment = new DetectionResultFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_result, container, false);

        textResult = view.findViewById(R.id.text_result);
        textTips = view.findViewById(R.id.text_tips);
        textError = view.findViewById(R.id.text_error);
        imageResult = view.findViewById(R.id.imageResult);

        displayResult();

        return view;
    }

    private void displayResult() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("inferenceResults")) {
            float[] logits = args.getFloatArray("inferenceResults");
            String imageUriStr = args.getString("imageUri");

            if (imageUriStr != null) {
                Uri imageUri = Uri.parse(imageUriStr);
                imageResult.setImageURI(imageUri);
            }

            if (logits != null && logits.length == CLASS_NAMES.length) {
                float maxLogit = Float.NEGATIVE_INFINITY;
                for (float logit : logits) {
                    if (logit > maxLogit) maxLogit = logit;
                }

                float sum = 0f;
                float[] probs = new float[logits.length];
                for (int i = 0; i < logits.length; i++) {
                    probs[i] = (float) Math.exp(logits[i] - maxLogit);
                    sum += probs[i];
                }

                for (int i = 0; i < probs.length; i++) {
                    probs[i] /= sum;
                }

                int maxIndex = 0;
                float maxProb = probs[0];
                for (int i = 1; i < probs.length; i++) {
                    if (probs[i] > maxProb) {
                        maxProb = probs[i];
                        maxIndex = i;
                    }
                }

                String predictedClass = CLASS_NAMES[maxIndex];
                float confidence = maxProb * 100;

                textResult.setText(" " + predictedClass);
                textTips.setText("Recommended Treatment: \n" + TREATMENT_MAP.get(predictedClass));
                textError.setVisibility(View.GONE);

                Log.d("DetectionResult", "Prediction: " + predictedClass + " (" + confidence + "%)");

                showChildInfoDialog(predictedClass, confidence, imageUriStr);

            } else {
                showError("Invalid model output.");
            }
        } else {
            showError("No result from model.");
        }
    }

    private void showChildInfoDialog(String predictedClass, float confidence, String imageUriStr) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_child_info, null);

        AutoCompleteTextView childNameInput = dialogView.findViewById(R.id.editTextChildName);
        TextView childAgeInput = dialogView.findViewById(R.id.editTextChildAge);
        Button confirmButton = dialogView.findViewById(R.id.buttonConfirmChildInfo);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.storage.FirebaseStorage storage = com.google.firebase.storage.FirebaseStorage.getInstance();

        List<String> childNames = new ArrayList<>();
        Map<String, String> childNameToAgeMap = new HashMap<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, childNames);
        childNameInput.setAdapter(adapter);

        // Fetch child names + ages
        db.collection("User").document(uid).collection("Child")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");

                        Object ageObj = doc.get("age");
                        String age;
                        if (ageObj instanceof Number) {
                            age = String.valueOf(((Number) ageObj).intValue());
                        } else if (ageObj instanceof String) {
                            age = (String) ageObj;
                        } else {
                            age = "Unknown";
                        }

                        if (name != null) {
                            childNames.add(name);
                            if (age != null) {
                                childNameToAgeMap.put(name, age);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });

        childNameInput.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = parent.getItemAtPosition(position).toString();
            String age = childNameToAgeMap.get(selectedName);
            if (age != null) {
                childAgeInput.setText(age);
            }
        });

        confirmButton.setOnClickListener(v -> {
            String childName = childNameInput.getText().toString().trim();
            String childAge = childAgeInput.getText().toString().trim();

            if (childName.isEmpty() || childAge.isEmpty()) {
                Toast.makeText(getContext(), "Please enter both name and age.", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(childAge);
                if (age < 4 || age > 12) {
                    Toast.makeText(getContext(), "Age must be between 4 and 12.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid numeric age.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> childData = new HashMap<>();
            childData.put("name", childName);
            childData.put("age", age);

            db.collection("User").document(uid)
                    .collection("Child").document(childName)
                    .set(childData)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Child info saved"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error saving child info", e));

            Uri localImageUri = Uri.parse(imageUriStr);
            String imagePath = "images/" + uid + "/" + System.currentTimeMillis() + ".jpg";
            com.google.firebase.storage.StorageReference imageRef = storage.getReference().child(imagePath);

            imageRef.putFile(localImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Map<String, Object> resultData = new HashMap<>();
                        resultData.put("childName", childName);
                        resultData.put("childAge", childAge);
                        resultData.put("prediction", predictedClass);
                        resultData.put("confidence", confidence);
                        resultData.put("treatment", TREATMENT_MAP.get(predictedClass));
                        resultData.put("imageUrl", downloadUri.toString());
                        resultData.put("timestamp", com.google.firebase.Timestamp.now());

                        db.collection("User").document(uid)
                                .collection("Detection")
                                .add(resultData)
                                .addOnSuccessListener(docRef -> Log.d("Firestore", "Detection result saved with image"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error saving detection result", e));
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("Storage", "Image upload failed", e);
                        Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    });

            dialog.dismiss();
        });

        dialog.show();
    }

    private void showError(String message) {
        textResult.setVisibility(View.GONE);
        textTips.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
        textError.setText("Error: " + message);
    }
}