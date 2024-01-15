package com.example.explorex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

public class TrasyAdapter extends RecyclerView.Adapter<TrasyAdapter.ViewHolder> {

    private ArrayList<String> savedRouteFilePaths;
    private TrasyAdapterListener listener;

    public TrasyAdapter(ArrayList<String> savedRouteFilePaths, TrasyAdapterListener listener) {
        this.savedRouteFilePaths = savedRouteFilePaths;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String filePath = savedRouteFilePaths.get(position);
        File file = new File(filePath);

        // Removing file extensions from file names
        String fileName = removeFileExtension(file.getName());

        holder.tvFileName.setText(fileName);
        holder.setPosition(position);

        // Handling click on btnShowRoute
        holder.btnShowRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getCurrentPosition() != RecyclerView.NO_POSITION) {
                    String filePath = savedRouteFilePaths.get(holder.getCurrentPosition());
                    listener.showRouteOnMap(filePath);
                }
            }
        });

        // Handling click on btnDelete
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog(filePath, holder.itemView);
            }
        });
    }

    // Method to remove file extension from the file name
    private String removeFileExtension(String fileName) {
        int lastDotPosition = fileName.lastIndexOf(".");
        if (lastDotPosition != -1) {
            return fileName.substring(0, lastDotPosition);
        } else {
            return fileName;
        }
    }

    @Override
    public int getItemCount() {
        return savedRouteFilePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvFileName;
        public ImageButton btnShowRoute;
        public ImageButton btnDelete;
        private int position;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            btnShowRoute = itemView.findViewById(R.id.btnShowRoute);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Handling click on btnShowRoute
            btnShowRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position != RecyclerView.NO_POSITION) {
                        String filePath = savedRouteFilePaths.get(position);
                        listener.showRouteOnMap(filePath);
                    }
                }
            });
        }

        // Set the position when binding the data
        public void setPosition(int position) {
            this.position = position;
        }

        // Getter method for position
        public int getCurrentPosition() {
            return position;
        }
    }

    // Method to display delete confirmation dialog
    private void showDeleteConfirmationDialog(String filePath, View itemView) {

        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setTitle("Potwierdź usunięcie");
        builder.setMessage("Czy jesteś pewien że chcesz usunąć ten plik?");

        // Add a button to confirm deletion
        builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Delete the file after confirmation
                deleteFile(filePath, itemView);
            }
        });

        // Add a button to cancel deletion
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do nothing on cancellation
            }
        });

        // Show the dialog
        builder.show();
    }

    // Method to delete a file
    private void deleteFile(String filePath, View itemView) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(itemView.getContext(), "Plik został usunięty", Toast.LENGTH_SHORT).show();
                // Refresh the list after deleting the file
                listener.updateRoutePaths(getUpdatedRoutesList());
            } else {
                Toast.makeText(itemView.getContext(), "Błąd podczas usuwania pliku", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to refresh the list of routes after deleting a file
    private ArrayList<String> getUpdatedRoutesList() {
        ArrayList<String> updatedRoutePaths = new ArrayList<>();
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/" + MapaFragment.Constants.ROUTES_DIRECTORY);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                updatedRoutePaths.add(file.getAbsolutePath());
            }
        }

        return updatedRoutePaths;
    }

    // Method to update the list of routes in TrasyFragment
    public void updateRoutePaths(ArrayList<String> updatedRoutePaths) {
        this.savedRouteFilePaths = updatedRoutePaths;
        notifyDataSetChanged();
    }

    public interface TrasyAdapterListener {
        void updateRoutePaths(ArrayList<String> updatedRoutePaths);
        void showRouteOnMap(String filePath);
    }
}