package com.example.majortask.Rider;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.majortask.R;
import com.example.majortask.Signing.SigningActivity;
import com.example.majortask.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db.collection("USERS")
                .document("DRIVER")
                .collection("root")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve its data
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        String email = documentSnapshot.getString("Email");
                        String phone = documentSnapshot.getString("Phone");
                        binding.firstName.setText(firstName);
                        binding.lastName.setText(lastName);
                        binding.email.setText(email);
                        binding.phoneNumber.setText(phone);
                        binding.accountType.setText("DRIVER");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.v("demo101","Failed to retrieve user data from the cloud!"+e.toString());
                    Toast.makeText(requireContext(), "Couldn't get user data from cloud",
                            Toast.LENGTH_SHORT).show();
                });
        db.collection("USERS")
                .document("RIDER")
                .collection("root")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve its data
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        String email = documentSnapshot.getString("Email");
                        String phone = documentSnapshot.getString("Phone");
                        binding.firstName.setText(firstName);
                        binding.lastName.setText(lastName);
                        binding.email.setText(email);
                        binding.phoneNumber.setText(phone);
                        binding.accountType.setText("RIDER");
                    }
                })
                .addOnFailureListener(ee -> {
                    // Handle any errors that may occur while fetching data
                    Log.v("demo101","Failed to retrieve user data from the cloud!"+ee.toString());
                    Toast.makeText(requireContext(), "Couldn't get user data from cloud",
                            Toast.LENGTH_SHORT).show();
                });
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Delete a value
                editor.remove("loggedUser");
                editor.apply();
                Intent intent = new Intent(requireActivity(), SigningActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }
}