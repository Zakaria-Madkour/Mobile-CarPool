package com.example.majortask.Driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.majortask.R;
import com.example.majortask.Common.CartFragment;
import com.example.majortask.Common.ProfileFragment;
import com.example.majortask.databinding.ActivityDriverMainBinding;

public class DriverMainActivity extends AppCompatActivity {
    ActivityDriverMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new DriverMainFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.home){
                replaceFragment(new DriverMainFragment());
            } else if (item.getItemId() == R.id.requests) {
                replaceFragment(new CartFragment("DRIVER"));
            } else if (item.getItemId() == R.id.add) {
                replaceFragment(new AddRideFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Set custom animations for enter and exit
//        fragmentTransaction.setCustomAnimations(R.anim.slide_out_left, R.anim.slide_in_right);

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}