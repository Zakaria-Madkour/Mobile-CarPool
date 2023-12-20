package com.example.majortask.Rider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.majortask.Common.CartFragment;
import com.example.majortask.Common.ProfileFragment;
import com.example.majortask.R;
import com.example.majortask.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home){
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.cart) {
                replaceFragment(new CartFragment("RIDER"));
            }else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }else if (item.getItemId() == R.id.history) {
                replaceFragment(new CartFragment("RIDER"));
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