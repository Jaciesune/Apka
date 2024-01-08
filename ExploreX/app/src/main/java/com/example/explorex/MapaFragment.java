package com.example.explorex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
public class MapaFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap myMap;
    private Button btnSetStartPoint;
    private EditText editText;
    private ListView savedRoutesListView;
    private boolean recordingRoute = false;
    private PolylineOptions routePolyline;
    private List<LatLng> routePoints;
    private List<String> savedRouteFiles;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Handler locationUpdateHandler;
    private static final int LOCATION_UPDATE_INTERVAL = 1000;
    private String enteredFileName;
    public MapaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sprawdzanie i pytanie o pozwolenie na dostęp do lokalizacji przy pierwszym uruchomieniu
        if (!checkLocationPermission()) {
            requestLocationPermissions();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);

        btnSetStartPoint = rootView.findViewById(R.id.btnSetStartPoint);
        btnSetStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordingRoute) {
                    // jeśli nagrywa, zakończ
                    stopRecordingRoute();
                    // zapisz używając nazwy podanej przez usera
                    saveLocationToFile(enteredFileName);
                } else {
                    // jeśli nie nagrywa, rozpocznij i zapisz plik o podanej nazwie
                    showFileNameDialogAndStartRecording();
                }
            }
        });

        // Sprawdzanie czy map fragment jest dodany
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("mapFragmentTag");

        if (mapFragment == null) {
            // Jeśli SupportMapFragment nie jest dodany, dodajemy dynamicznie
            mapFragment = new SupportMapFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.mapContainer, mapFragment, "mapFragmentTag").commit();
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    private void showFileNameDialogAndStartRecording() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rozpocznij trasę");

        final EditText editText = new EditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);
        builder.setPositiveButton("Start", (dialog, which) -> {
            // Store the entered text in the class-level variable
            enteredFileName = editText.getText().toString().trim();

            if (!enteredFileName.isEmpty()) {
                // Start recording route with the entered file name
                startRecordingRoute(enteredFileName);
                // Display a toast indicating the start of recording with the file name
                String toastMessage = "Rozpoczęto nagrywanie trasy. Nazwa pliku: '" + enteredFileName + "'";
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showFileNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Rozpocznij trasę");
        final EditText editText = new EditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(editText);
        builder.setPositiveButton("Start", (dialog, which) -> {
            // Store the entered text in the class-level variable
            enteredFileName = editText.getText().toString().trim();

            if (!enteredFileName.isEmpty()) {
                startRecordingRoute(enteredFileName);
            }
        });
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void startRecordingRoute(String fileName) {
        recordingRoute = true;
        btnSetStartPoint.setText("Zakończ");
        routePolyline = new PolylineOptions().color(Color.BLUE).width(5);
        routePoints = new ArrayList<>();
        // Start location updates
        startLocationUpdates();
    }

    private void stopRecordingRoute() {
        // Stop location updates
        stopLocationUpdates();
        recordingRoute = false;
        btnSetStartPoint.setText("Start");
        // Use the stored file name or generate a unique one
        String fileName = (enteredFileName != null && !enteredFileName.isEmpty()) ? enteredFileName : "route_" + System.currentTimeMillis();
        // Save route data to file
        saveRouteToFile(fileName);
        // Draw the recorded route on the map
        drawRouteOnMap(routePoints);
        // Display a toast indicating that the file is saved
        String toastMessage = "Trasa zapisana pod nazwą '" + fileName + "' w: " + getExternalFilePath(fileName);
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void startLocationUpdates() {
        locationUpdateHandler = new Handler(Looper.getMainLooper());
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Update location every LOCATION_UPDATE_INTERVAL milliseconds
                updateLocation();
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates() {
        if (locationUpdateHandler != null) {
            locationUpdateHandler.removeCallbacksAndMessages(null);
        }
    }

    private void updateLocation() {
        if (checkLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    routePoints.add(currentLocation);
                    routePolyline.add(currentLocation);
                }
            } else {
                // Handle the case where location permission is not granted
            }
        }
    }

    private void saveLocationToFile(String fileName) {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions here
            requestLocationPermissions();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            LatLng endPoint = new LatLng(location.getLatitude(), location.getLongitude());
            routePoints.add(endPoint);
            routePolyline.add(endPoint);
        }
        recordingRoute = false;
        btnSetStartPoint.setText("Start");
        saveRouteToFile(fileName);
        drawRouteOnMap(routePoints);
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void drawRouteOnMap(List<LatLng> route) {
        if (myMap != null && route != null && !route.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(route);
            myMap.addPolyline(polylineOptions);
        }
    }

    public class Constants {
        public static final String ROUTES_DIRECTORY = "Routes";
    }
    private void saveRouteToFile(String fileName) {
        StringBuilder routeData = new StringBuilder();
        for (LatLng point : routePoints) {
            routeData.append(point.latitude).append(",").append(point.longitude).append("\n");
        }

        try {
            FileOutputStream fos = new FileOutputStream(getExternalFilePath(fileName), true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(routeData.toString());
            osw.close();
            fos.close();
            // Display a toast indicating that the file is saved
            String toastMessage = "Plik '" + fileName + "' zapisany w: " + getExternalFilePath(fileName);
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String getExternalFilePath(String fileName) {
        // Get the public storage directory on the device
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + Constants.ROUTES_DIRECTORY);

        if (!directory.exists()) {
            directory.mkdirs();
        }
        // Create the file path with the directory name, file name, and extension
        return directory.getAbsolutePath() + "/" + fileName + ".txt";
    }
    public void loadAndDrawRoute(String filePath) {
        List<LatLng> routePoints = loadRoutePointsFromFile(filePath);
        drawRouteOnMap(routePoints);
    }
    public void showRouteOnMap(String filePath) {
        List<LatLng> routePoints = loadRoutePointsFromFile(filePath);

        if (!routePoints.isEmpty()) {
            drawRouteOnMap(routePoints);
        } else {
            Toast.makeText(requireContext(), "Trasa nie zawiera punktów", Toast.LENGTH_SHORT).show();
        }
    }
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
    private void updateSavedRoutesList() {
        // Pobierz listę plików zapisanych tras z katalogu publicznego przechowywania
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + Constants.ROUTES_DIRECTORY);
        File[] files = directory.listFiles();

        // Zaktualizuj listę zapisanych tras
        savedRouteFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                savedRouteFiles.add(file.getName());
            }
        }
        // Utwórz adapter i ustaw go dla widoku listy
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, savedRouteFiles);
        savedRoutesListView.setAdapter(adapter);

        // Dodaj nasłuchiwanie zdarzeń kliknięcia na element listy
        savedRoutesListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedFileName = savedRouteFiles.get(position);
            // Tutaj możesz obsłużyć wybór zapisanego pliku
            Toast.makeText(requireContext(), "Wybrano trasę: " + selectedFileName, Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        // Sprawdzenie pozwolenia na lokalizację
        if (checkLocationPermission()) {
            // Lokalizacja użytkownika
            myMap.setMyLocationEnabled(true);

            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            if (getArguments() != null && getArguments().containsKey("routeFilePath")) {
                String routeFilePath = getArguments().getString("routeFilePath");
                showRouteOnMap(routeFilePath);
            }
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                myMap.addMarker(new MarkerOptions().position(currentLatLng).title("Aktualna lokalizacja").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        } else {
            // Brak zezwolenia na lokalizację, wyświetl komunikat i poproś użytkownika o uprawnienia
            requestLocationPermissions();
        }
    }
    private void showLocationPermissionErrorMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Brak zezwolenia na dostęp do lokalizacji");
        builder.setMessage("Aplikacja nie działa bez zezwolenia na dostęp do lokalizacji. Uruchom aplikację ponownie po nadaniu dostępu.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Zamknięcie aplikacji, możesz również użyć System.exit(0);
                requireActivity().finish();
            }
        });
        builder.show();
    }
    private void createFile(String fileName) {
        try {
            FileOutputStream fos = requireContext().openFileOutput(fileName + ".txt", Context.MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write("Zawartość pliku");
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}