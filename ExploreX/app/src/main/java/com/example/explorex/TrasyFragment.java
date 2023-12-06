package com.example.explorex;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
public class TrasyFragment extends Fragment {

    private ListView listViewRoutes;
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

        trasyAdapter = new TrasyAdapter(loadSavedRoutes(), new TrasyAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeRoute(position);
            }
        });

        recyclerView.setAdapter(trasyAdapter);

        return rootView;
    }

    private void removeRoute(int position) {
        ArrayList<String> updatedRoutes = loadSavedRoutes();
        updatedRoutes.remove(position);
        trasyAdapter.updateRoutes(updatedRoutes);

        // Zapisz zaktualizowaną listę tras do pliku
        saveRoutesToFile(updatedRoutes);
    }

    private void saveRoutesToFile(ArrayList<String> routes) {
        try {
            // Otwórz plik "trasy.txt" w trybie zastępowania (czyli usuń istniejący i utwórz nowy)
            FileOutputStream fos = requireContext().openFileOutput("trasy.txt", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Iteruj przez listę tras i zapisuj każdy wpis do pliku
            for (String route : routes) {
                osw.write(route + "\n");
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