package com.example.smartsmile;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DentalDiseaseClassifier {

    private final Interpreter tflite;
    private final List<String> labels;
    private final int inputSize = 224;

    public DentalDiseaseClassifier(Context context) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        tflite = new Interpreter(loadModelFile(context, "model_resnet.tflite"), options);
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context, String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    public String classify(Bitmap bitmap) {
        if (isBlankImage(bitmap)) {
            return "ERROR_BLANK_IMAGE";
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);

        float[][] output = new float[1][labels.size()];
        tflite.run(byteBuffer, output);

        int maxIndex = 0;
        float maxConfidence = output[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > maxConfidence) {
                maxConfidence = output[0][i];
                maxIndex = i;
            }
        }

        return labels.get(maxIndex);
    }

    public float[] getClassProbabilities(Bitmap bitmap) {
        if (isBlankImage(bitmap)) return null;

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(resizedBitmap);

        float[][] output = new float[1][labels.size()];
        tflite.run(byteBuffer, output);
        return output[0];
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int pixelValue : intValues) {
            int r = (pixelValue >> 16) & 0xFF;
            int g = (pixelValue >> 8) & 0xFF;
            int b = pixelValue & 0xFF;

            byteBuffer.putFloat(r / 255.0f);
            byteBuffer.putFloat(g / 255.0f);
            byteBuffer.putFloat(b / 255.0f);
        }

        return byteBuffer;
    }

    private boolean isBlankImage(Bitmap bitmap) {
        long total = 0;
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                int pixel = bitmap.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                total += r + g + b;
            }
        }
        long avg = total / ((width / 10) * (height / 10) * 3);
        return avg < 30; // too dark
    }
}