package com.netraapp.rxquality.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.netraapp.rxquality.db.DatabaseHelper;
import com.netraapp.rxquality.enums.SignalQuality;
import com.netraapp.rxquality.model.NMarker;
import com.netraapp.rxquality.model.MarkerNetworkInfoWrapper;
import com.netraapp.rxquality.model.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class NetworkInfoRepository {
    public List<NMarker> findAllMarker(View view){
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Sélectionner toutes les colonnes de la table des marqueurs
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_MARKERS;
        Cursor cursor = db.rawQuery(query, null);

        List <NMarker> NMarkerList = new ArrayList<>();

        while (cursor.moveToNext()) {

            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
            String snippet = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SNIPPET));

            NMarker NMarker = new NMarker();
            NMarker.setId(id);
            NMarker.setLatitude(latitude);
            NMarker.setLongitude(longitude);
            NMarker.setTitle(title);
            NMarker.setSnippet(snippet);

            NMarkerList.add(NMarker);

        }

        cursor.close();
        db.close();
        return NMarkerList;
    }

    public List<NetworkInfo> findAllNetworkInfo(View view){

        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Sélectionner toutes les colonnes de la table des informations réseau
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NETWORK_INFO;
        Cursor cursor = db.rawQuery(query, null);

        List<NetworkInfo> networkInfoList = new ArrayList<>();

        while (cursor.moveToNext()) {

            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            long markerId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MARKER_ID));
            String networkType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NETWORK_TYPE));
            int signalStrength = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_STRENGTH));
            int signalCoverage = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVERAGE));
            int signalFrequency = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY));
            String signalQuality = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_QUALITY));

            NetworkInfo networkInfo = new NetworkInfo();
            networkInfo.setId(id);
            networkInfo.setMarkerId(markerId);
            networkInfo.setNetworkType(networkType);
            networkInfo.setSignalStrength(signalStrength);
            networkInfo.setCoverage(signalCoverage);
            networkInfo.setFrequency(signalFrequency);
            networkInfo.setSignalQuality(SignalQuality.valueOf(signalQuality));

            networkInfoList.add(networkInfo);

        }

        cursor.close();
        db.close();
        return networkInfoList;

    }

    public NetworkInfo getNetworkInfoByMarkerId(long markerId, View view) {
        NetworkInfo networkInfo = null;
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Définir la requête SQL avec une clause WHERE pour filtrer par marker_id
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NETWORK_INFO +
                " WHERE " + DatabaseHelper.COLUMN_MARKER_ID + " = ?";

        // Exécuter la requête avec le markerId en tant qu'argument
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(markerId)});

        // Vérifier s'il y a des résultats
        if (cursor != null && cursor.moveToFirst()) {

            // Construire un objet NetworkInfo à partir des données du curseur
            networkInfo = new NetworkInfo();
            networkInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            networkInfo.setMarkerId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MARKER_ID)));
            networkInfo.setNetworkType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NETWORK_TYPE)));
            networkInfo.setSignalStrength(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_STRENGTH)));
            networkInfo.setFrequency(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY)));
            networkInfo.setLinkSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINK_SPEED)));
            networkInfo.setCoverage(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVERAGE)));
            networkInfo.setSignalQuality(SignalQuality.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_QUALITY))));
        }

        // Fermer le curseur une fois que vous avez terminé
        if (cursor != null) {
            cursor.close();
        }

        // Fermer la base de données une fois que vous avez terminé
        db.close();

        return networkInfo;
    }

    // Méthode pour récupérer les informations du réseau et du marqueur en fonction du marker_id
    public MarkerNetworkInfoWrapper getMarkerNetworkInfoByMarkerId(long markerId, View view) {
        MarkerNetworkInfoWrapper result = null;
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Définir la requête SQL avec une clause JOIN
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_MARKERS +
                " JOIN " + DatabaseHelper.TABLE_NETWORK_INFO +
                " ON " + DatabaseHelper.TABLE_MARKERS + "." + DatabaseHelper.COLUMN_ID + " = " + DatabaseHelper.TABLE_NETWORK_INFO + "." + DatabaseHelper.COLUMN_MARKER_ID +
                " WHERE " + DatabaseHelper.TABLE_MARKERS + "." + DatabaseHelper.COLUMN_ID + " = ?";

        // Exécuter la requête avec le markerId en tant qu'argument
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(markerId)});

        // Vérifier s'il y a des résultats
        if (cursor != null && cursor.moveToFirst()) {
            // Construire un objet MarkerNetworkInfoWrapper avec les données du curseur
            result = new MarkerNetworkInfoWrapper();

            // Ajouter les informations du marqueur
            NMarker NMarker = new NMarker();
            NMarker.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TABLE_MARKERS + "." + DatabaseHelper.COLUMN_ID)));
            NMarker.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)));
            NMarker.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)));
            NMarker.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
            NMarker.setSnippet(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SNIPPET)));
            result.setMarker(NMarker);

            // Ajouter les informations du réseau
            NetworkInfo networkInfo = new NetworkInfo();
            networkInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TABLE_NETWORK_INFO + "." + DatabaseHelper.COLUMN_ID)));
            networkInfo.setMarkerId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MARKER_ID)));
            networkInfo.setNetworkType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NETWORK_TYPE)));
            networkInfo.setSignalStrength(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_STRENGTH)));
            networkInfo.setFrequency(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY)));
            networkInfo.setLinkSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LINK_SPEED)));
            networkInfo.setCoverage(cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVERAGE)));
            networkInfo.setSignalQuality(SignalQuality.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNAL_QUALITY))));

            result.setNetworkInfo(networkInfo);
        }

        // Fermer le curseur une fois que vous avez terminé
        if (cursor != null) {
            cursor.close();
        }

        // Fermer la base de données une fois que vous avez terminé
        db.close();

        return result;
    }

}
