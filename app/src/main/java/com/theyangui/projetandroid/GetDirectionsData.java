package com.theyangui.projetandroid;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Skred on 24/01/2018.
 * Cette classe permet la récupération du chemin à parcourir pour atteindre un marker.
 * A l'aide de la BDD de google Direction, nous allons récuperer le chemin ( par défaut c'est le chemin en véhicule )
 * puis il va dessiner sur la map la route à parcourir, elle va également changer les information du marker pour
 * retourner la distance et la durée du trajet depuis notre position
 *
 */

public class GetDirectionsData extends AsyncTask<Object, String,String> {

    GoogleMap mMap;
    String url;
    String googleDirectionsData;
    String duration, distance;
    LatLng latLng;

    /**
     * avant d'exécuter notre fonction, la classe va en arrière plan récuperer le Json issus de notre requete URL
     * @param objects représente les information de notre classe principale que l'on fournit à getDirectionsData, nous donnons ainsi notre map
     *                qui permettra à la map de le modifier,
     *                notre requete URL que l'on souhaite analysé
     *                et la position du marker recherché.
     * @return
     */
    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng = (LatLng)objects[2];

        DownloadUrl downloadURL = new DownloadUrl();
        try {
            googleDirectionsData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionsData;
    }

    /**
     * Lorsque la classe sera executé, le json sera analysé puis selon les information, cela va nous modifier les information sur le marker
     * ( en nous donnant la durée et la distance à notre position, puis appelera la fonction displaydirection() afin de nous afficher le chemin.
     * @param s représente le json fournit par GoogleDirection issus de notre requete.
     */
    protected void onPostExecute(String s){



        mMap.clear();

        String[] directionsList = null;
        HashMap<String,String> detailList = null;
        DataParser parser = new DataParser();
        directionsList = parser.parseDirections(s);
        detailList = parser.parserDureeDistance(s);
        duration = detailList.get("duration");
        distance = detailList.get("distance");

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Duration = "+duration);
        markerOptions.snippet("Distance = "+distance);


        mMap.addMarker(markerOptions);


        displayDirection(directionsList);

    }

    /**
     * va récuperer les information sur le chemin à parcourir ( position d'une portion de la route ) puis va nous déssiner le chemin sur notre Map
     * @param directionsList nous donnes les différents information vis à vis de la route à parcourir.
     */
    public void displayDirection(String[] directionsList)
    {

        int count = directionsList.length;
        for(int i = 0;i<count;i++)
        {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.RED);
            options.width(6);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }

}
