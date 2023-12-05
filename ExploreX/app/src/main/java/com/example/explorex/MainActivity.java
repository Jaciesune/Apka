package com.example.explorex;

import com.example.explorex.TrasyFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.explorex.databinding.ActivityMainBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements MapaFragment.OnRouteCompleteListener {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    Button button;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.Mapa_navbar) {
                MapaFragment mapaFragment = new MapaFragment();
                replaceFragment(mapaFragment, "MapaFragmentTag");
            } else if (item.getItemId() == R.id.Trasy_navbar) {
                // Create an instance of TrasyFragment and replace the current fragment
                TrasyFragment trasyFragment = new TrasyFragment();
                replaceFragment(trasyFragment, "TrasyFragmentTag");
            } else {
                UzytkownikFragment uzytkownikFragment = new UzytkownikFragment();
                replaceFragment(uzytkownikFragment, "UzytkownikFragmentTag");
            }
            return true;
        });
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout_button);

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // Do something if the user is logged in
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Menu
    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment, tag);
        fragmentTransaction.commit();
    }


    @Override
    public void onRouteComplete(String routeInfo) {
        // Assuming TrasyFragment is the fragment you want to communicate with
        TrasyFragment trasyFragment = findTrasyFragment();
        if (trasyFragment != null) {
            trasyFragment.setOnRouteCompleteListener(this);
        }
    }
    private TrasyFragment findTrasyFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return (TrasyFragment) fragmentManager.findFragmentByTag("TrasyFragmentTag");
    }

}