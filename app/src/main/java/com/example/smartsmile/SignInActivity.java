package com.example.smartsmile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private ImageView eyeIcon; // To hold the eye icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // Initialize views
        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        buttonSignIn = findViewById(R.id.button_signin);
        eyeIcon = findViewById(R.id.eye_icon);

        // Set window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the click listener for the eye icon to toggle password visibility
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Set onClickListener for the Sign In button
        buttonSignIn.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Check if email and password are entered
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                // Call the sign-in API to verify the credentials
                signInUser(email, password);
            }
        });
    }

    // Toggle the password visibility
    private void togglePasswordVisibility() {
        int currentInputType = editTextPassword.getInputType();

        if (currentInputType == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show password
            editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye_on_icon); // Change to eye on icon
        } else {
            // Hide password
            editTextPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.eye_off_icon); // Change to eye off icon
        }

        // Ensure the cursor stays at the end after toggling visibility
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

    // Method to send a POST request to verify user credentials
    private void signInUser(String email, String password) {
        // Create a HashMap to hold the request parameters
        HashMap<String, String> params = new HashMap<>();
        params.put("UserEmail", email);
        params.put("UserPassword", password);

        // Make a network request to verify the credentials
        String url = "http://your_api_url.com/verifyUser.php"; // Replace with your actual API URL

        // Assuming you have a network library, for example, using Retrofit or Volley
        // Here, I'll simulate a network response
        mockApiRequest(url, params);
    }

    // Mock API request for demonstration purposes (Replace with actual network request logic)
    private void mockApiRequest(String url, HashMap<String, String> params) {
        // Simulating a successful login response from the server
        // You should replace this with actual network request logic

        String response = "{ \"error\": false, \"message\": \"Login successful!\" }"; // Mock response

        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean error = jsonResponse.getBoolean("error");
            if (!error) {
                // Login successful, navigate to MainActivity
                navigateToMainActivity();
            } else {
                // Show an error message
                String message = jsonResponse.getString("message");
                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(SignInActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Navigate to MainActivity upon successful login
    private void navigateToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // Close SignInActivity so the user cannot return to it by pressing back
    }
}
