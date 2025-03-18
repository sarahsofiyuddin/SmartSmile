package com.example.smartsmile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 🟢 Initialize cameraLauncher in onCreate()
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (imageUri != null) {
                            navigateToPreviewFragment(imageUri);
                        } else {
                            Log.e("DetectionScanFragment", "Image URI is null after capture!");
                        }
                    } else {
                        Log.e("DetectionScanFragment", "Failed to capture image");
                    }
                }
        );
    }

    private Uri imageUri; // Store the image URI

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
                        imageUri = result.getData().getData();
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
                        if (imageUri != null) {
                            navigateToPreviewFragment(imageUri);
                        }
                    } else {
                        Log.e("DetectionScanFragment", "Failed to capture image");
                    }
                }
        );

        buttonCapture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1002);
            } else {
                openCamera();
            }
        });


        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.e("DetectionScanFragment", "Camera permission denied!");
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                // 🔥 UPDATE GLOBAL imageUri
                imageUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.smartsmile.fileprovider", photoFile);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                // 🔥 USE cameraLauncher.launch()
                cameraLauncher.launch(cameraIntent);
            }
        } catch (IOException e) {
            Log.e("DetectionScanFragment", "Error creating image file", e);
        }
    }


    // Create a file for storing the captured image
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Ensure the directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File imageFile = new File(storageDir, imageFileName);
        return imageFile;
    }

    // Navigate to DetectionPreviewFragment with selected image
    private void navigateToPreviewFragment(Uri imageUri) {

        if (imageUri == null) {
            Log.e("DetectionScanFragment", "Image URI is null!");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("imageUri", imageUri);

        DetectionPreviewFragment previewFragment = new DetectionPreviewFragment();
        previewFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout_detection, previewFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}