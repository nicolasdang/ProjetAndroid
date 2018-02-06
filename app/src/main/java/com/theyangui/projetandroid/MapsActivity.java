package com.theyangui.projetandroid;


import android.Manifest;
import android.content.pm.PackageManager;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * ceci est en réalité la classe principale, ou nous effectuerons les fonctions lié à la map
 * la plupart des fonctions repose sur un même principe, les différents interaction de l'utilisateur
 * nous permets de récuperer une requete URL qui nous ressort
 * les information issu de la BDD de google elles seront ensuite afficher sur la map.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener
{


    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private LatLng positionMarker;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private SeekBar seek;

    int PROXIMITY_RADIUS = 10000;
    double latitude,longitude;

    /**
     * la classe est crée lorsque l'activité lié à la map est crée.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }
        // recupere la mapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        seek = (SeekBar) findViewById(R.id.seekBar2);
        final TextView seekBarValue = (TextView)findViewById(R.id.Detail);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            /**
             * lorsque la Seekbar change, cela va changer le texte informant l'utilisateur.
             * @param seek
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seek, int progress,
            boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText("Vous recherchez à " + String.valueOf(progress) + "Km");
            }




            @Override
            public void onStartTrackingTouch(SeekBar seek) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    /**
     * Lorsque la Map sera prête, cette fonction sera appellé et va nous permettre de la modifier.
     * nous appellerons également buildGoogleApiClient afin d'utiliser les fonctionnalité de l'api Google
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(this);
    }

    /**
     *Nous allons créer un client google  qui va permettre à notre application d'utiliser l'api google
     */
    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    /**
     * Si la position de l'utilisateur change, va enlever l'ancien marker et va en créer un à la position actuel.
     * va également récuperer les positions dans les variables afin de nous permettre de localiser l'utilisateur pour les
     * autres fonctions
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        markerOptions.title("PositionActuel");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }


    /**
     * va réagir lorsque les boutons sont sélectionné, à l'aide du switch
     * nous pouvons agir différement selon le bouton cliqué et nous éviter de créer plusieur fonction onClick
     * @param v représente le bouton cliqué
     */
    public void onClick(View v)
    {
        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();

        switch(v.getId())
        {
            // Nous allons chercher les lieux issus de la barre de recherche
            case R.id.buttonSearch:
                mMap.clear();

                EditText tp =  findViewById (R.id.TextLieu);

                String chaine = tp.getText().toString();
                chaine =  chaine.replaceAll("\\s", "");

                String url = getUrl(latitude, longitude, chaine);

                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "montre les " + chaine +" au alentours", Toast.LENGTH_SHORT).show();
                break;

            // Si le bouton Filtrer est activé , nous devions afficher tout les restaurant de la map à une certaine position, cependant
            // beaucoup d'hotel sont considéré comme des restaurant ce qui ne nous donnait pas les résultat voulu, nous avons donc choisi
            // d'afficher la liste des mcDonald et attendant de trouver une solution.
            case R.id.Filtrer:

                mMap.clear();
                String restaurant = "McDonald";
                 url = getUrl(latitude, longitude, restaurant);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "montre les McDo au alentours", Toast.LENGTH_SHORT).show();
                break;

                // va nous donner le chemin à parcourir pour arriver vers le marker selectionné.
            case R.id.buttonGo:

                Object dataTransfers[] = new Object[3];


                GetDirectionsData getDirectionsData = new GetDirectionsData();
                 url = getDirectionsUrl(positionMarker);


                dataTransfers[0] = mMap;
                dataTransfers[1] = url;
                dataTransfers[2] = positionMarker;

                getDirectionsData.execute(dataTransfers);
                Toast.makeText(MapsActivity.this, "calcul de distance & dessin trajet", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * va nous construire l'url qui nous permettra de récuperer les information lié
     * au chemin à parcourir pour atteindre un certains marker
     * depuis notre position (tel que la durée du chemin,la distance ou le detail du chemin à parcourir)
     * @param positionMarker
     * @return
     */
    private String getDirectionsUrl(LatLng positionMarker)
    {
        double lat = positionMarker.latitude;
        double lng = positionMarker.longitude;
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+lat+","+lng);
        googleDirectionsUrl.append("&key=" + "AIzaSyAI2_26H8WYP5kLTIs-4wTvLB7FAhXe8pE");

        return googleDirectionsUrl.toString();
    }

    /**
     * Va nous permettre de créer :l'url afin de faire notre requête
     * @param latitude represente la coordonné en latitude du lieu
     * @param longitude représente la coordonné en longitude du lieu
     * @param nearbyPlace nous donnes le nom du lieu
     * @return
     */
        private String getUrl(double latitude , double longitude , String nearbyPlace)
            {
                int seekValue = seek.getProgress();
                StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                googlePlaceUrl.append("location="+latitude+","+longitude);
                googlePlaceUrl.append("&radius="+seekValue*1000);
                googlePlaceUrl.append("&name="+nearbyPlace);
                googlePlaceUrl.append("&sensor=true");
                googlePlaceUrl.append("&key="+"AIzaSyAI2_26H8WYP5kLTIs-4wTvLB7FAhXe8pE");

                Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());
                Toast.makeText(MapsActivity.this, "batiment à " +  seekValue + "km de votre position", Toast.LENGTH_SHORT).show();

                return googlePlaceUrl.toString();
            }

    /**
     * Lorsque l'appli est allumé va actualisé la position de l'utilisateur tout les 50 millisecond
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(50);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    /**
     * cette fonction nous permets de connaitre le maker qu'a selectionné l'utilisateur.
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        positionMarker = marker.getPosition();
        Toast.makeText(MapsActivity.this, "vous avez selectionné un marker", Toast.LENGTH_SHORT).show();

        return false;
    }



}
