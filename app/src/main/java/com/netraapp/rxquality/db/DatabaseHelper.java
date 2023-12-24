package com.netraapp.rxquality.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Toast;

import com.netraapp.rxquality.enums.SignalQuality;
import com.netraapp.rxquality.model.MarkerNetworkInfoWrapper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rx_info.db";
    private static final int DATABASE_VERSION = 1;

    // Table pour stocker les informations des marqueurs
    public static final String TABLE_MARKERS = "markers";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SNIPPET = "snippet";

    // Table pour stocker les informations réseau
    public static final String TABLE_NETWORK_INFO = "network_info";
    public static final String COLUMN_MARKER_ID = "marker_id";
    public static final String COLUMN_NETWORK_TYPE = "network_type";
    public static final String COLUMN_SIGNAL_STRENGTH = "signal_strength";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_LINK_SPEED = "link_speed";
    public static final String COLUMN_COVERAGE = "coverage";
    public static final String COLUMN_SIGNAL_QUALITY = "signal_quality";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table pour les marqueurs
        String createMarkersTable = "CREATE TABLE " + TABLE_MARKERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LATITUDE + " REAL,"
                + COLUMN_LONGITUDE + " REAL,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_SNIPPET + " TEXT"
                + ")";
        db.execSQL(createMarkersTable);

        // Création de la table pour les informations réseau
        String createNetworkInfoTable = "CREATE TABLE " + TABLE_NETWORK_INFO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MARKER_ID + " INTEGER,"
                + COLUMN_NETWORK_TYPE + " TEXT,"
                + COLUMN_SIGNAL_STRENGTH + " FLOAT,"
                + COLUMN_FREQUENCY + " FLOAT,"
                + COLUMN_LINK_SPEED + " FLOAT,"
                + COLUMN_COVERAGE + " FLOAT,"
                + COLUMN_SIGNAL_QUALITY + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_MARKER_ID + ") REFERENCES " + TABLE_MARKERS + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(createNetworkInfoTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean deleteMarker(long markerId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Supprimer d'abord les enregistrements de la table network_info liés à ce marqueur
        db.delete(TABLE_NETWORK_INFO, COLUMN_MARKER_ID + " = ?", new String[]{String.valueOf(markerId)});

        // Ensuite, supprimer le marqueur de la table markers
        int rowsAffected = db.delete(TABLE_MARKERS, COLUMN_ID + " = ?", new String[]{String.valueOf(markerId)});

        // Fermer la connexion à la base de données
        db.close();

        // Retourner true si au moins une ligne a été supprimée, sinon false
        return rowsAffected > 0;
    }


    public long insertMarker(double latitude, double longitude, String title, String snippet) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_SNIPPET, snippet);

        return db.insert(TABLE_MARKERS, null, values);
    }

    public long insertMarkerIfNotExists(double latitude, double longitude, String title, String snippet) {
        // Vérifier si le marqueur existe déjà
        if (!markerExists(latitude, longitude)) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_LATITUDE, latitude);
            values.put(COLUMN_LONGITUDE, longitude);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_SNIPPET, snippet);

            return db.insert(TABLE_MARKERS, null, values);
        } else {
            // Le marqueur existe déjà, retourner -1 pour indiquer l'absence d'insertion
            return -1;
        }
    }

    private boolean markerExists(double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Arrondir les paramètres de latitude et de longitude à 2 chiffres après la virgule
        latitude = roundToDecimalPlaces(latitude, 2);
        longitude = roundToDecimalPlaces(longitude, 2);

        String query = "SELECT * FROM " + TABLE_MARKERS +
                " WHERE ROUND(" + COLUMN_LATITUDE + ", 2) = " + latitude +
                " AND ROUND(" + COLUMN_LONGITUDE + ", 2) = " + longitude;

        Cursor cursor = db.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return exists;
    }

    // Fonction pour arrondir un nombre à un certain nombre de chiffres après la virgule
    private double roundToDecimalPlaces(double value, int decimalPlaces) {
        double multiplier = Math.pow(10, decimalPlaces);
        return Math.round(value * multiplier) / multiplier;
    }




    public long insertNetworkInfo(long markerId, String networkType, float signalStrength, float frequency, float linkSpeed, float coverage, SignalQuality signalQuality) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MARKER_ID, markerId);
        values.put(COLUMN_NETWORK_TYPE, networkType);
        values.put(COLUMN_SIGNAL_STRENGTH, signalStrength);
        values.put(COLUMN_FREQUENCY, frequency);
        values.put(COLUMN_LINK_SPEED, linkSpeed);
        values.put(COLUMN_COVERAGE, coverage);
        values.put(COLUMN_SIGNAL_QUALITY, signalQuality.toString()); // Assuming SignalQuality is an enum

        return db.insert(TABLE_NETWORK_INFO, null, values);
    }

    // Méthode appelée lorsque le bouton est cliqué
    /*
    public void onInsertDataButtonClick(View view) {
        // Obtenez une instance de votre DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());

        // Insérer des enregistrements dans les tables
        long markerId1 = insertMarker(14.753032527354652, -17.38335939613469, "Marqueur 1", "Description 1");
        long markerId2 = insertMarker(14.750892595600797, -17.384611907116636, "Marqueur 2", "Description 2");
        long markerId3 = insertMarker(14.754912428694185, -17.37946128304819, "Marqueur 3", "Description 3");
        long markerId4 = insertMarker(14.751158021080508, -17.38225891459588, "Marqueur 4", "Description 4");
        long markerId5 = insertMarker(14.753926392423725, -17.3792260136731, "Marqueur 5", "Description 5");

        // Insérer des enregistrements dans la table network_info pour chaque marqueur
        insertNetworkInfo(1, "LTE", 80, 1000, 150, 4, SignalQuality.FORT);
        insertNetworkInfo(2, "5G", 90, 1200, 200, 5, SignalQuality.FORT);
        insertNetworkInfo(3, "3G", 70, 800, 100, 3, SignalQuality.MOYEN);
        insertNetworkInfo(4, "4G", 85, 1100, 180, 4, SignalQuality.FORT);
        insertNetworkInfo(5, "LTE", 75, 900, 120, 3, SignalQuality.MOYEN);

        // Vous pouvez également afficher un message à l'utilisateur pour indiquer que les données ont été insérées
        Toast.makeText(view.getContext(), "Données insérées avec succès", Toast.LENGTH_SHORT).show();
    }
    */

    public void onInsertDataButtonClick(View view) {
        // Obtenez une instance de votre DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());

        // Insérer des enregistrements dans les tables (en vérifiant l'existence d'abord)
        long markerId1 = databaseHelper.insertMarkerIfNotExists(14.753032527354652, -17.38335939613469, "Marqueur 1", "Description 1");
        long markerId2 = databaseHelper.insertMarkerIfNotExists(14.750892595600797, -17.384611907116636, "Marqueur 2", "Description 2");
        long markerId3 = databaseHelper.insertMarkerIfNotExists(14.754912428694185, -17.37946128304819, "Marqueur 3", "Description 3");
        long markerId4 = databaseHelper.insertMarkerIfNotExists(14.751158021080508, -17.38225891459588, "Marqueur 4", "Description 4");
        long markerId5 = databaseHelper.insertMarkerIfNotExists(14.753926392423725, -17.3792260136731, "Marqueur 5", "Description 5");

        // Vérifiez l'id retourné pour chaque insertion
        if (markerId1 != -1) {
            databaseHelper.insertNetworkInfo(1, "LTE", 80, 1000, 150, 4,
                                 determineSignalQuality("LTE", 80, 1000, 150, 4));
        }

        if (markerId2 != -1) {
            databaseHelper.insertNetworkInfo(2, "5G", 90, 1200, 200, 5,
                                 determineSignalQuality("5G", 90, 1200, 200, 5));
        }

        if (markerId3 != -1) {
            databaseHelper.insertNetworkInfo(3, "3G", 70, 200, 50, 3,
                                 determineSignalQuality("3G", 70, 200, 50, 3));
        }

        if (markerId4 != -1) {
            databaseHelper.insertNetworkInfo(4, "4G", 85, 1100, 180, 4,
                                 determineSignalQuality("4G", 85, 1100, 180, 4));
        }

        if (markerId5 != -1) {
            databaseHelper.insertNetworkInfo(5, "LTE", 75, 900, 120, 3,
                                 determineSignalQuality("LTE", 75, 900, 120, 3));
        }

        // Vous pouvez également afficher un message à l'utilisateur pour indiquer que les données ont été insérées
        Toast.makeText(view.getContext(), "Données insérées avec succès", Toast.LENGTH_SHORT).show();
    }

    public void insertMyPosition(View view, MarkerNetworkInfoWrapper markerNetworkInfoWrapper){
        // Obtenez une instance de votre DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(view.getContext());

        double latitude = markerNetworkInfoWrapper.getMarker().getLatitude();
        double longitude = markerNetworkInfoWrapper.getMarker().getLongitude();
        String title = markerNetworkInfoWrapper.getMarker().getTitle();
        String snippet = markerNetworkInfoWrapper.getMarker().getSnippet();

        String networkType = markerNetworkInfoWrapper.getNetworkInfo().getNetworkType();
        float signalStrength = markerNetworkInfoWrapper.getNetworkInfo().getSignalStrength();
        float frequency = markerNetworkInfoWrapper.getNetworkInfo().getFrequency();
        float linkSpeed = markerNetworkInfoWrapper.getNetworkInfo().getLinkSpeed();
        float coverage = markerNetworkInfoWrapper.getNetworkInfo().getCoverage();
        SignalQuality signalQuality = markerNetworkInfoWrapper.getNetworkInfo().getSignalQuality();

        // Insérer des enregistrements dans les tables (en vérifiant l'existence d'abord)
        long markerId = databaseHelper.insertMarkerIfNotExists(latitude, longitude, title, snippet);

        // Vérifiez l'id retourné pour chaque insertion
        if (markerId != -1) {
            databaseHelper.insertNetworkInfo(markerId, networkType, signalStrength, frequency, linkSpeed, coverage, signalQuality);
            Toast.makeText(view.getContext(), "Données insérées avec succès", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(view.getContext(), "Données redondant non enregistrer", Toast.LENGTH_LONG).show();
        }

    }
    public SignalQuality determineSignalQuality(String networkType, int signalStrength, int frequency, int linkSpeed, int coverage) {

        SignalQuality signalQuality = null;

        if ("LTE".equals(networkType)) {
            if (signalStrength > 80 && frequency > 1000 && linkSpeed > 100 && coverage > 3) {
                signalQuality = SignalQuality.FORT;
            } else if (signalStrength > 70 && frequency > 800 && linkSpeed > 80 && coverage > 2) {
                signalQuality = SignalQuality.MOYEN;
            } else {
                signalQuality = SignalQuality.FAIBLE;
            }
        } else if ("4G".equals(networkType)) {
            if (signalStrength > 80 && frequency > 1000 && linkSpeed > 100 && coverage > 3) {
                signalQuality = SignalQuality.FORT;
            } else if (signalStrength > 70 && frequency > 800 && linkSpeed > 80 && coverage > 2) {
                signalQuality = SignalQuality.MOYEN;
            } else {
                signalQuality = SignalQuality.FAIBLE;
            }
        } else if ("5G".equals(networkType)) {
            if (signalStrength > 90 && frequency > 1200 && linkSpeed > 150 && coverage > 4) {
                signalQuality = SignalQuality.FORT;
            } else if (signalStrength > 80 && frequency > 1000 && linkSpeed > 120 && coverage > 3) {
                signalQuality = SignalQuality.MOYEN;
            } else {
                signalQuality = SignalQuality.FAIBLE;
            }
        } else if ("3G".equals(networkType)) {
            if (signalStrength > 60 && frequency > 600 && linkSpeed > 50 && coverage > 2) {
                signalQuality = SignalQuality.MOYEN;
            } else {
                signalQuality = SignalQuality.FAIBLE;
            }
        } else {
            // Autres types de réseau
            // Ajoutez des conditions pour d'autres types de réseau si nécessaire
        }

        return signalQuality;
    }



}
