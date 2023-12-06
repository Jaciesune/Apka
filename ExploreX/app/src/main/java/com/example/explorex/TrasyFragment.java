package com.example.explorex;

import android.content.Context;
import android.os.Bundle;
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
    private ArrayList<ArrayList<LatLng>> savedRoutes = new ArrayList<>();

    public TrasyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedRoutes = loadSavedRoutesFromFile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerViewRoutes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        trasyAdapter = new TrasyAdapter(savedRoutes, new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeRoute(position);
            }
        });

        recyclerView.setAdapter(trasyAdapter);

        return rootView;
    }

    private void removeRoute(int position) {
        savedRoutes.remove(position);
        trasyAdapter.updateRoutes(savedRoutes);
        saveRoutesToFile(savedRoutes);
    }

    private void saveRoutesToFile(ArrayList<ArrayList<LatLng>> routes) {
        try {
            FileOutputStream fos = requireContext().openFileOutput("trasy.txt", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (ArrayList<LatLng> route : routes) {
                for (LatLng point : route) {
                    osw.write(point.latitude + "," + point.longitude + ",");
                }
                osw.write("\n");
            }

            osw.close();
            fos.close();

            Toast.makeText(requireContext(), "Updated trasy.txt file", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving routes to file", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<ArrayList<LatLng>> loadSavedRoutesFromFile() {
        ArrayList<ArrayList<LatLng>> savedRoutes = new ArrayList<>();
        try {
            InputStream inputStream = requireContext().openFileInput("trasy.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

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
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error loading routes from file", Toast.LENGTH_SHORT).show();
        }
        return savedRoutes;
    }
}
