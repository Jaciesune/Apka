package com.example.explorex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrasyAdapter extends RecyclerView.Adapter<TrasyAdapter.ViewHolder> {

    private ArrayList<String> savedRouteFilePaths;  // Correct variable name
    private OnItemClickListener listener;
    private OnItemClickListener showRouteListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);

        void onItemClick(int position);

        void onShowRouteClick(int position);
    }

    public TrasyAdapter(ArrayList<String> savedRouteFilePaths, OnItemClickListener listener) {
        this.savedRouteFilePaths = savedRouteFilePaths;
        this.listener = listener;
    }

    public void updateRoutePaths(ArrayList<String> updatedRoutePaths) {
        this.savedRouteFilePaths = updatedRoutePaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    public void setOnShowRouteClickListener(OnItemClickListener listener) {
        this.showRouteListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = savedRouteFilePaths.get(position);

        holder.tvFileName.setText("Route " + (position + 1));

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(holder.getAdapterPosition(), v);
            }
        });

        // Handle item click if needed
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        listener.onItemClick(adapterPosition);
                    }
                }
            }
        });

        holder.btnShowRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showRouteListener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        showRouteListener.onShowRouteClick(adapterPosition);
                    }
                }
            }
        });
    }

    private void showDeleteConfirmationDialog(final int position, View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this route?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onDeleteClick(position);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public int getItemCount() {
        return savedRouteFilePaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageButton btnDelete;
        public ImageButton btnShowRoute;
        public TextView tvFileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShowRoute = itemView.findViewById(R.id.btnShowRoute);
        }
    }
}
