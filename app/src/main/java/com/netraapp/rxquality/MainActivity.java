package com.netraapp.rxquality;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.netraapp.rxquality.db.DatabaseHelper;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.netraapp.rxquality.enums.SignalQuality;
import com.netraapp.rxquality.model.MarkerNetworkInfoWrapper;
import com.netraapp.rxquality.model.NMarker;
import com.netraapp.rxquality.model.NetworkInfo;
import com.netraapp.rxquality.repository.NetworkInfoRepository;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private DatabaseHelper databaseHelper;
    private Location lastKnownLocation;
    private NetworkInfoRepository networkInfoRepository;
    private List<NMarker> markers;

    private Button infoReseauxButton;

    // Déclarer une variable pour suivre si la localisation a déjà été affichée
    private boolean locationDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        networkInfoRepository = new NetworkInfoRepository();

        // Vérifier et demander les autorisations de localisation
        if (checkLocationPermission()) {
            initMap();
        } else {
            requestLocationPermission();
        }

        // Bouton pour accéder aux informations enregistrées
        Button infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InfoActivity.class));
            }
        });

        // Bouton pour afficher la position actuelle
        Button showCurrentLocationButton = findViewById(R.id.showCurrentLocationButton);
        showCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Vérifier si la localisation a déjà été affichée
                if (!locationDisplayed) {
                    infoReseauxButton.setEnabled(true);
                    showCurrentLocation();
                    locationDisplayed = true;

                    // Désactiver temporairement le bouton pour éviter les clics répétés
                    showCurrentLocationButton.setEnabled(false);

                    // Réactiver le bouton après un certain délai (par exemple, 2 secondes)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showCurrentLocationButton.setEnabled(true);
                        }
                    }, 999999999);
                }
            }
        });

        infoReseauxButton = findViewById(R.id.infoReseauxButton);
        infoReseauxButton.setEnabled(false);
        infoReseauxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onInfoReseauxButtonClick();
            }
        });

        /*
        Button insertDataButton = findViewById(R.id.insertDataButton);
        // Ajouter un écouteur d'événements au bouton

        insertDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Appeler la méthode pour insérer des données lorsque le bouton est cliqué
                databaseHelper.onInsertDataButtonClick(v);
            }
        });
        */
    }

    private void onInfoReseauxButtonClick() {
        // Récupérer les informations réseau
        MarkerNetworkInfoWrapper networkInfo = (MarkerNetworkInfoWrapper) getNetworkInfo();

        // Afficher les informations réseau dans une boîte de dialogue
        String printNetworkInfo = "\nLatitude : " + networkInfo.getMarker().getLatitude() +
                                    "\n\nLongitude : " + networkInfo.getMarker().getLongitude() +
                                    "\n\nType de réseau : " + networkInfo.getNetworkInfo().getNetworkType() +
                                    "\n\nForce du signal : " + networkInfo.getNetworkInfo().getSignalStrength() + " dBm" +
                                    "\n\nFréquence : " + networkInfo.getNetworkInfo().getFrequency() + " Hz" +
                                    "\n\nVitesse de liaison : " + networkInfo.getNetworkInfo().getLinkSpeed() + " Mb/s" +
                                    "\n\nCouverture : " + networkInfo.getNetworkInfo().getCoverage() + " dBm" +
                                    "\n\nQualité du signal : " + networkInfo.getNetworkInfo().getSignalQuality()  ;

        showNetworkInfoDialog(printNetworkInfo, networkInfo);
    }

    // Initialiser la carte Google Maps
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    // Vérifier si les autorisations de localisation sont accordées
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Demander les autorisations de localisation
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    // Gérer la réponse de demande d'autorisation
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap();
            }
        } else if (requestCode == PERMISSION_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // La permission a été accordée, vous pouvez appeler à nouveau getNetworkInfo ici
//                String networkInfo = getNetworkInfo();
                // Faites quelque chose avec les informations réseau (par exemple, affichez-les dans une TextView)
                // textView.setText(networkInfo);
            } else {
                // La permission a été refusée, vous pouvez informer l'utilisateur ou prendre d'autres mesures
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        markers = networkInfoRepository.findAllMarker(new View(getApplicationContext()));

        // Ajouter un écouteur de clic sur les marqueurs
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Obtenir le marqueur correspondant à partir de la liste des marqueurs
                NMarker clickedMarker = getClickedMarker(marker);
                if (clickedMarker != null) {
                    // Afficher les détails du réseau dans une boîte de dialogue
                    showNetworkDetailsDialog(clickedMarker);
                    return true; // Indiquer que le clic sur le marqueur est géré
                }
                return false;
            }
        });

        // Ajoutez un marqueur pour chaque position
        for (int i = 0; i < markers.size(); i++) {

            NMarker marker = markers.get(i);
            long id = marker.getId();
            double latitude = marker.getLatitude();
            double longitude = marker.getLongitude();
            String title = marker.getTitle();
            String snippet = marker.getSnippet();

            MarkerNetworkInfoWrapper markerNetworkInfoWrapper = networkInfoRepository.getMarkerNetworkInfoByMarkerId(id, new View(getApplicationContext()));

            float bitmapDescriptorFactory = 0;

            if (markerNetworkInfoWrapper.getNetworkInfo().getSignalQuality().equals(SignalQuality.FAIBLE)) {
                bitmapDescriptorFactory = BitmapDescriptorFactory.HUE_RED;
            } else if (markerNetworkInfoWrapper.getNetworkInfo().getSignalQuality().equals(SignalQuality.MOYEN)) {
                bitmapDescriptorFactory = BitmapDescriptorFactory.HUE_ORANGE;
            } else if (markerNetworkInfoWrapper.getNetworkInfo().getSignalQuality().equals(SignalQuality.FORT)) {
                bitmapDescriptorFactory = BitmapDescriptorFactory.HUE_GREEN;
            }

            addMarker(latitude, longitude, title, snippet, BitmapDescriptorFactory.defaultMarker(bitmapDescriptorFactory));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Réagir au clic sur le marqueur
        Toast.makeText(this, "Clic sur le marqueur: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true; // Si vous retournez true, le clic sur le marqueur ne déclenchera pas le comportement par défaut (afficher l'info-bulle)
    }


    private void showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        locationRequest = new LocationRequest();
                        locationRequest.setInterval(1000);
                        locationRequest.setFastestInterval(1000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new com.google.android.gms.location.LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                // Mettez à jour la dernière position connue
                                lastKnownLocation = location;

                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                                // Récupérer les informations réseau
//                                String networkInfo = getNetworkInfo();

                                // Afficher les informations réseau dans une boîte de dialogue
//                                showNetworkInfoDialog(networkInfo);
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

//        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
    @Override
    public void onLocationChanged(Location location) {
        // Mettez à jour la dernière position connue
        lastKnownLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }
    private void addMarker(double latitude, double longitude, String title, String snippet, BitmapDescriptor icon) {
        if (googleMap != null) {
            Log.d("AddMarker", "Adding marker at (" + latitude + ", " + longitude + ")");
            LatLng position = new LatLng(latitude, longitude);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(icon);

            Marker marker = googleMap.addMarker(markerOptions);

            // Ajouter une étiquette près du marqueur
            addLabelToMarker(marker, title);
        } else {
            Log.e("AddMarker", "googleMap is null");
        }
    }
    private void addLabelToMarker(Marker marker, String label) {
        if (googleMap != null) {
            LatLng position = marker.getPosition();

            // Créer un GroundOverlay avec le texte
            GroundOverlayOptions labelOverlay = new GroundOverlayOptions()
                    .position(position, 100) // Ajustez le rayon selon vos besoins
                    .transparency(0.5f) // Ajustez la transparence si nécessaire
                    .zIndex(3) // Ajustez l'indice Z pour que l'étiquette apparaisse au-dessus des marqueurs
                    .bearing(0) // Ajustez la rotation si nécessaire
                    .image(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, getMarkerLabelView(label))));

            // Ajouter l'overlay à la carte
            googleMap.addGroundOverlay(labelOverlay);
        }
    }
    // Créer une vue avec le texte pour GroundOverlay
    private View getMarkerLabelView(String label) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(Color.BLACK);
        labelView.setBackgroundColor(Color.WHITE);
        labelView.setPadding(10, 5, 10, 5);

        // Ajuster la taille du texte ici
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8); // Utilisez la taille souhaitée

        return labelView;
    }
    // Convertir une vue en Bitmap
    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @SuppressLint("SetTextI18n")
    private void showNetworkDetailsDialog(NMarker marker) {
        MarkerNetworkInfoWrapper markerNetworkInfoWrapper = networkInfoRepository.getMarkerNetworkInfoByMarkerId(marker.getId(), new View(getApplicationContext()));

        // Créez une vue personnalisée pour l'AlertDialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.layout_copy_button, null);

        // Affichez les détails du réseau dans la vue personnalisée
        TextView txtNetworkDetails = dialogView.findViewById(R.id.txtNetworkDetails);
        txtNetworkDetails.setText("\nType de réseau : " + markerNetworkInfoWrapper.getNetworkInfo().getNetworkType() + "\n\n" +
                "Latitude : " + markerNetworkInfoWrapper.getMarker().getLatitude() + "\n\n" +
                "Longitude : " + markerNetworkInfoWrapper.getMarker().getLongitude() + "\n\n" +
                "Force du signal : " + markerNetworkInfoWrapper.getNetworkInfo().getSignalStrength() + " dBm\n\n" +
                "Fréquence : " + markerNetworkInfoWrapper.getNetworkInfo().getFrequency() + " Hz\n\n" +
                "Vitesse de liaison : " + markerNetworkInfoWrapper.getNetworkInfo().getLinkSpeed() + " Mb/s\n\n" +
                "Couverture : " + markerNetworkInfoWrapper.getNetworkInfo().getCoverage() + " dBm\n\n" +
                "Qualité du signal : " + markerNetworkInfoWrapper.getNetworkInfo().getSignalQuality());

        // Ajoutez un bouton pour copier dans le presse-papiers
        Button btnCopy = dialogView.findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Obteneir les informations du réseau sous forme de chaîne
                String networkInfoString = txtNetworkDetails.getText().toString();

                // Copier la chaîne dans le presse-papiers
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Network Info", networkInfoString);
                clipboard.setPrimaryClip(clip);

                // Affichez un message pour indiquer que les informations ont été copiées
                Toast.makeText(MainActivity.this, "Informations copiées dans le presse-papiers", Toast.LENGTH_SHORT).show();
            }
        });

        // Créez et affichez l'AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Détails du réseau")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Cela ferme simplement la boîte de dialogue
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Méthode pour obtenir le NMarker correspondant à partir du Marker cliqué
    private NMarker getClickedMarker(Marker clickedMarker) {
        for (NMarker marker : markers) {
            if (marker.getLatitude() == clickedMarker.getPosition().latitude &&
                    marker.getLongitude() == clickedMarker.getPosition().longitude) {
                return marker;
            }
        }
        return null;
    }

    // Méthode pour récupérer les informations réseau
    private Object getNetworkInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String networkType = "";
        int signalStrength = 0;
        int frequency = 0;
        int linkSpeed = 0;
        int coverage = 0;
        MarkerNetworkInfoWrapper markerNetworkInfoWrapper = new MarkerNetworkInfoWrapper();
        NMarker nMarker = new NMarker();
        NetworkInfo networkInfo = new NetworkInfo();

        if (telephonyManager != null) {
            // Récupérer le type de réseau
            // Vérifier la permission READ_PHONE_STATE
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // Demander la permission si elle n'a pas été accordée
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
                // Retourner une chaîne indiquant que la permission est manquante
                return "Permission manquante : READ_PHONE_STATE";
            }

            int networkTypeInt = telephonyManager.getNetworkType();
            networkType = getNetworkTypeString(networkTypeInt);

            // Récupérer la force du signal
            signalStrength = telephonyManager.getSignalStrength().getLevel();

            if ("2G".equals(networkType)) {
                // Logique pour le réseau 2G (GSM, CDMA, etc.)
                frequency = get2GFrequency(telephonyManager);
            } else if ("3G".equals(networkType)) {
                // Logique pour le réseau 3G (UMTS, etc.)
                frequency = get3GFrequency(telephonyManager);
            } else if ("4G".equals(networkType)) {
                // Logique pour le réseau 4G (LTE)
                frequency = get4GFrequency(telephonyManager);
            } else if ("5G".equals(networkType)) {
                // Logique pour le réseau 5G (NR)
                frequency = get5GFrequency(telephonyManager);
            } else {
                // Logique pour d'autres types de réseau ou inconnus
            }

            // Récupérer la vitesse de liaison
            linkSpeed = telephonyManager.getDataNetworkType();

            // Récupérer la couverture
            coverage = telephonyManager.getPhoneCount();

            nMarker.setId(0);
            nMarker.setLatitude(lastKnownLocation.getLatitude());
            nMarker.setLongitude(lastKnownLocation.getLongitude());
            nMarker.setTitle("Marker");
            nMarker.setSnippet("Marker");

            networkInfo.setId(0);
            networkInfo.setMarkerId(0);
            networkInfo.setNetworkType(networkType);
            networkInfo.setSignalStrength(signalStrength);
            networkInfo.setFrequency(frequency);
            networkInfo.setLinkSpeed(linkSpeed);
            networkInfo.setCoverage(coverage);
            networkInfo.setSignalQuality(databaseHelper.determineSignalQuality(
                    networkType, signalStrength, frequency, linkSpeed, coverage
            ));

            markerNetworkInfoWrapper.setMarker(nMarker);
            markerNetworkInfoWrapper.setNetworkInfo(networkInfo);
        }

        return markerNetworkInfoWrapper;
    }
    private String getNetworkTypeString(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "Unknown";
        }
    }

    // Méthode pour obtenir la fréquence pour le réseau 2G
    private int get2GFrequency(TelephonyManager telephonyManager) {
        int frequency = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            CellInfo cellInfo = getCellInfo(telephonyManager);
            if (cellInfo instanceof CellInfoGsm) {
                CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                frequency = cellIdentityGsm.getArfcn();
            } else if (cellInfo instanceof CellInfoCdma) {
                // Logique pour CDMA (si nécessaire)
            }
        }

        return frequency;
    }

    // Méthode pour obtenir la fréquence pour le réseau 3G
    private int get3GFrequency(TelephonyManager telephonyManager) {
        int frequency = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            CellInfo cellInfo = getCellInfo(telephonyManager);
            if (cellInfo instanceof CellInfoWcdma) {
                CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) cellInfo).getCellIdentity();
                frequency = cellIdentityWcdma.getUarfcn();
            }
        }

        return frequency;
    }

    // Méthode pour obtenir la fréquence pour le réseau 4G
    private int get4GFrequency(TelephonyManager telephonyManager) {
        int frequency = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            CellInfo cellInfo = getCellInfo(telephonyManager);
            if (cellInfo instanceof CellInfoLte) {
                CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                frequency = cellIdentityLte.getEarfcn();
            }
        }

        return frequency;
    }

    // Méthode pour obtenir la fréquence pour le réseau 5G
    private int get5GFrequency(TelephonyManager telephonyManager) {
        int frequency = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CellInfo cellInfo = getCellInfo(telephonyManager);
            if (cellInfo instanceof CellInfoNr) {
                CellIdentityNr cellIdentityNr = (CellIdentityNr) ((CellInfoNr) cellInfo).getCellIdentity();
                frequency = cellIdentityNr.getNrarfcn();
            }
        }

        return frequency;
    }

    // Méthode utilitaire pour obtenir les informations de cellule
    @SuppressLint("MissingPermission")
    private CellInfo getCellInfo(TelephonyManager telephonyManager) {
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        if (cellInfos != null && cellInfos.size() > 0) {
            return cellInfos.get(0);
        }
        return null;
    }
    private void showNetworkInfoDialog(String printNetworkInfo, MarkerNetworkInfoWrapper networkInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Informations réseau")
                .setMessage(printNetworkInfo)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Cela ferme simplement la boîte de dialogue
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Enregistrer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseHelper.insertMyPosition(new View(getApplicationContext()), networkInfo);
                    }
                })
        ;

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
