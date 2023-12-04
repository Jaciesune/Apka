package com.example.explorex;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.Manifest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

public class MapaFragment extends Fragment implements OnMapReadyCallback, RouteListener {
    private GoogleMap myMap;
    private final int FinePermissionCode = 1;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    Location currentLocation;
    // Required empty public constructor
    FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLoc;
    private LatLng destLoc;

    public MapaFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the class-level requestPermissionLauncher here in onCreate
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    getLastLocation();
                } else {
                    Toast.makeText(requireContext(), "Aplikacja nie ma pozwolenia na dostęp do lokalizacji urządzenia", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getLastLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mapa, container, false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

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
                myMap.clear();
                MarkerOptions options = new MarkerOptions().position(location).title("Twoja lokalizacja");
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                myMap.addMarker(options);
                destLoc = latLng;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                myMap.addMarker(markerOptions);

                getRoutePoints(location, destLoc);
            }
        });

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
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapaFragment.this);
                }
            }
        });
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
                    .context(requireActivity())  // use requireActivity() instead of MainActivity.this
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
        ArrayList<Polyline> polylines = null;
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylines = new ArrayList<>();
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
}