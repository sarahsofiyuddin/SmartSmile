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
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DetectionPreviewFragment extends Fragment {

    private static final String TAG = "DetectionPreviewFragment";

    private Interpreter tflite;
    private ImageView imageView;
    private Button buttonSubmit, buttonReupload;
    private Uri imageUri;

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

        // Load model
        try {
            tflite = new Interpreter(loadModelFile());
            Log.d(TAG, "TFLite model loaded successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model", e);
        }

        if (getArguments() != null && getArguments().containsKey("imageUri")) {
            imageUri = getArguments().getParcelable("imageUri");
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getContext().getAssets().openFd("model_resnet.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_preview, container, false);

        imageView = view.findViewById(R.id.imageViewPreview);
        buttonSubmit = view.findViewById(R.id.button_submit);
        buttonReupload = view.findViewById(R.id.button_reupload);

        if (imageUri != null) {
            imageView.setImageURI(imageUri);
        }

        buttonSubmit.setOnClickListener(v -> runModelAndShowResult());
        buttonReupload.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void runModelAndShowResult() {
        float[] results = runInference(imageUri);
        if (results == null) return;

        DetectionResultFragment resultFragment = new DetectionResultFragment();
        Bundle bundle = new Bundle();
        bundle.putFloatArray("inferenceResults", results);
        bundle.putString("imageUri", imageUri.toString());
        resultFragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout_detection, resultFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private float[] runInference(Uri imageUri) {
        try {
            InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

            TensorImage tensorImage = new TensorImage();
            tensorImage.load(bitmap);

            ImageProcessor imageProcessor = new ImageProcessor.Builder()
                    .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                    .add(new NormalizeOp(127.5f, 127.5f)) // Normalize to [-1, 1]
                    .build();

            tensorImage = imageProcessor.process(tensorImage);

            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CLASSES}, org.tensorflow.lite.DataType.FLOAT32);

            tflite.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

            return outputBuffer.getFloatArray();
        } catch (Exception e) {
            Log.e(TAG, "Error during inference", e);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }
}