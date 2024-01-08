package com.example.explorex;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.explorex.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    private Button logoutButton;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isNightMode = false;

    // Globalne zmienne kolorów
    private int currentColor;
    private int targetColor;

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

        // Initialize light sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            setupLightSensor();
        }

        // Inicjalizacja globalnych zmiennych kolorów
        currentColor = getResources().getColor(R.color.lightModeColorPrimary);
        targetColor = getResources().getColor(R.color.lightModeColorPrimary);
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

    public void showRouteOnMap(ArrayList<LatLng> route) {
        MapaFragment mapaFragment = new MapaFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("ROUTE", route);
        mapaFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout, mapaFragment)
                .addToBackStack(null)
                .commit();
    }


    private void setupLightSensor() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final long THREE_SECONDS_IN_MILLIS = 3000;
    private boolean isNightModePending = false;
    private long nightModeStartTime = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];

            // Adjust the threshold based on your preference
            float threshold = 25.0f;

            if (lightLevel < threshold && !isNightMode) {
                // Enable NightMode
                if (!isNightModePending) {
                    isNightModePending = true;
                    nightModeStartTime = System.currentTimeMillis();
                }
            } else if (lightLevel >= threshold && isNightMode) {
                // Enable LightMode
                if (!isNightModePending) {
                    isNightModePending = true;
                    nightModeStartTime = System.currentTimeMillis();
                }
            } else {
                // Reset the timer if the condition is not met
                isNightModePending = false;
                nightModeStartTime = 0;
            }

            // Check if the condition has been met for at least 3 seconds
            if (isNightModePending && (System.currentTimeMillis() - nightModeStartTime) >= THREE_SECONDS_IN_MILLIS) {
                // Change the mode
                isNightMode = !isNightMode;
                int newColor = isNightMode
                        ? ContextCompat.getColor(this, R.color.nightModeColorPrimary)
                        : ContextCompat.getColor(this, R.color.lightModeColorPrimary);

                // Animate the color change
                animateColorChange(newColor);

                // Apply the night mode changes to the current views without recreating the activity
                applyNightMode(isNightMode);

                // Reset the timer and flag
                isNightModePending = false;
                nightModeStartTime = 0;
            }
        }
    }




    private void applyNightMode(boolean isNightMode) {
        // Determine background, button, and text colors based on day/night mode
        int backgroundColor = isNightMode
                ? ContextCompat.getColor(this, R.color.nightModeColorPrimary)
                : ContextCompat.getColor(this, R.color.lightModeColorPrimary);

        int buttonColor = isNightMode
                ? ContextCompat.getColor(this, R.color.nightModeColorPrimaryDark)
                : ContextCompat.getColor(this, R.color.lightModeColorPrimaryDark);

        int textColor = isNightMode
                ? ContextCompat.getColor(this, R.color.white)
                : ContextCompat.getColor(this, R.color.black);

        // Set background color
        binding.bottomNavigationView.setBackgroundColor(backgroundColor);

        // Set item text colors
        ColorStateList itemTextColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        textColor, // Color for checked state
                        textColor, // Color for unchecked state
                }
        );
        binding.bottomNavigationView.setItemTextColor(itemTextColorStateList);

        // Set icon colors
        int iconColorSelected = isNightMode
                ? ContextCompat.getColor(this, R.color.nightModeColorPrimaryDark)
                : ContextCompat.getColor(this, R.color.lightModeColorPrimaryDark);


        // Explicitly set item icon tint for the checked state
        ColorStateList itemIconColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        iconColorSelected, // Color for selected state
                        iconColorSelected, // Color for unselected state
                }
        );
        binding.bottomNavigationView.setItemIconTintList(itemIconColorStateList);

        Arrays.asList(R.id.logout_button, R.id.btnSetStartPoint, R.id.textViewLoggedUser, R.id.textViewUzytkownik)
                .forEach(id -> {
                    View view = findViewById(id);
                    if (view != null) {
                        if(id != R.id.textViewUzytkownik)
                        {
                            view.setBackgroundColor(buttonColor);
                        }else
                        if (view instanceof TextView) {
                            if(id == R.id.textViewUzytkownik)
                            {

                            }
                            ((TextView) view).setTextColor(textColor);
                        }
                    }
                });
    }

    // Custom easing function using PathInterpolator
    private float calculateFraction(float x, float start, float end) {
        // Map x to the range [0, 1]
        float fraction = Math.max(0, Math.min(1, (x - start) / (end - start)));

        // Use a PathInterpolator with custom control points
        Interpolator interpolator = new PathInterpolator(0.4f, 0f, 0.2f, 1f);

        // Apply the easing function
        return interpolator.getInterpolation(fraction);
    }

    public interface OnLocationPermissionGrantedListener {
        void onLocationPermissionGranted();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the sensor listener to avoid memory leaks
        sensorManager.unregisterListener(this);
    }

    private void animateColorChange(int newColor) {
        Drawable[] drawables = {
                new ColorDrawable(currentColor),
                new ColorDrawable(newColor)
        };

        TransitionDrawable transitionDrawable = new TransitionDrawable(drawables);
        getWindow().setBackgroundDrawable(transitionDrawable);

        transitionDrawable.startTransition(500); // Adjust the duration as needed

        // Save the current color for the next iteration
        currentColor = newColor;
    }

}