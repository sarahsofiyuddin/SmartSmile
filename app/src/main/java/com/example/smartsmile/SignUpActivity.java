package com.example.smartsmile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class  SignUpActivity extends AppCompatActivity {

    private EditText editText_name, editText_email, editText_password, editText_confirmPassword;
    private Button buttonSignUp;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private ImageView eyeIconPassword, eyeIconConfirmPassword;
    private Button googleSignUpButton;
    private Button phoneButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_UP = 9002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText_name = (EditText) findViewById(R.id.editText_name);
        editText_email = (EditText) findViewById(R.id.editText_email);
        editText_password = (EditText) findViewById(R.id.editText_password);
        editText_confirmPassword = (EditText) findViewById(R.id.editText_confirmPassword);
        eyeIconPassword = findViewById(R.id.eye_icon_password);
        eyeIconConfirmPassword = findViewById(R.id.eye_icon_confirm_password);

        // Password Eye Toggle
        eyeIconPassword.setOnClickListener(v -> {
            if (editText_password.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                editText_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconPassword.setImageResource(R.drawable.eye_on_icon); // <-- Use your open-eye icon here
            } else {
                // Hide password
                editText_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconPassword.setImageResource(R.drawable.eye_off_icon);
            }
            // Move cursor to the end
            editText_password.setSelection(editText_password.getText().length());
        });

        // Confirm Password Eye Toggle
        eyeIconConfirmPassword.setOnClickListener(v -> {
            if (editText_confirmPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editText_confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                eyeIconConfirmPassword.setImageResource(R.drawable.eye_on_icon);
            } else {
                editText_confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeIconConfirmPassword.setImageResource(R.drawable.eye_off_icon);
            }
            editText_confirmPassword.setSelection(editText_confirmPassword.getText().length());
        });

        buttonSignUp = (Button) findViewById(R.id.buttonSignUp);

        progressDialog = new ProgressDialog(this);

        TextView redirectText = findViewById(R.id.signUp_redirecttext);

        mAuth = FirebaseAuth.getInstance();

        buttonSignUp.setOnClickListener(v -> {
            String name = editText_name.getText().toString().trim();
            String email = editText_email.getText().toString().trim();
            String password = editText_password.getText().toString();
            String confirmPassword = editText_confirmPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Registering...");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            if (user != null) {
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileUpdateTask -> {
                                            if (profileUpdateTask.isSuccessful()) {
                                                saveUserToFirestore(user.getUid(), name, email, "email");
                                                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, SignInActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                        } else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        });

        redirectText.setText(android.text.Html.fromHtml("Already have an account? <u>Sign In</u>"));

        redirectText.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        googleSignUpButton = findViewById(R.id.google_signUp);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.google_icon);
        if (drawable != null) {
            int iconSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            drawable.setBounds(0, 0, iconSize, iconSize);
            googleSignUpButton.setCompoundDrawables(drawable, null, null, null);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Handle Google Sign Up
        googleSignUpButton.setOnClickListener(v -> signUpWithGoogle());

        phoneButton = findViewById(R.id.signup_phone);

        // Handle Phone Sign Up
        phoneButton.setOnClickListener(v -> showPhoneAuthDialog());

    }

    private void saveUserToFirestore(String uid, String name, String email, String provider) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("provider", provider);
        userMap.put("createdAt", FieldValue.serverTimestamp());

        db.collection("User").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(SignUpActivity.this, "User profile saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpActivity.this, "Failed to save user profile", Toast.LENGTH_SHORT).show());
    }

    private void signUpWithGoogle() {
        Intent signUpIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signUpIntent, RC_SIGN_UP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_UP) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseGoogleSignUp(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-Up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseGoogleSignUp(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), user.getDisplayName(), user.getEmail(), "google");
                        }
                        Toast.makeText(SignUpActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Firebase Google Sign-Up failed", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SignUpActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(String id, PhoneAuthProvider.ForceResendingToken token) {
                            verificationId[0] = id;
                            Toast.makeText(SignUpActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();

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
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), user.getPhoneNumber(), "", "phone");
                        }
                        Toast.makeText(this, "Phone sign-up successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(this, "Phone sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}