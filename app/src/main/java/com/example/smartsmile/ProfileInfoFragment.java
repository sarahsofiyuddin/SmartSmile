package com.example.smartsmile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.app.Activity.RESULT_OK;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ProfileInfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int PICK_IMAGE_REQUEST = 1;
    private TextView textViewName, textViewEmail, textViewPhone;
    private CardView cardUpdatePassword, cardUpdateEmail, cardUpdatePhone, cardLogout;
    private ImageButton buttonEditProfileImage;
    private ShapeableImageView profileImageView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private Uri imageUri;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileInfoFragment newInstance(String param1, String param2) {
        ProfileInfoFragment fragment = new ProfileInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);

        textViewName = view.findViewById(R.id.textViewName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewPhone = view.findViewById(R.id.textViewPhone);
        profileImageView = view.findViewById(R.id.profile_image);
        buttonEditProfileImage = view.findViewById(R.id.buttonEditProfileImage);

        cardUpdatePassword = view.findViewById(R.id.cardUpdatePassword);
        cardUpdateEmail = view.findViewById(R.id.cardUpdateEmail);
        cardUpdatePhone = view.findViewById(R.id.cardUpdatePhone);
        cardLogout = view.findViewById(R.id.cardLogout);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        loadUserInfo();
        setupCardClickListeners();

        buttonEditProfileImage.setOnClickListener(v -> openFileChooser());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String uid = auth.getCurrentUser().getUid();
            StorageReference fileRef = storageRef.child(uid + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                            firestore.collection("User").document(uid)
                                    .update("profileImageUrl", uri.toString())
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile image updated", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save URL: " + e.getMessage(), Toast.LENGTH_SHORT).show())
                    )
            ).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String phone = user.getPhoneNumber();

            textViewName.setText(name != null ? name : "Name not set");
            textViewEmail.setText(email != null ? email : "Email not set");
            textViewPhone.setText(phone != null ? phone : "Phone not set");

            firestore.collection("User").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String imageUrl = documentSnapshot.getString("profileImageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(profileImageView);
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
    private void setupCardClickListeners() {
        cardUpdatePassword.setOnClickListener(v ->
                showUpdateDialog("Password"));

        cardUpdateEmail.setOnClickListener(v ->
                showUpdateDialog("Email"));

        cardUpdatePhone.setOnClickListener(v ->
                showPhoneDialog());

        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        });
    }
    private void showUpdateDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_info, null);
        builder.setView(dialogView);

        TextView title = dialogView.findViewById(R.id.textViewTitle);
        EditText editOld = dialogView.findViewById(R.id.editTextOldValue);
        EditText editNew = dialogView.findViewById(R.id.editTextNewValue);
        EditText editConfirm = dialogView.findViewById(R.id.editTextConfirmValue);
        Button btnUpdate = dialogView.findViewById(R.id.buttonUpdate);

        if (type.equalsIgnoreCase("email")) {
            title.setText("Update Email");
            editOld.setHint("Enter old email");
            editNew.setHint("Enter new email");
            editConfirm.setHint("Confirm new email");
            editOld.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            editNew.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            editConfirm.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            title.setText("Update Password");
            editOld.setHint("Enter old password");
            editNew.setHint("Enter new password");
            editConfirm.setHint("Confirm new password");
            editOld.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnUpdate.setOnClickListener(v -> {
            String old = editOld.getText().toString().trim();
            String newInfo = editNew.getText().toString().trim();
            String confirm = editConfirm.getText().toString().trim();

            if (old.isEmpty() || newInfo.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newInfo.equals(confirm)) {
                Toast.makeText(getContext(), "New and confirm do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || user.getEmail() == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), old);
            user.reauthenticate(credential).addOnSuccessListener(unused -> {
                if (type.equalsIgnoreCase("email")) {
                    user.updateEmail(newInfo)
                            .addOnSuccessListener(unused1 -> {
                                Toast.makeText(getContext(), "Email updated", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadUserInfo();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    user.updatePassword(newInfo)
                            .addOnSuccessListener(unused1 -> {
                                Toast.makeText(getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Re-authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void showPhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_phone, null);
        builder.setView(dialogView);

        EditText editPhone = dialogView.findViewById(R.id.editTextNewPhone);
        EditText editOTP = dialogView.findViewById(R.id.editTextOTP);
        Button btnSendOTP = dialogView.findViewById(R.id.buttonSendOTP);
        Button btnVerifyOTP = dialogView.findViewById(R.id.buttonVerifyOTP);

        AlertDialog dialog = builder.create();

        btnSendOTP.setOnClickListener(v -> {
            String phone = editPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(getContext(), "Enter phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                            editOTP.setText(credential.getSmsCode());
                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(getContext(), "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String id,
                                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
                            verificationId = id;
                            resendToken = token;
                            Toast.makeText(getContext(), "OTP sent", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        btnVerifyOTP.setOnClickListener(v -> {
            String otp = editOTP.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(getContext(), "Enter OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                user.updatePhoneNumber(credential)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Phone number updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadUserInfo();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to update phone: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
    }
}