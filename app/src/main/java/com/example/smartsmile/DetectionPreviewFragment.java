package com.example.smartsmile;

import android.content.res.AssetFileDescriptor;
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

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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

    private static final String TAG = "DetectionPreviewFragment";

    private Interpreter tflite;

    private ImageView imageView;
    private Button buttonSubmit, buttonReupload;
    private Uri imageUri;

    // Model input image size for ResNet (usually 224x224)
    private static final int IMG_SIZE = 224;

    // Number of output classes
    private static final int NUM_CLASSES = 4;

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

        // Load the TFLite model
        try {
            tflite = new Interpreter(loadModelFile());
            Log.d(TAG, "TFLite model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model", e);
        }

        // Get the image URI passed as argument
        if (getArguments() != null && getArguments().containsKey("imageUri")) {
            imageUri = getArguments().getParcelable("imageUri");
            Log.d(TAG, "Received image URI: " + imageUri);
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getContext().getAssets().openFd("model_resnet.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detection_preview, container, false);

        imageView = view.findViewById(R.id.imageViewPreview);
        buttonSubmit = view.findViewById(R.id.button_submit);
        buttonReupload = view.findViewById(R.id.button_reupload);

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
            if (imageUri != null) {
                // Run inference on the image
                float[] results = runInference(imageUri);
                if (results != null) {
                    // Pass results to DetectionResultFragment
                    DetectionResultFragment resultFragment = new DetectionResultFragment();

                    Bundle bundle = new Bundle();
                    // You can name these keys as you want; here we send probabilities per class
                    bundle.putFloatArray("inferenceResults", results);

                    resultFragment.setArguments(bundle);

                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.frameLayout_detection, resultFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        // Reupload button - Go back to DetectionScan Fragment
        buttonReupload.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;

    }

    // Preprocess and run inference
    private float[] runInference(Uri imageUri) {
        try {
            // Load bitmap from URI
            InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            // Convert to RGB if needed
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            // Resize and normalize bitmap
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);
            float[][][][] input = preprocessBitmap(resized);

            // Output buffer: 1 example, NUM_CLASSES probabilities
            float[][] output = new float[1][NUM_CLASSES];

            // Run inference
            tflite.run(input, output);

            Log.d(TAG, "Inference results: " + arrayToString(output[0]));

            return output[0];
        } catch (Exception e) {
            Log.e(TAG, "Error during inference", e);
            return null;
        }
    }

    // Convert bitmap to float input tensor with shape [1,224,224,3]
    private float[][][][] preprocessBitmap(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);

        int width = resizedBitmap.getWidth();
        int height = resizedBitmap.getHeight();

        float[][][][] input = new float[1][IMG_SIZE][IMG_SIZE][3]; // NHWC

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = resizedBitmap.getPixel(x, y);

                // Normalize RGB to [0, 1] like in Colab
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f; // Red
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;  // Green
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;         // Blue
            }
        }

        return input;
    }

    private String arrayToString(float[] arr) {
        StringBuilder sb = new StringBuilder();
        for (float v : arr) {
            sb.append(String.format("%.4f", v)).append(" ");
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
            Log.d(TAG, "TFLite interpreter closed");
        }
    }

}