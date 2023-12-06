package com.example.explorex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.explorex.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Menu
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Find the logout button
        logoutButton = findViewById(R.id.logout_button);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.Mapa_navbar) {
                replaceFragment(new MapaFragment());
                updateLogoutButtonVisibility(false);
            } else if (item.getItemId() == R.id.Trasy_navbar) {
                replaceFragment(new TrasyFragment());
                updateLogoutButtonVisibility(false);
            } else {
                replaceFragment(new UzytkownikFragment());
                updateLogoutButtonVisibility(true);
            }
            return true;
        });

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        // Initially hide the logout button
        updateLogoutButtonVisibility(false);
    }

    // Menu
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void updateLogoutButtonVisibility(boolean isVisible) {
        if (isVisible) {
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            logoutButton.setVisibility(View.GONE);
        }
    }
    public Button getLogoutButton() {
        return logoutButton;
    }
}