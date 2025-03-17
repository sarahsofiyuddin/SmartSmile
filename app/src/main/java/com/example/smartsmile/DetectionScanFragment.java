package com.example.smartsmile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetectionScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetectionScanFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DetectionScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetectionScanFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetectionScanFragment newInstance(String param1, String param2) {
        DetectionScanFragment fragment = new DetectionScanFragment();
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
        View view = inflater.inflate(R.layout.fragment_detection_scan, container, false);

        Button buttonUpload = view.findViewById(R.id.button_upload);
        Button buttonCapture = view.findViewById(R.id.button_capture);

        // Upload from gallery
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Log.d("DetectionScanFragment", "Gallery Image Selected: " + imageUri);

                        if (imageUri != null) {
                            navigateToPreviewFragment(imageUri);
                        }
                    } else {
                        Log.e("DetectionScanFragment", "Failed to select image from gallery");
                    }
                }
        );

        buttonUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // Capture from camera
        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri imageUri = result.getData().getData();
                        Log.d("DetectionScanFragment", "Camera Image Captured: " + imageUri);

                        if (imageUri != null) {
                            navigateToPreviewFragment(imageUri);
                        }
                    } else {
                        Log.e("DetectionScanFragment", "Failed to capture image");
                    }
                }
        );

        buttonCapture.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure a camera app is available
            if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                try {
                    File photoFile = createImageFile();
                    if (photoFile != null) {
                        Uri imageUri = FileProvider.getUriForFile(requireContext(),
                                "com.example.smartsmile.fileprovider", photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        cameraLauncher.launch(cameraIntent);
                    }
                } catch (IOException e) {
                    Log.e("DetectionScanFragment", "Error creating image file");
                }
            } else {
                Log.e("DetectionScanFragment", "No camera app available!");
            }
        });

        return view;
    }

    // Navigate to DetectionPreviewFragment with selected image
    private void navigateToPreviewFragment(Uri imageUri) {

        if (imageUri == null) {
            Log.e("DetectionScanFragment", "Image URI is null!");
            return;
        }


        Log.d("DetectionScanFragment", "Navigating to preview with image: " + imageUri.toString());

        Bundle bundle = new Bundle();
        bundle.putParcelable("imageUri", imageUri);

        DetectionPreviewFragment previewFragment = new DetectionPreviewFragment();
        previewFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout_detection, previewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Create a file for storing the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

}