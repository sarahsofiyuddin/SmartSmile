package com.example.smartsmile;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

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

    private TextView textViewName, textViewEmail, textViewPhone;
    private CardView cardUpdatePassword, cardUpdateEmail, cardUpdatePhone, cardLogout;
    private FirebaseAuth auth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

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

        cardUpdatePassword = view.findViewById(R.id.cardUpdatePassword);
        cardUpdateEmail = view.findViewById(R.id.cardUpdateEmail);
        cardUpdatePhone = view.findViewById(R.id.cardUpdatePhone);
        cardLogout = view.findViewById(R.id.cardLogout);

        auth = FirebaseAuth.getInstance();

        loadUserInfo();
        setupCardClickListeners();

        return view;
    }
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String phone = user.getPhoneNumber();

            textViewName.setText(name != null ? name : "Name not set");
            textViewEmail.setText(email != null ? email : "Email not set");
            textViewPhone.setText(phone != null ? phone : "Phone not set");
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

        EditText editOld = dialogView.findViewById(R.id.editTextOldValue);
        EditText editNew = dialogView.findViewById(R.id.editTextNewValue);
        EditText editConfirm = dialogView.findViewById(R.id.editTextConfirmValue);
        Button btnUpdate = dialogView.findViewById(R.id.buttonUpdate);

        builder.setTitle(type.equals("password") ? "Update Password" : "Update Email");

        AlertDialog dialog = builder.create();

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

            // Re-authenticate the user
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), old);
            user.reauthenticate(credential).addOnSuccessListener(unused -> {
                // Now safe to update email or password
                if (type.equals("email")) {
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

        builder.setTitle("Update Phone Number");

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