package com.example.smartsmile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private ImageView eyeIcon;
    Button googleSignInButton;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001; // Google Sign In
    private GoogleSignInClient mGoogleSignInClient; //Google authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        // Set window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signIn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editText_email);
        editTextPassword = findViewById(R.id.editText_password);
        buttonSignIn = findViewById(R.id.button_signin);
        eyeIcon = findViewById(R.id.eye_icon);

        // Set up the click listener for the eye icon to toggle password visibility
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        mAuth = FirebaseAuth.getInstance();

        buttonSignIn.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String UserId = user.getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference userRef = db.collection("User").document(UserId);

                            userRef.get().addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("email", user.getEmail());
                                    userData.put("name", user.getDisplayName()); // Optional
                                    userData.put("phone", user.getPhoneNumber()); // Optional
                                    userData.put("created_at", FieldValue.serverTimestamp());

                                    userRef.set(userData, SetOptions.merge());
                                }
                            });

                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        TextView forgotPasswordText = findViewById(R.id.login_forgotpassword);

        forgotPasswordText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this, R.style.RoundedDialogTheme);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.forgot_password, null);
            builder.setView(dialogView);

            EditText emailInput = dialogView.findViewById(R.id.editTextResetEmail);

            builder.setPositiveButton("Reset", (dialog, which) -> {
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(SignInActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(SignInActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();

            // Customize button colors (optional)
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black));
        });

        TextView redirectText = findViewById(R.id.signIn_redirecttext);

        // Set partial underline using HTML
        redirectText.setText(android.text.Html.fromHtml("Not yet registered? <u>Sign Up</u>"));

        // Handle click
        redirectText.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        googleSignInButton = findViewById(R.id.google_signIn);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.google_icon);
        if (drawable != null) {
            int iconSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            drawable.setBounds(0, 0, iconSize, iconSize);
            googleSignInButton.setCompoundDrawables(drawable, null, null, null);
        }

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(v -> signIn());

        Button phoneButton = findViewById(R.id.signup_phone);
        phoneButton.setOnClickListener(v -> showPhoneAuthDialog());

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

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String UserId = user.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("User").document(UserId);

                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", user.getEmail());
                                userData.put("name", user.getDisplayName()); // Optional
                                userData.put("phone", user.getPhoneNumber()); // Optional
                                userData.put("created_at", FieldValue.serverTimestamp());

                                userRef.set(userData, SetOptions.merge());
                            }
                        });

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignInActivity.this, "Firebase Auth failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPhoneAuthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.RoundedDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.phone_auth, null);
        builder.setView(view);

        EditText phoneInput = view.findViewById(R.id.editTextPhone);
        EditText otpInput = view.findViewById(R.id.editTextOTP);
        Button sendOTPBtn = view.findViewById(R.id.buttonSendOTP);
        Button verifyBtn = view.findViewById(R.id.buttonVerifyOTP);

        // Initially hide OTP input and Verify button
        otpInput.setVisibility(View.GONE);
        verifyBtn.setVisibility(View.GONE);

        AlertDialog dialog = builder.create();
        dialog.show();

        final String[] verificationId = new String[1];

        sendOTPBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            if (phone.isEmpty()) {
                phoneInput.setError("Enter phone number");
                return;
            }

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            signInWithPhoneAuthCredential(credential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(SignInActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                            verificationId[0] = id;
                            Toast.makeText(SignInActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();

                            // Show OTP input and Verify button
                            otpInput.setVisibility(View.VISIBLE);
                            verifyBtn.setVisibility(View.VISIBLE);
                        }
                    })
                    .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        verifyBtn.setOnClickListener(v -> {
            String code = otpInput.getText().toString().trim();
            if (code.isEmpty()) {
                otpInput.setError("Enter OTP");
                return;
            }

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId[0], code);
            signInWithPhoneAuthCredential(credential);
            dialog.dismiss();
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String UserId = user.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userRef = db.collection("User").document(UserId);

                        userRef.get().addOnSuccessListener(documentSnapshot -> {
                            if (!documentSnapshot.exists()) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", user.getEmail());
                                userData.put("name", user.getDisplayName()); // Optional
                                userData.put("phone", user.getPhoneNumber()); // Optional
                                userData.put("created_at", FieldValue.serverTimestamp());

                                userRef.set(userData, SetOptions.merge());
                            }
                        });

                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
