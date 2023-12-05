package com.example.explorex;

import android.os.Bundle;
import com.example.explorex.MapaFragment.OnRouteCompleteListener;
import com.google.android.gms.maps.model.LatLng;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TrasyFragment extends Fragment implements OnRouteCompleteListener {

    private TextView textViewTrasy;
    private List<RouteInfo> routeInfoList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RoutesAdapter routesAdapter;
    private MapaFragment.OnRouteCompleteListener onRouteCompleteListener;

    public TrasyFragment() {
        // Required empty public constructor
    }

    public static TrasyFragment newInstance() {
        return new TrasyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trasy, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapter with routeInfoList
        routesAdapter = new RoutesAdapter(routeInfoList);
        recyclerView.setAdapter(routesAdapter);

        textViewTrasy = rootView.findViewById(R.id.textViewTrasy);
        return rootView;
    }

    // Add this method to set the OnRouteCompleteListener
    public void setOnRouteCompleteListener(MapaFragment.OnRouteCompleteListener listener) {
        this.onRouteCompleteListener = listener;
    }

    private void informMapaFragment(String routeInfo) {
        if (onRouteCompleteListener != null) {
            onRouteCompleteListener.onRouteComplete(routeInfo);
        }
    }

    public void addRoute(List<LatLng> routePoints) {
        // Add the route to your list or perform any other necessary operations
        RouteInfo routeInfo = new RouteInfo(routePoints.get(0), routePoints.get(routePoints.size() - 1));
        routeInfoList.add(routeInfo);
        routesAdapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
    }
    // Add this method to update information in TrasyFragment
    public void updateRouteInfo(String routeInfo) {
        textViewTrasy.setText(routeInfo);
    }

    @Override
    public void onRouteComplete(String routeInfo) {
        // Handle the route information received from MapaFragment
        updateRouteInfo(routeInfo);
    }

    // Add a model class to represent route information
    public static class RouteInfo {
        private LatLng startPoint;
        private LatLng finishPoint;

        public RouteInfo(LatLng startPoint, LatLng finishPoint) {
            this.startPoint = startPoint;
            this.finishPoint = finishPoint;
        }

        public LatLng getStartPoint() {
            return startPoint;
        }

        public LatLng getFinishPoint() {
            return finishPoint;
        }
    }

    // Modify TrasyFragment to use a list of RouteInfo
    private static class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder> {
        private List<RouteInfo> routes;

        public RoutesAdapter(List<RouteInfo> routes) {
            this.routes = routes;
        }

        @NonNull
        @Override
        public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_item, parent, false);
            return new RouteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
            // Bind route data to the view
            RouteInfo routeInfo = routes.get(position);

            // Update the view with route information
            holder.textViewStartPoint.setText("Start: " + routeInfo.getStartPoint().toString());
            holder.textViewFinishPoint.setText("Finish: " + routeInfo.getFinishPoint().toString());
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }

        // ViewHolder class
        public static class RouteViewHolder extends RecyclerView.ViewHolder {
            // Add your route item views here
            TextView textViewStartPoint;
            TextView textViewFinishPoint;

            public RouteViewHolder(@NonNull View itemView) {
                super(itemView);
                // Initialize your route item views
                textViewStartPoint = itemView.findViewById(R.id.textViewStartPoint);
                textViewFinishPoint = itemView.findViewById(R.id.textViewFinishPoint);
            }
        }
    }
}
