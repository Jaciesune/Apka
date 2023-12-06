package com.example.explorex;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class TrasyFragment extends Fragment {

    private RecyclerView recyclerView;
    private TrasyAdapter trasyAdapter;

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
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewRoutes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load routes initially
        ArrayList<GroupedLatLng> initialRoutes = loadSavedRoutes();
        trasyAdapter = new TrasyAdapter(initialRoutes, new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeRoute(position);
            }
        });

        recyclerView.setAdapter(trasyAdapter);

        return rootView;
    }

    private void removeRoute(int position) {
        ArrayList<GroupedLatLng> updatedRoutes = loadSavedRoutes();
        updatedRoutes.remove(position);
        trasyAdapter.updateRoutes(updatedRoutes);

        // Zaktualizuj listę tras w pliku po usunięciu trasy
        saveRoutesToFile(updatedRoutes);
    }

    private void saveRoutesToFile(ArrayList<GroupedLatLng> routes) {
        try {
            // Open the file "trasy.txt" in append mode
            FileOutputStream fos = requireContext().openFileOutput("trasy.txt", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Iteruj przez listę tras i zapisuj każdy wpis do pliku
            for (GroupedLatLng route : routes) {
                osw.write(route.getLatitude() + "," + route.getLongitude() + "," + route.getGroup() + "\n");
            }

            // Zamknij strumienie
            osw.close();
            fos.close();

            Toast.makeText(requireContext(), "Zaktualizowano plik trasy.txt", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Błąd podczas zapisywania trasy do pliku", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<GroupedLatLng> loadSavedRoutes() {
        ArrayList<GroupedLatLng> savedRoutes = new ArrayList<>();
        try {
            // Open the file "trasy.txt" for reading
            InputStream inputStream = requireContext().openFileInput("trasy.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Read each line from the file and add it to the list
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Parse each line into GroupedLatLng and add it to the list
                String[] coordinates = line.split(",");

                // Ensure that the array has at least 3 elements before accessing them
                if (coordinates.length >= 3) {
                    try {
                        double latitude = Double.parseDouble(coordinates[0]);
                        double longitude = Double.parseDouble(coordinates[1]);
                        int group = Integer.parseInt(coordinates[2]);
                        LatLng latLng = new LatLng(latitude, longitude);
                        savedRoutes.add(new GroupedLatLng(latLng, group));
                    } catch (NumberFormatException e) {
                        // Log a warning for parsing errors
                        Log.w("TrasyFragment", "Error parsing line: " + line, e);
                    }
                } else {
                    // Log a warning for lines with insufficient coordinates
                    Log.w("TrasyFragment", "Skipping line: " + line);
                }
            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            // Log an error for IO exceptions
            Log.e("TrasyFragment", "Error reading file", e);
        }
        return savedRoutes;
    }
}
