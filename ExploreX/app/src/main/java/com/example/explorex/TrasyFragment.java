package com.example.explorex;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrasyFragment extends Fragment implements OnMapReadyCallback {

    private RecyclerView recyclerView;
    private TrasyAdapter trasyAdapter;
    private ArrayList<String> savedRouteFilePaths = new ArrayList<>();
    private OnShowRouteClickListener onShowRouteClickListener;
    private MapView mapView;
    private GoogleMap googleMap;

    public TrasyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewRoutes);
        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        trasyAdapter = new TrasyAdapter(savedRouteFilePaths, new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                deleteFile(position);
            }

            @Override
            public void onItemClick(int position) {
                // Handle item click if needed
            }

            @Override
            public void onShowRouteClick(int position, View rootView) {
                // Get the file path based on the position
                String selectedFileName = savedRouteFilePaths.get(position);

                // Show the route on the map using the file path
                showRouteOnMap(selectedFileName);

                // If you need to do something with the root layout of the item, use rootView
            }

            @Override
            public void onShowButtonClick(int position, View rootView) {
                // Handle btnShowRoute click here
                // You can implement the logic to load the route or perform any other action
                // For example, you can call showRouteOnMap(savedRouteFilePaths.get(position));
            }
        });

        // Set layout manager and adapter for the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(trasyAdapter);

        // Load saved routes when the fragment is created
        loadSavedRoutes();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    private void showRouteOnMap(String filePath) {
        // Wczytaj punkty trasy z pliku
        List<LatLng> routePoints = loadRoutePointsFromFile(filePath);

        // Wyczyszczenie aktualnych punktów na mapie
        googleMap.clear();

        // Dodanie nowych punktów trasy do mapy
        for (int i = 0; i < routePoints.size(); i++) {
            LatLng point = routePoints.get(i);

            // Dodaj zielony marker na pierwszym punkcie
            if (i == 0) {
                googleMap.addMarker(new MarkerOptions()
                        .position(point)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Początek trasy"));
            } else {
                // Dodaj standardowy marker dla pozostałych punktów trasy
                googleMap.addMarker(new MarkerOptions().position(point).title("Punkt trasy"));
            }
        }

        // Przybliżenie mapy do obszaru trasy
        if (!routePoints.isEmpty()) {
            LatLng firstPoint = routePoints.get(0);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 15f));
        }
        mapView.setVisibility(View.VISIBLE);
    }

    // Metoda do uzyskania dostępu do MapaFragment
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

        return new ArrayList<>();
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
        savedRouteFilePaths.clear();
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

    public void setOnShowRouteClickListener(OnShowRouteClickListener listener) {
        this.onShowRouteClickListener = listener;
    }

    public interface OnShowRouteClickListener {
        void onShowRouteClick(String routeFilePath);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}