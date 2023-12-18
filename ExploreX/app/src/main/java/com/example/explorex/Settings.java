package com.example.explorex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    private RadioButton nightRadioButton;
    private RadioButton lightRadioButton;
    private RadioButton automaticRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Find the "Zakończ" button in the layout
        Button finishButton = findViewById(R.id.finish_button);

        // Find the RadioGroup containing the RadioButtons
        RadioGroup radioGroup = findViewById(R.id.radio_group);

        // Find the RadioButtons by their IDs
        nightRadioButton = findViewById(R.id.checkbox_night);
        lightRadioButton = findViewById(R.id.checkbox_light);
        automaticRadioButton = findViewById(R.id.checkbox_automatic);


        // Set OnClickListener for the "Zakończ" button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the status of the RadioButtons
                boolean isNightChecked = nightRadioButton.isChecked();
                boolean isLightChecked = lightRadioButton.isChecked();
                boolean isAutomaticChecked = automaticRadioButton.isChecked();

                // Show a toast message if Night Mode is enabled
                if (nightRadioButton.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Ciemny Motyw Włączony", Toast.LENGTH_SHORT).show();
                }
                if (lightRadioButton.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Jasny Motyw Włączony", Toast.LENGTH_SHORT).show();
                }
                if (automaticRadioButton.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Automatyczny Motyw Włączony", Toast.LENGTH_SHORT).show();
                }

                // Navigate to UzytkownikFragment
                navigateToUzytkownikFragment();
            }
        });
    }

    private void navigateToUzytkownikFragment() {
        // Create an intent to launch the MainActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Add a flag to clear the back stack so that pressing back won't return to Settings
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Store the status of each checkbox in shared preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("CheckboxNight", nightRadioButton.isChecked());
        editor.putBoolean("CheckboxLight", lightRadioButton.isChecked());
        editor.putBoolean("checkbox_automatic", automaticRadioButton.isChecked());
        editor.apply();

        // Start the MainActivity
        startActivity(intent);

        // Finish the Settings activity
        finish();
    }


}
