package com.example.majortask.Signing;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.majortask.Driver.DriverMainActivity;
import com.example.majortask.Rider.MainActivity;
import com.example.majortask.R;
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class SignUpFragment extends Fragment {
    SharedPreferences sharedPreferences;
    private FirebaseHelper firebaseHelper;
    private FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override // In case the user was already signed-in
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("loggedUser", currentUser.getUid());
            editor.apply();
            firebaseHelper.checkIfRiderOrDriver(currentUser.getUid(), new FirebaseHelper.RiderOrDriverCallback() {
                @Override
                public void isRider(boolean rider) {
                    if(rider){
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                }
                @Override
                public void isDriver(boolean driver) {
                    if(driver){
                        Intent intent = new Intent(requireActivity(), DriverMainActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                }
                @Override
                public void isRiderOrDriverFetchError(String errorMessage) {
                    Toast.makeText(requireContext(), "User not registered in either driver or rider",
                            Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = new FirebaseHelper();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseApp.initializeApp(requireContext()); // Initialize Firebase if not already initialized
        // Now you can use Firestore within this Fragment
        db = FirebaseFirestore.getInstance();
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainPageFrame, new SignInFragment());
                fragmentTransaction.commit();
            }
        });

        binding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.signupButton.setEnabled(false);

                String firstName, lastName, email, password, phoneNo;

                firstName = String.valueOf(binding.firstName.getText());
                lastName = String.valueOf(binding.lastName.getText());
                email = String.valueOf(binding.username.getText());
                password = String.valueOf(binding.password.getText());
                phoneNo = String.valueOf(binding.phone.getText());

                // Check all the fields are not empty
                if (TextUtils.isEmpty(firstName)){
                    Toast.makeText(requireContext(), "Please provide a firstname", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(lastName)){
                    Toast.makeText(requireContext(), "Please provide a lastname", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(requireContext(), "Please provide an email", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(requireContext(), "Please provide a password", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (password.length() < 8 ){
                    Toast.makeText(requireContext(), "Min accepted password length is 8 characters", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(phoneNo)){
                    Toast.makeText(requireContext(), "Please provide a phone number", Toast.LENGTH_SHORT).show();
                    binding.signupButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 1- Add the user to firestore file
                                    Map<String, Object> newData = new HashMap<>();
                                    newData.put("Email", email);
                                    newData.put("FirstName", firstName);
                                    newData.put("LastName", lastName);
                                    newData.put("Phone", phoneNo);

                                    if (binding.radioRider.isChecked()){
                                        addUserToFirestoreUserFile("RIDER",mAuth.getCurrentUser(),newData);
                                    } else if (binding.radioDriver.isChecked()) {
                                        addUserToFirestoreUserFile("DRIVER",mAuth.getCurrentUser(),newData);
                                    }
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    // go to the home signin screen
                                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.mainPageFrame, new SignInFragment());
                                    fragmentTransaction.commit();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    binding.signupButton.setEnabled(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(requireContext(), "Couldn't Create User",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
    }

    void addUserToFirestoreUserFile(String documentPath, FirebaseUser user, Map<String, Object> userData){
        db.collection("USERS")
                .document(documentPath)
                .collection("root")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(a->{ Log.d(TAG, "Added user to firestore successfully");})
                .addOnFailureListener(e->{ Log.d(TAG, "Failed to add user to firestore");});
    }
}