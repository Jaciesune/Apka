package com.example.explorex;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TrasyFragment extends Fragment {

    private ListView listViewRoutes;

    public TrasyFragment() {
        // Required empty public constructor
    }

    public static TrasyFragment newInstance(String param1, String param2) {
        TrasyFragment fragment = new TrasyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);

        listViewRoutes = rootView.findViewById(R.id.listViewRoutes);

        // Load saved routes from the file
        ArrayList<String> savedRoutes = loadSavedRoutes();

        // Create an ArrayAdapter to display the routes in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, savedRoutes);

        // Set the adapter to the ListView
        listViewRoutes.setAdapter(adapter);

        return rootView;
    }

    private ArrayList<String> loadSavedRoutes() {
        ArrayList<String> savedRoutes = new ArrayList<>();
        try {
            // Open the file "trasy.txt" for reading
            InputStream inputStream = requireContext().openFileInput("trasy.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Read each line from the file and add it to the list
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                savedRoutes.add(line);
            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return savedRoutes;
    }
}