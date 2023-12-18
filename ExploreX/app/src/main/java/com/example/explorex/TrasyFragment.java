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
import androidx.fragment.app.FragmentTransaction;
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
    private OnShowRouteClickListener onShowRouteClickListener;

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
            }
            @Override
            public void onShowRouteClick(int position) {
                String selectedFileName = savedRouteFilePaths.get(position);
                if (onShowRouteClickListener != null) {
                    onShowRouteClickListener.onShowRouteClick(selectedFileName);
                }
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

            @Override
            public void onShowRouteClick(int position) {
                String selectedFileName = savedRouteFilePaths.get(position);
                showRouteOnMap(selectedFileName);
            }
        });

        // Load saved routes when the fragment is created
        loadSavedRoutes();

        return rootView;
    }

    private void openMapFragment(String filePath) {
        MapaFragment mapaFragment = new MapaFragment();
        Bundle bundle = new Bundle();
        bundle.putString("routeFilePath", filePath);
        mapaFragment.setArguments(bundle);

        // Uzyskujemy menedżera fragmentów dla fragmentu nadrzędnego (this) za pomocą getChildFragmentManager()
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Zamieniamy aktualny MapaFragment w kontenerze na nowy MapaFragment z argumentami
        fragmentTransaction.replace(R.id.mapContainer, mapaFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showRouteOnMap(String filePath) {
        MapaFragment mapaFragment = getMapaFragment();
        if (mapaFragment != null) {
            // Sprawdź, czy mapaFragment jest dodany do hierarchii fragmentów
            if (mapaFragment.isAdded()) {
                mapaFragment.loadAndDrawRoute(filePath);
            } else {
                // Dodaj kod obsługi, gdy MapaFragment nie jest jeszcze dodany
                Toast.makeText(requireContext(), "MapaFragment nie jest jeszcze dodany", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Jeżeli mapaFragment jest nullem, utwórz nową instancję i dodaj do widoku
            mapaFragment = new MapaFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapaFragment, "mapFragmentTag")
                    .addToBackStack(null)
                    .commit();
            // Oczekaj na dodanie fragmentu do hierarchii fragmentów, a następnie wczytaj trasę
            getChildFragmentManager().executePendingTransactions();
            mapaFragment.loadAndDrawRoute(filePath);
        }
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
}
