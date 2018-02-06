package com.theyangui.projetandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nicolas on 18/01/2018.
 *
 * Cette classe fera la transition entre notre mMap et les information en JSON de google
 * nous allons dans un premier temps récuperer les informations se trouvant dans l'url rentré à l'aide de l'objet
 * DownloadUrl.
 * Nous allons ensuite l'analyser à l'aide de dataParser afin de ressortir les informations que l'on veut.
 *
 * Lorsque la liste des informations sera prêtes, nous allons modifier notre map en fonction.
 *
 *
 */


class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    String url;

    /**
     * nous allons utiliser un Thread afin de lire les informations de l'url
     * les mettres dans une String qui va ensuite
     * etre analysé .
     * @param objects
     * @return
     */
    @Override
    protected String doInBackground(Object... objects){
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadUrl downloadURL = new DownloadUrl();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    /**
     * lorsque la fonction en background est terminé, nous allons analyser le message
     * et en ressortir une liste qui sera utilisé pour afficher les lieux
     * de manière efficace
     * @param s
     */
    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parsePlace(s);
        Log.d("nearbyplacesdata","called parse method");
        showNearbyPlaces(nearbyPlaceList);
    }

    /**
     *  Va afficher sur la map la liste des lieux que nous recherchons
     *  en posant des marker sur les lieux concerné et en affichant des informations les concernants
     * @param nearbyPlaceList
     */
    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

            LatLng latLng = new LatLng( lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : "+ vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
    }

}
