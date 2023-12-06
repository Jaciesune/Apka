package com.example.explorex;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.explorex.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;


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

    private void setupLightSensor() {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];

            // Adjust the threshold based on your preference
            float threshold = 25.0f;

            int newColor;
            if (lightLevel < threshold && !isNightMode) {
                // Enable NightMode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isNightMode = true;
                targetColor = getResources().getColor(R.color.nightModeColorPrimary);
                newColor = targetColor;
            } else if (lightLevel >= threshold && isNightMode) {
                // Enable LightMode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isNightMode = false;
                targetColor = getResources().getColor(R.color.lightModeColorPrimary);
                newColor = targetColor;
            } else {
                // Use a custom easing function for smoother transition
                float fraction = calculateFraction(lightLevel, threshold, 500.0f);
                newColor = (int) new ArgbEvaluator().evaluate(fraction, currentColor, targetColor);
            }

            // Animate the color change
            animateColorChange(newColor);
        }
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
