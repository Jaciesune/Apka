package com.example.explorex;

import com.google.android.gms.maps.model.LatLng;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrasyFragment extends Fragment {

    private RecyclerView recyclerView;
    private TrasyAdapter trasyAdapter;
    private ArrayList<String> savedRouteFilePaths = new ArrayList<>(); // Change to ArrayList

    public TrasyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewRoutes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        trasyAdapter = new TrasyAdapter(savedRouteFilePaths, new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteFile(position);
            }

            @Override
            public void onItemClick(int position) {
                String selectedFilePath = savedRouteFilePaths.get(position);
                // Implement your logic for opening or displaying the route
            }

            public void onShowRouteClick(int position) {
                String selectedFilePath = savedRouteFilePaths.get(position);
                showRouteOnMap(selectedFilePath);
            }
        });

        recyclerView.setAdapter(trasyAdapter);

        // Ustaw słuchacza dla przycisku "Pokaż trasę"
        trasyAdapter.setOnShowRouteClickListener(new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {

            }

            @Override
            public void onItemClick(int position) {

            }

            public void onShowRouteClick(int position) {
                String selectedFilePath = savedRouteFilePaths.get(position);
                showRouteOnMap(selectedFilePath);
            }
        });

        // Load saved routes when the fragment is created
        loadSavedRoutes();

        return rootView;
    }

    private void showRouteOnMap(String filePath) {
        // Implementuj logikę do wczytywania punktów trasy z pliku
        List<LatLng> routePoints = loadRoutePointsFromFile(filePath);

        // Sprawdź, czy istnieje MapaFragment
        MapaFragment mapaFragment = getMapaFragment();

        if (mapaFragment != null) {
            // Wywołaj metodę drawRouteOnMap z MapaFragment
            mapaFragment.drawRouteOnMap(routePoints);
        } else {
            // MapaFragment nie istnieje lub nie jest dostępny
            // Możesz obsłużyć to odpowiednio
            Toast.makeText(requireContext(), "Map is not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Metoda do uzyskania dostępu do MapaFragment (przykładowa implementacja)
    private MapaFragment getMapaFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        return (MapaFragment) fragmentManager.findFragmentById(R.id.mapContainer);
    }

    // Metoda do wczytywania punktów trasy z pliku (przykładowa implementacja)
    private List<LatLng> loadRoutePointsFromFile(String filePath) {

        List<LatLng> routePoints = new ArrayList<>();

        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                String[] coordinates = line.split(",");
                double latitude = Double.parseDouble(coordinates[0].trim());
                double longitude = Double.parseDouble(coordinates[1].trim());
                LatLng point = new LatLng(latitude, longitude);
                routePoints.add(point);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routePoints;
    }

    private void deleteFile(int position) {
        String filePath = savedRouteFilePaths.get(position);
        File fileToDelete = new File(filePath);

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                // Usunięto plik
                savedRouteFilePaths.remove(position);
                trasyAdapter.notifyItemRemoved(position);
            } else {
                // Błąd podczas usuwania pliku
                Toast.makeText(requireContext(), "Błąd podczas usuwania pliku", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Plik nie istnieje
            Toast.makeText(requireContext(), "Plik nie istnieje", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedRoutes() {
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + MapaFragment.Constants.ROUTES_DIRECTORY);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                savedRouteFilePaths.add(file.getAbsolutePath());
            }
        }

        // Notify the adapter that the data has changed
        trasyAdapter.updateRoutePaths(savedRouteFilePaths);
    }
}
