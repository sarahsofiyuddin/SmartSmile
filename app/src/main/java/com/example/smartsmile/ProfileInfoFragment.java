package com.example.smartsmile;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

    public ProfileInfoFragment() {
        // Required empty public constructor
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
                        Toast.makeText(getContext(), "Update Password clicked", Toast.LENGTH_SHORT).show()
                // You can start a new fragment or activity here
        );

        cardUpdateEmail.setOnClickListener(v ->
                Toast.makeText(getContext(), "Update Email clicked", Toast.LENGTH_SHORT).show()
        );

        cardUpdatePhone.setOnClickListener(v ->
                Toast.makeText(getContext(), "Update Phone Number clicked", Toast.LENGTH_SHORT).show()
        );

        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            // Optional: navigate back to login screen
            startActivity(new Intent(getActivity(), SignInActivity.class));
            getActivity().finish();
        });
    }
}