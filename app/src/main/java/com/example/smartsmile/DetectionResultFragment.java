package com.example.smartsmile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetectionResultFragment extends Fragment {

    private static final String[] CLASS_NAMES = {
            "Dental Calculus",
            "Dental Caries",
            "Gingivitis",
            "Hypodontia"
    };

    private TextView textResult;
    private TextView textConfidence;
    private TextView textError;

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
        textConfidence = view.findViewById(R.id.text_confidence);
        textError = view.findViewById(R.id.text_error);

        displayResult();

        return view;
    }

    private void displayResult() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("inferenceResults")) {
            float[] logits = args.getFloatArray("inferenceResults");

            if (logits != null && logits.length == CLASS_NAMES.length) {

                // Log raw output
                for (int i = 0; i < logits.length; i++) {
                    Log.d("DetectionResult", "Raw Logit [" + i + "] (" + CLASS_NAMES[i] + "): " + logits[i]);
                }

                // Apply softmax manually
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
                    Log.d("DetectionResult", "Softmax Prob [" + i + "] (" + CLASS_NAMES[i] + "): " + probs[i]);
                }

                // Get max prediction
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

                textResult.setText("Predicted: " + predictedClass);
                textConfidence.setText(String.format("Confidence: %.2f%%", confidence));
                textError.setVisibility(View.GONE);

                Log.d("DetectionResult", "Final prediction: " + predictedClass + " (" + confidence + "%)");
            } else {
                showError("Invalid model output.");
            }
        } else {
            showError("Model failed to produce a result.");
        }
    }

    private void showError(String message) {
        textResult.setVisibility(View.GONE);
        textConfidence.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
        textError.setText("Error: " + message);
    }
}
