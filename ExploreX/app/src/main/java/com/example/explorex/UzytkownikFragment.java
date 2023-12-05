package com.example.explorex;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.explorex.databinding.FragmentUzytkownikBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UzytkownikFragment extends Fragment {

    private FragmentUzytkownikBinding binding;
    private TextView textViewLoggedUser;

    public static UzytkownikFragment newInstance() {
        return new UzytkownikFragment();
    }

    public UzytkownikFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUzytkownikBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // Update this line to get the logoutButton from MainActivity
        Button logoutButton = ((MainActivity) requireActivity()).getLogoutButton();

        // Find the TextView for the logged-in user
        textViewLoggedUser = binding.textViewLoggedUser;

        // Check if the user is logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // User is logged in, show the logout button
            logoutButton.setVisibility(View.VISIBLE);

            // Set click listener for the logout button
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sign out the user
                    auth.signOut();

                    // Redirect to the login screen
                    Intent intent = new Intent(requireContext(), Login.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            });

            // Display "Zalogowany jako " followed by the user's email
            textViewLoggedUser.setText(user.getEmail());
        } else {
            // User is not logged in, hide the logout button
            logoutButton.setVisibility(View.GONE);

            // Clear the text if the user is not logged in
            textViewLoggedUser.setText("");
        }

        if (user != null) {
            // User is logged in, display the email
            TextView textViewLoggedUser = binding.textViewLoggedUser;
            textViewLoggedUser.setText(user.getEmail());
        }

        return rootView;
    }
}