package com.netraapp.rxquality;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netraapp.rxquality.db.DatabaseHelper;

public class InfoActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        databaseHelper = new DatabaseHelper(getApplicationContext());

        Button deleteMarkerButton = findViewById(R.id.deleteMarkerButton);
        deleteMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteMarkerDialog();
            }
        });

        displayData();
    }

    // Méthode pour afficher la boîte de dialogue de suppression du marqueur
    private void showDeleteMarkerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Supprimer un marqueur");
        builder.setMessage("Veuillez entrer l'ID du marqueur à supprimer");

        // Ajouter un champ de texte dans la boîte de dialogue
        final EditText input = new EditText(this);
        builder.setView(input);

        // Ajouter le bouton de suppression
        builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Récupérer l'ID du champ de texte
                String markerIdString = input.getText().toString();

                // Vérifier si l'ID est non vide
                if (!markerIdString.isEmpty()) {
                    // Convertir l'ID en entier
                    long markerId = Long.parseLong(markerIdString);

                    // Appeler la méthode pour supprimer le marqueur
                    deleteMarker(markerId);
                } else {
                    // Afficher un message si l'ID est vide
                    showToast("Veuillez entrer un ID valide");
                }
            }
        });

        // Ajouter le bouton d'annulation
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Méthode pour supprimer le marqueur de la base de données
    private void deleteMarker(long markerId) {
        boolean success = databaseHelper.deleteMarker(markerId);

        if (success) {
            showToast("Le marqueur a été supprimé avec succès");
            displayData();
        } else {
            showToast("Échec de la suppression du marqueur");
        }
    }

    // Méthode utilitaire pour afficher les messages toast
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayData() {
        displayNetworkInfoData();
        displayMarkerData();
    }

    private void displayMarkerData() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Sélectionner toutes les colonnes de la table des marqueurs
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_MARKERS;
        Cursor cursor = db.rawQuery(query, null);

        TextView markerTextView = findViewById(R.id.markerTextView);
        StringBuilder markerData = new StringBuilder();

        // Parcourir les résultats et ajouter les données à markerData
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
            String snippet = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SNIPPET));

            markerData.append("    ID: ")
                    .append(id).append("\n")
                    .append("   Latitude: ")
                    .append(latitude).append("\n")
                    .append("   Longitude: ")
                    .append(longitude).append("\n")
                    .append("   Title: ")
                    .append(title).append("\n")
                    .append("   Snippet: ")
                    .append(snippet).append("\n")
                    .append("\n")
                    .append("\n");
        }
        markerData.append("=====================");


        // Afficher les données dans le TextView
        markerTextView.setText(markerData.toString());

        // Fermer le curseur et la base de données
        cursor.close();
        db.close();
    }

    private void displayNetworkInfoData() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Sélectionner toutes les colonnes de la table des informations réseau
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NETWORK_INFO;
        Cursor cursor = db.rawQuery(query, null);

        TextView networkInfoTextView = findViewById(R.id.networkInfoTextView);
        StringBuilder networkInfoData = new StringBuilder();

        // Parcourir les résultats et ajouter les données à networkInfoData
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            long markerId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MARKER_ID));
            String networkType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NETWORK_TYPE));
            int signalStrength = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_STRENGTH));
            int signalCoverage = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVERAGE));
            int signalFrequency = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY));
            int linkSpeed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINK_SPEED));
            String signalQuality = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_QUALITY));

            networkInfoData.append("    ID: ").append(id).append("\n")
                    .append("   Marker ID: ").append(markerId).append("\n")
                    .append("   Network Type: ").append(networkType).append("\n")
                    .append("   Signal Strength: ").append(signalStrength).append(" dBm").append("\n")
                    .append("   Signal Coverage: ").append(signalCoverage).append(" dBm").append("\n")
                    .append("   Signal Frequency: ").append(signalFrequency).append(" Hz").append("\n")
                    .append("   Link Speed: ").append(linkSpeed).append(" Mb/s").append("\n")
                    .append("   Signal Quality: ").append(signalQuality).append("\n")
                    .append("\n")
                    .append("\n");

        }

        // Afficher les données dans le TextView
        networkInfoTextView.setText(networkInfoData.toString());

        // Fermer le curseur et la base de données
        cursor.close();
        db.close();
    }
}