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
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.Utils.Person;
import com.example.majortask.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseHelper firebaseHelper;
    SharedPreferences sharedPreferences;



    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        firebaseHelper = new FirebaseHelper();
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

        firebaseHelper.retrievePersonById(mAuth.getUid(), new FirebaseHelper.retrievePersonCallback() {
            @Override
            public void retrievedPersonData(Person person) {
                binding.firstName.setText(person.getFirstName());
                binding.lastName.setText(person.getLastName());
                binding.email.setText(person.getEmail());
                binding.phoneNumber.setText(person.getPhone());
                binding.accountType.setText(person.getType());
            }

            @Override
            public void networkConnectionError(String errorMessage) {

            }
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