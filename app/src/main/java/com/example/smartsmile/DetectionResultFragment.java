package com.example.smartsmile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetectionResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetectionResultFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String[] CLASS_NAMES = {
            "Dental Calculus",
            "Dental Caries",
            "Gingivitis",
            "Hypodontia"
    };

    private TextView textResult;
    private TextView textConfidence;
    private TextView textError;

    public DetectionResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetectionResultFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetectionResultFragment newInstance(String param1, String param2) {
        DetectionResultFragment fragment = new DetectionResultFragment();
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
            float[] results = args.getFloatArray("inferenceResults");

            if (results != null && results.length == CLASS_NAMES.length) {
                int maxIndex = 0;
                float maxValue = results[0];

                for (int i = 1; i < results.length; i++) {
                    if (results[i] > maxValue) {
                        maxValue = results[i];
                        maxIndex = i;
                    }
                }

                String predictedClass = CLASS_NAMES[maxIndex];
                float confidence = maxValue * 100;

                textResult.setText("Predicted: " + predictedClass);
                textConfidence.setText(String.format("Confidence: %.2f%%", confidence));
                textError.setVisibility(View.GONE);
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