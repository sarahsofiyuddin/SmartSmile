package com.example.smartsmile;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set default fragment to DetectionFragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Set default selected item
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if(itemId == R.id.nav_home){
                    loadFragment(new HomeFragment(), false);

                } else if (itemId == R.id.nav_info){
                    loadFragment(new InfoFragment(), false);

                } else if (itemId == R.id.nav_detection){
                    loadFragment(new DetectionFragment(), false);

                } else if (itemId == R.id.nav_report){
                    loadFragment(new ReportFragment(), false);

                } else { //nav_profile
                    loadFragment(new ProfileFragment(), false);

                }

                return true;

            }
        });

    }

    private void loadFragment(Fragment fragment, boolean isAppInitialized){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isAppInitialized){
            fragmentTransaction.add(R.id.frameLayout, fragment);

        } else {
            fragmentTransaction.replace(R.id.frameLayout, fragment);

        }

        fragmentTransaction.commit();
    }
}