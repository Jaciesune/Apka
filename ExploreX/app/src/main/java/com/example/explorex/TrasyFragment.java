package com.example.explorex;

import android.os.Bundle;
import com.example.explorex.MapaFragment.OnRouteCompleteListener;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TrasyFragment extends Fragment implements OnRouteCompleteListener {

    private TextView textViewTrasy;
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

    // Add this method to update information in TrasyFragment
    public void updateRouteInfo(String routeInfo) {
        textViewTrasy.setText(routeInfo);
    }


    @Override
    public void onRouteComplete(String routeInfo) {
        // Handle the route information received from MapaFragment
        updateRouteInfo(routeInfo);
    }
}