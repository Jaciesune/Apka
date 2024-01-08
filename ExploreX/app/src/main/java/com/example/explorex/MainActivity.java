package com.example.explorex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import android.widget.ImageButton;
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

        // Initialize global variables for color modes
        applyNightMode(isNightMode());

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
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isCheckboxNightChecked = preferences.getBoolean("CheckboxNight", false);
        boolean isCheckboxLightChecked = preferences.getBoolean("CheckboxLight", false);
        boolean isCheckboxAutomaticChecked = preferences.getBoolean("checkbox_automatic", false);

        // If checkbox_automatic is checked, use automatic mode based on light level
        if (isCheckboxAutomaticChecked && event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];
            float threshold = getThreshold(isCheckboxNightChecked, isCheckboxLightChecked);

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

                // Set background and button colors directly
                setNightModeColors(isNightMode);

                // Reset the timer and flag
                isNightModePending = false;
                nightModeStartTime = 0;
            }
        }
    }

    private void setNightModeColors(boolean isNightMode) {
        int backgroundColor;
        int buttonColor;
        int textColor;

        if (isNightMode) {
            backgroundColor = ContextCompat.getColor(this, R.color.nightModeColorPrimary);
            buttonColor = ContextCompat.getColor(this, R.color.nightModeColorPrimaryDark);
            textColor = ContextCompat.getColor(this, R.color.white);
        } else {
            backgroundColor = ContextCompat.getColor(this, R.color.lightModeColorPrimary);
            buttonColor = ContextCompat.getColor(this, R.color.lightModeColorPrimaryDark);
            textColor = ContextCompat.getColor(this, R.color.black);
        }

        // Set background color of BottomNavigationView
        binding.bottomNavigationView.setBackgroundColor(backgroundColor);

        // Set button and text colors for specified views
        List<Integer> viewIds = Arrays.asList(
                R.id.logout_button, R.id.btnSetStartPoint, R.id.textViewLoggedUser,
                R.id.textViewUzytkownik, R.id.settingsButton
        );

        for (int id : viewIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setBackgroundColor(buttonColor);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(textColor);
                }
            }
        }

        // Set the background color for textViewUzytkownik
        TextView textViewUzytkownik = findViewById(R.id.textViewUzytkownik);
        if (textViewUzytkownik != null) {
            textViewUzytkownik.setBackgroundColor(buttonColor);
        }

        // Set item text colors
        for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = binding.bottomNavigationView.getMenu().getItem(i);
            SpannableString spannableString = new SpannableString(menuItem.getTitle());
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(textColor);
            spannableString.setSpan(foregroundColorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItem.setTitle(spannableString);
        }

        // Set icon colors
        ColorStateList itemIconColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        buttonColor, // Color for selected state
                        buttonColor, // Color for unselected state
                }
        );
        binding.bottomNavigationView.setItemIconTintList(itemIconColorStateList);
    }


    // Method to get the current light level
    private float getCurrentLightLevel(SensorEvent event) {
        // Assuming that the illuminance value is at index 0 in the values array
        return event.values[0];
    }

    private float getThreshold(boolean isCheckboxNightChecked, boolean isCheckboxLightChecked) {
        float threshold;

        if (isCheckboxLightChecked) {
            // Set a lower threshold for LightMode
            threshold = 1.0f;
        } else if (isCheckboxNightChecked) {
            // Set a higher threshold for NightMode
            threshold = 9999.9f;
        } else {
            // Use a default threshold for other cases
            threshold = 25.0f;
        }

        return threshold;
    }
    private boolean isNightMode() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return preferences.getBoolean("CheckboxNight", false);
    }

    private void applyNightMode(boolean isNightMode) {
        // Retrieve preferences object
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Set background color of BottomNavigationView
        int backgroundColor = ContextCompat.getColor(this, isNightMode ? R.color.nightModeColorPrimary : R.color.lightModeColorPrimary);
        binding.bottomNavigationView.setBackgroundColor(backgroundColor);

        // Set button and text colors for specified views
        int buttonColor = ContextCompat.getColor(this, isNightMode ? R.color.nightModeColorPrimaryDark : R.color.lightModeColorPrimaryDark);
        int textColor = ContextCompat.getColor(this, isNightMode ? R.color.white : R.color.black);

        List<Integer> viewIds = Arrays.asList(
                R.id.logout_button, R.id.btnSetStartPoint, R.id.textViewLoggedUser, R.id.settingsButton, R.id.textViewUzytkownik
        );

        for (int id : viewIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setBackgroundColor(buttonColor);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(textColor);
                }
            }
        }

        // Set the background color for textViewUzytkownik
        TextView textViewUzytkownik = findViewById(R.id.textViewUzytkownik);
        if (textViewUzytkownik != null) {
            textViewUzytkownik.setBackgroundColor(buttonColor);
            textViewUzytkownik.setTextColor(textColor);
        }

        // Set item text colors
        int itemTextColor = ContextCompat.getColor(this, isNightMode ? R.color.white : R.color.black);
        int itemIconColorSelected = ContextCompat.getColor(this, isNightMode ? R.color.nightModeColorPrimaryDark : R.color.lightModeColorPrimaryDark);

        for (int i = 0; i < binding.bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = binding.bottomNavigationView.getMenu().getItem(i);
            SpannableString spannableString = new SpannableString(menuItem.getTitle());
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(itemTextColor);
            spannableString.setSpan(foregroundColorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItem.setTitle(spannableString);
        }

        // Set icon colors
        ColorStateList itemIconColorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked},
                },
                new int[]{
                        itemIconColorSelected, // Color for selected state
                        itemIconColorSelected, // Color for unselected state
                }
        );
        binding.bottomNavigationView.setItemIconTintList(itemIconColorStateList);
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
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, newColor);
        colorAnimation.setDuration(500); // Adjust the duration as needed

        colorAnimation.addUpdateListener(animator -> {
            int animatedValue = (int) animator.getAnimatedValue();
            getWindow().setBackgroundDrawable(new ColorDrawable(animatedValue));

            // Set background and button colors directly
            setNightModeColors(animatedValue == ContextCompat.getColor(this, R.color.nightModeColorPrimary));
        });

        colorAnimation.start();

        // Save the current color for the next iteration
        currentColor = newColor;
    }

}