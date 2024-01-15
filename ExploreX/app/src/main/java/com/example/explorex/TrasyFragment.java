package com.example.explorex;

import com.example.explorex.TrasyAdapter.TrasyAdapterListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TrasyFragment extends Fragment implements TrasyAdapter.TrasyAdapterListener {

    private RecyclerView recyclerView;
    private TrasyAdapter trasyAdapter;
    private ArrayList<String> savedRouteFilePaths = new ArrayList<>();
    private MapView mapView;

    public TrasyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerViewRoutes);
        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        // Ustawianie na Recycler View
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);

        // Wczytywanie trasy po utworzeniu widoku
        loadSavedRoutes();

        // Przekazywanie tego fragmentu jako TrasyAdapterListener
        trasyAdapter = new TrasyAdapter(savedRouteFilePaths, this);
        recyclerView.setAdapter(trasyAdapter);

        // Odświeżenie tras po załadowaniu
        refreshRoutesList();

        // Inicjalizacja mapy
        initializeMap();

        return rootView;
    }

    private void initializeMap() {
        mapView.getMapAsync(googleMap -> {

            // Sprawdzenie czy są jakieś punkty trasy na mapie
            if (!savedRouteFilePaths.isEmpty()) {
                // Pobranie pierwszego i ostatniego punktu z trasy
                String firstRouteFilePath = savedRouteFilePaths.get(0);
                String lastRouteFilePath = savedRouteFilePaths.get(savedRouteFilePaths.size() - 1);

                ArrayList<LatLng> firstRoutePoints = readRoutePointsFromFile(firstRouteFilePath);
                ArrayList<LatLng> lastRoutePoints = readRoutePointsFromFile(lastRouteFilePath);

                // Dodanie znaczników na początek i koniec
                if (firstRoutePoints.size() > 0) {
                    LatLng firstPoint = firstRoutePoints.get(0);
                    googleMap.addMarker(new MarkerOptions()
                            .position(firstPoint)
                            .title("Start")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }

                if (lastRoutePoints.size() > 0) {
                    LatLng lastPoint = lastRoutePoints.get(lastRoutePoints.size() - 1);
                    googleMap.addMarker(new MarkerOptions()
                            .position(lastPoint)
                            .title("Meta")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }
        });
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
    }

    // Metoda do odświeżania listy tras
    private void refreshRoutesList() {
        trasyAdapter.updateRoutePaths(savedRouteFilePaths);
    }

    @Override
    public void updateRoutePaths(ArrayList<String> updatedRoutePaths) {
        // Zaktualizuj listę tras w adapterze
        trasyAdapter.updateRoutePaths(updatedRoutePaths);
    }

    @Override
    public void showRouteOnMap(String filePath) {
        // Odczytaj punkty trasy z pliku
        ArrayList<LatLng> routePoints = readRoutePointsFromFile(filePath);

        if (routePoints != null && !routePoints.isEmpty()) {
            // Wywołanie rysowania trasy
            drawRouteOnMap(routePoints);
        } else {
            Toast.makeText(requireContext(), "Błąd odczytu trasy z pliku", Toast.LENGTH_SHORT).show();
        }
    }
    private void drawRouteOnMap(ArrayList<LatLng> routePoints) {
        mapView.getMapAsync(googleMap -> {
            // Ustaw styl linii trasy
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .color(Color.BLUE)
                    .width(5)
                    .geodesic(false);

            // Dodaj linie trasy na mapę
            Polyline polyline = googleMap.addPolyline(polylineOptions);

            // Ustaw koniec trasy na przeźroczysty
            int polylineSize = polyline.getPoints().size();
            if (polylineSize > 0) {
                LatLng lastPoint = polyline.getPoints().get(polylineSize - 1);

                // Utwórz przezroczysty marker
                Bitmap transparentMarkerBitmap = getTransparentMarkerBitmap(); // Zdefiniuj tę metodę zgodnie z Twoimi potrzebami
                BitmapDescriptor transparentMarker = BitmapDescriptorFactory.fromBitmap(transparentMarkerBitmap);

                googleMap.addMarker(new MarkerOptions()
                        .position(lastPoint)
                        .title("Meta")
                        .icon(transparentMarker));
            }

            // Wyśrodkuj mapę na trasie
            if (!routePoints.isEmpty()) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : routePoints) {
                    builder.include(point);
                }
                LatLngBounds bounds = builder.build();

                // Dodaj obsługę pustego obszaru, aby uniknąć problemu z "CameraUpdateFactory.newLatLngBounds"
                int padding = 100; // Możesz dostosować padding według potrzeb
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                // Sprawdź, czy czas trwania animacji jest dodatni
                int animationDurationMs = 1000; // Ustaw dowolny czas trwania animacji w milisekundach
                if (animationDurationMs > 0) {
                    googleMap.animateCamera(cameraUpdate, animationDurationMs, null);
                } else {
                    // Jeśli czas trwania animacji jest ujemny lub równy zero, użyj moveCamera
                    googleMap.moveCamera(cameraUpdate);
                }
            }
        });
    }

    private Bitmap getTransparentMarkerBitmap() {
        // Utwórz Bitmap z przezroczystym tłem
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        return bitmap;
    }
    // Metoda do odczytu punktów trasy z pliku tekstowego
    private ArrayList<LatLng> readRoutePointsFromFile(String filePath) {
        ArrayList<LatLng> routePoints = new ArrayList<>();

        try {
            // Otwórz plik do odczytu
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            // Odczytaj linie z pliku
            String line;
            while ((line = reader.readLine()) != null) {
                // Parsuj linie na współrzędne
                String[] coordinates = line.split(",");
                if (coordinates.length == 2) {
                    double latitude = Double.parseDouble(coordinates[0]);
                    double longitude = Double.parseDouble(coordinates[1]);
                    LatLng point = new LatLng(latitude, longitude);
                    routePoints.add(point);
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return routePoints;
    }

    public void onShowRouteClicked(String filePath) {

        showRouteOnMap(filePath);
    }
}