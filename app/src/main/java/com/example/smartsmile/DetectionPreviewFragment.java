package com.example.smartsmile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

public class DetectionPreviewFragment extends Fragment {

    private static final String TAG = "DetectionPreviewFragment";

    private ImageView imageView;
    private Button buttonSubmit, buttonReupload;
    private Uri imageUri;
    private Bitmap selectedBitmap;

    private static final int IMG_SIZE = 224;
    private static final int NUM_CLASSES = 4;

    public DetectionPreviewFragment() {

    }

    public static DetectionPreviewFragment newInstance(String param1, String param2) {
        DetectionPreviewFragment fragment = new DetectionPreviewFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("imageUri")) {
            imageUri = getArguments().getParcelable("imageUri");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_preview, container, false);

        imageView = view.findViewById(R.id.imageViewPreview);
        buttonSubmit = view.findViewById(R.id.button_submit);
        buttonReupload = view.findViewById(R.id.button_reupload);

        if (imageUri != null) {
            imageView.setImageURI(imageUri);
            try {
                InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                selectedBitmap = BitmapFactory.decodeStream(imageStream);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
            }
        }

        buttonSubmit.setOnClickListener(v -> runModelAndShowResult());
        buttonReupload.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void runModelAndShowResult() {
        if (selectedBitmap == null) {
            Toast.makeText(getContext(), "Please upload or capture an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DentalDiseaseClassifier classifier = new DentalDiseaseClassifier(getContext());

            // Early check: is image blank or unclear?
            if (classifier.classify(selectedBitmap).equals("ERROR_BLANK_IMAGE")) {
                Toast.makeText(getContext(), "The image is too dark or unclear. Please reupload or recapture.", Toast.LENGTH_LONG).show();
                return;
            }

            // Now get class probabilities
            float[] probs = classifier.getClassProbabilities(selectedBitmap);
            if (probs == null) {
                Toast.makeText(getContext(), "Model failed to analyze image. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navigate to result fragment
            DetectionResultFragment resultFragment = new DetectionResultFragment();
            Bundle bundle = new Bundle();
            bundle.putFloatArray("inferenceResults", probs);
            bundle.putString("imageUri", imageUri.toString());
            resultFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout_detection, resultFragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception e) {
            Log.e(TAG, "Model error", e);
            Toast.makeText(getContext(), "An error occurred while analyzing the image.", Toast.LENGTH_SHORT).show();
        }
    }
}