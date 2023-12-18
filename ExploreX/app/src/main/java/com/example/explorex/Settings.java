package com.example.explorex;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Settings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Find the "Zakończ" button in the layout
        Button finishButton = findViewById(R.id.finish_button);

        // Set OnClickListener for the "Zakończ" button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // Add any other extras or flags you might need

        // Start the MainActivity
        startActivity(intent);

        // Finish the Settings activity
        finish();
    }
}
