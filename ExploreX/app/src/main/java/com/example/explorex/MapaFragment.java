package com.example.explorex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.codebyashish.googledirectionapi.AbstractRouting;
import com.codebyashish.googledirectionapi.ErrorHandling;
import com.codebyashish.googledirectionapi.RouteDrawing;
import com.codebyashish.googledirectionapi.RouteInfoModel;
import com.codebyashish.googledirectionapi.RouteListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

public class MapaFragment extends Fragment implements OnMapReadyCallback, RouteListener {
    private GoogleMap myMap;
    private final int FinePermissionCode = 1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLoc;
    private LatLng destLoc;
    private boolean isStartPointSet = false;
    private Button btnSetStartPoint;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ArrayList<LatLng> routePoints = new ArrayList<>();
    private ArrayList<ArrayList<LatLng>> savedRoutes = new ArrayList<>();
    private Marker locationMarker;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public MapaFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the launcher
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission is granted, proceed with the operation
                        getLastLocation();
                    } else {
                        // Permission is denied
                        Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Initialize location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(3000); // 3 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize the location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle location updates here
                    updateLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        // Initialize the fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLastLocation();
        savedRoutes = loadSavedRoutesFromFile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        btnSetStartPoint = rootView.findViewById(R.id.btnSetStartPoint);
        btnSetStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonText();
            }
        });

        return rootView;
    }

    private void toggleButtonText() {
        isStartPointSet = !isStartPointSet;
        updateButtonText();

        if (isStartPointSet) {
            // Rozpocznij pobieranie lokalizacji użytkownika i rysowanie trasy
            getLastLocation();
        } else {
            // Zakończ rysowanie trasy i zapisz do pliku
            saveRouteToFile();
        }
    }

    private void saveRouteToFile() {
        // Check if routePoints is not empty
        if (!routePoints.isEmpty()) {
            try {
                // Open a file output stream for "trasy.txt" in append mode
                FileOutputStream fos = requireContext().openFileOutput("trasy.txt", Context.MODE_APPEND);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                // Iterate through routePoints and write each LatLng to the file
                for (LatLng point : routePoints) {
                    osw.write(point.latitude + "," + point.longitude + "\n");
                }

                // Close the streams
                osw.close();
                fos.close();

                Toast.makeText(requireContext(), "Trasa dodana do pliku trasy.txt", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Błąd podczas zapisywania trasy do pliku", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error saving route to file: " + e.getMessage());
            }
        } else {
            Log.e("TAG", "Route points list is empty. No data to save to file.");
        }
    }



    private void updateButtonText() {
        if (isStartPointSet) {
            btnSetStartPoint.setText("Zakończ");
        } else {
            btnSetStartPoint.setText("Rozpocznij");
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Request location updates
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            updateLocationMarker(currentLatLng);
        } else {
            Toast.makeText(requireContext(), "Lokalizacja jest niedostępna", Toast.LENGTH_SHORT).show();
        }

        LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        MarkerOptions options = new MarkerOptions().position(location).title("Twoja lokalizacja");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        myMap.addMarker(options);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 15);
        googleMap.animateCamera(cameraUpdate);

        myMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (isStartPointSet) {
                    myMap.clear();
                    if (userLoc != null) {
                        updateLocationMarker(userLoc);
                    }

                    destLoc = latLng;
                    MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                    markerOptions.position(latLng);
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    myMap.addMarker(markerOptions);
                    routePoints.add(latLng);

                    updateMapView(userLoc, destLoc);

                    drawRouteOnMap(savedRoutes, routePoints);

                    savedRoutes.add(routePoints);
                    routePoints = new ArrayList<>();

                    addStartPointButton();
                    updateButtonText();
                }
            }
        });
    }

    private void drawRouteOnMap(ArrayList<ArrayList<LatLng>> routes, ArrayList<LatLng> currentRoute) {
        for (ArrayList<LatLng> route : routes) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLUE);
            polylineOptions.width(12);
            polylineOptions.addAll(route);
            polylineOptions.startCap(new RoundCap());
            polylineOptions.endCap(new RoundCap());
            myMap.addPolyline(polylineOptions);
        }

        // Draw the current route
        PolylineOptions currentRouteOptions = new PolylineOptions();
        currentRouteOptions.color(Color.RED);
        currentRouteOptions.width(12);
        currentRouteOptions.addAll(currentRoute);
        currentRouteOptions.startCap(new RoundCap());
        currentRouteOptions.endCap(new RoundCap());
        myMap.addPolyline(currentRouteOptions);
    }

    private void updateLocationMarker(LatLng latLng) {
        if (myMap != null) {
            // Sprawdź, czy myMap nie jest nullem przed dodaniem markera
            if (locationMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                locationMarker = myMap.addMarker(markerOptions);
            } else {
                locationMarker.setPosition(latLng);
            }

            if (isStartPointSet) {
                // Dodaj kod dotyczący aktualizacji widoku związanego z punktem początkowym, jeśli jest taka potrzeba
                // ...

                // Przykład: Centrowanie kamery na nowej lokalizacji
                myMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    private void updateMapView(LatLng startPoint, LatLng endPoint) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startPoint);
        builder.include(endPoint);
        LatLngBounds bounds = builder.build();

        int padding = 100; // margines w pikselach
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        myMap.animateCamera(cameraUpdate);
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Use the class-level requestPermissionLauncher instead of creating a new local one
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    updateLocationMarker(currentLatLng); // Dodane: Aktualizuj marker lokalizacji
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
                    mapFragment.getMapAsync(MapaFragment.this);
                }
            }
        });
    }

    private void addStartPointButton() {
        if (myMap != null) {
            LatLng startPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            MarkerOptions startMarkerOptions = new MarkerOptions().position(startPoint).title("Punkt startowy");
            startMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            myMap.addMarker(startMarkerOptions);

            // Dodaj przycisk do wyznaczania punktu startowego
            myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.getTitle().equals("Punkt startowy")) {
                        // Tutaj obsłuż logikę po naciśnięciu przycisku
                        // Na przykład możesz użyć punktu startowego do innych operacji
                        Toast.makeText(requireContext(), "Punkt startowy wyznaczony!", Toast.LENGTH_SHORT).show();

                        // Zaktualizuj stan przycisku i tekst
                        isStartPointSet = true;
                        updateButtonText();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FinePermissionCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(requireContext(), "Aplikacja nie ma pozwolenia na dostęp do lokalizacji urządzenia", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getRoutePoints(LatLng start, LatLng end) {
        if (start == null || end == null) {
            Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_LONG).show();
            Log.e("TAG", " latlngs are null");
        } else {
            userLoc = start;
            destLoc = end;
            RouteDrawing routeDrawing = new RouteDrawing.Builder()
                    .context(requireActivity())
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener((RouteListener) this)
                    .alternativeRoutes(true)
                    .waypoints(userLoc, destLoc)
                    .build();
            routeDrawing.execute();
        }
    }

    public void onRouteFailure(ErrorHandling e) {
        Log.w("TAG", "onRoutingFailure: " + e);
    }

    public void onRouteStart() {
        Log.d("TAG", "yes started");
    }

    public void onRouteSuccess(ArrayList<RouteInfoModel> routeInfoModelArrayList, int routeIndexing) {
        ArrayList<Polyline> polylines = new ArrayList<>();  // Przenieś inicjalizację tutaj

        PolylineOptions polylineOptions = new PolylineOptions();
        for (int i = 0; i < routeInfoModelArrayList.size(); i++) {
            if (i == routeIndexing) {
                Log.e("TAG", "onRoutingSuccess: routeIndexing" + routeIndexing);
                polylineOptions.color(Color.BLACK);
                polylineOptions.width(12);
                polylineOptions.addAll(routeInfoModelArrayList.get(routeIndexing).getPoints());
                polylineOptions.startCap(new RoundCap());
                polylineOptions.endCap(new RoundCap());
                Polyline polyline = myMap.addPolyline(polylineOptions);
                polylines.add(polyline);
            }
        }
    }

    public void onRouteCancelled() {
        Log.d("TAG", "route canceled");
        // restart your route drawing
    }

    private ArrayList<ArrayList<LatLng>> loadSavedRoutesFromFile() {
        ArrayList<ArrayList<LatLng>> savedRoutes = new ArrayList<>();
        try {
            FileInputStream fis = requireContext().openFileInput("trasy.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] coordinates = line.split(",");
                ArrayList<LatLng> route = new ArrayList<>();
                for (int i = 0; i < coordinates.length; i += 2) {
                    double lat = Double.parseDouble(coordinates[i]);
                    double lng = Double.parseDouble(coordinates[i + 1]);
                    route.add(new LatLng(lat, lng));
                }
                savedRoutes.add(route);
            }
            bufferedReader.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error loading routes from file", Toast.LENGTH_SHORT).show();
        }
        return savedRoutes;
    }
    @Override
    public void onStop() {
        super.onStop();
        // Remove location updates when the fragment is stopped
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}