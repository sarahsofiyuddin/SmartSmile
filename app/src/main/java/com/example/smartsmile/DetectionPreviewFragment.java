package com.example.smartsmile;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetectionPreviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetectionPreviewFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DetectionPreviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetectionPreviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetectionPreviewFragment newInstance(String param1, String param2) {
        DetectionPreviewFragment fragment = new DetectionPreviewFragment();
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

        View view = inflater.inflate(R.layout.fragment_detection_preview, container, false);

        ImageView imageView = view.findViewById(R.id.imageViewPreview);
        Button buttonSubmit = view.findViewById(R.id.button_submit);
        Button buttonReupload = view.findViewById(R.id.button_reupload);

        // Log if we received the image URI
        if (getArguments() != null && getArguments().containsKey("imageUri")) {
            Uri imageUri = getArguments().getParcelable("imageUri");
            Log.d("DetectionPreviewFragment", "Received Image URI: " + imageUri);

            if (imageUri != null) {
                imageView.setImageURI(imageUri);
            } else {
                Log.e("DetectionPreviewFragment", "Image URI is null!");
            }
        } else {
            Log.e("DetectionPreviewFragment", "No image URI received!");
        }

        // Submit button - Navigate to DetectionResultFragment
        buttonSubmit.setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout_detection, new DetectionResultFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Reupload button - Go back to DetectionScan Fragment
        buttonReupload.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;

    }
}