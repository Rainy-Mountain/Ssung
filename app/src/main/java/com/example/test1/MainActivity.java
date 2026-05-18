package com.example.test1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위치 권한 허용
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        NavigationBarView navigationBarView = findViewById(R.id.bottom_navigation);

        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    transfer(Fragment1.newInstance("param1", "param2"));
                    return true;
                } else if (itemId == R.id.setting) {
                    transfer(Fragment2.newInstance("param1", "param2"));
                    return true;
                } else if (itemId == R.id.info) {
                    transfer(Fragment3.newInstance("param1", "param2"));
                    //startActivity(new Intent(MainActivity.this, PedometerActivity.class));
                    return true;
                } else if (itemId == R.id.image) {
                    transfer(Fragment4.newInstance("param1", "param2"));
                    return true;
                }
                return false;
            }
        });

        transfer(Fragment1.newInstance("param1", "param2"));

    }

    public void transfer(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containers, fragment)
                .commit();
    }
}









