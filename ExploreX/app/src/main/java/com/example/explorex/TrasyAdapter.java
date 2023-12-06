package com.example.explorex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrasyAdapter extends RecyclerView.Adapter<TrasyAdapter.ViewHolder> {

    private ArrayList<GroupedLatLng> routes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public TrasyAdapter(ArrayList<GroupedLatLng> routes, OnItemClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    public void updateRoutes(ArrayList<GroupedLatLng> updatedRoutes) {
        this.routes = updatedRoutes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupedLatLng route = routes.get(position);
        // Assuming tvRoute is a TextView in your item_route layout
        holder.tvRoute.setText(String.format("Group %d - Lat: %f, Lng: %f", route.getGroup(), route.getLatLng().latitude, route.getLatLng().longitude));

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(adapterPosition);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageButton btnDelete;
        TextView tvRoute;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
