// RouteUtils.java
package com.example.explorex;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RouteUtils {

    public static ArrayList<LatLng> loadRouteFromFile(Context context, String filePath) {
        ArrayList<LatLng> route = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] coordinates = line.split(",");
                for (int i = 0; i < coordinates.length; i += 2) {
                    double lat = Double.parseDouble(coordinates[i]);
                    double lng = Double.parseDouble(coordinates[i + 1]);
                    route.add(new LatLng(lat, lng));
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            // Handle errors, e.g., when the file does not exist
            Toast.makeText(context, "Error loading route from file", Toast.LENGTH_SHORT).show();
        }
        return route;
    }
}
