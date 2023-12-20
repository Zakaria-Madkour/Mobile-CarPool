package com.example.majortask.Signing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.majortask.Driver.DriverMainActivity;
import com.example.majortask.Rider.MainActivity;
import com.example.majortask.R;
import com.example.majortask.Utils.FirebaseHelper;
import com.example.majortask.databinding.FragmentSignInBinding;
import com.example.majortask.databinding.FragmentSignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignInFragment extends Fragment {
    SharedPreferences sharedPreferences;
    FragmentSignInBinding binding;
    private FirebaseAuth mAuth;
    FirebaseHelper firebaseHelper;

    public SignInFragment() {
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
        firebaseHelper = new FirebaseHelper();
        sharedPreferences =  requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Change the color of signup
        String fullText = binding.signupText.getText().toString();
        SpannableString spannableString = new SpannableString(fullText);
        int color = Color.BLUE; // Change to your desired color
        spannableString.setSpan(new ForegroundColorSpan(color), 20, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), 20, 27, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.signupText.setText(spannableString);


        binding.signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainPageFrame, new SignUpFragment());
                fragmentTransaction.commit();
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.loginButton.setEnabled(false);
                // 1.  validate username and password
                String email, password;
                email = String.valueOf(binding.username.getText());
                password = String.valueOf(binding.password.getText());
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(requireContext(), "Please provide an email", Toast.LENGTH_SHORT).show();
                    binding.loginButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    Toast.makeText(requireContext(), "Please provide a password", Toast.LENGTH_SHORT).show();
                    binding.loginButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    return;
                }

                //2. Authenticate the username with the password
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("loggedUser", user.getUid());
                                    editor.apply();
                                    firebaseHelper.checkIfRiderOrDriver(user.getUid(), new FirebaseHelper.RiderOrDriverCallback() {
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
                                } else {
                                    // If sign in fails, display a message to the user.
                                    binding.loginButton.setEnabled(true);
                                    binding.progressBar.setVisibility(View.GONE);
                                    Toast.makeText(requireContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }
}