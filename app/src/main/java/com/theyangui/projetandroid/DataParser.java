package com.theyangui.projetandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nicolas on 18/01/2018.
 * va analyser un texte JSON et en ressortir une liste contenant
 * les information que l'on a besoin pour notre application
 */

public class DataParser {

    /**
     * va analyser un fichier Json est va nous retourner les informations que l'on veut.
     * @param googleDirectionsJson texte en Json que l'on veut analyser
     * @return va nous retourner une HashMap<String,String> contenant la durée et la distance du trajet.
     */
    private HashMap<String,String> getDuration(JSONArray googleDirectionsJson)
    {
        HashMap<String,String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance ="";


        try {

            duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");

            googleDirectionsMap.put("duration" , duration);
            googleDirectionsMap.put("distance", distance);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return googleDirectionsMap;
    }

    /**
     * chaque lieu est caractérisé par des couples de string (ex : latitude + sa latitude )
     * cette fonction nous permets d'en récuperer les couples qui la compose
     *  @param googlePlaceJson
     * @return retourne des couple de String avec les information voulu et de les catégoriser.
     */
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        Log.d("getPlace", "Entered");


        try {
            if(!googlePlaceJson.isNull("name"))
            {

                placeName = googlePlaceJson.getString("name");

            }
            if( !googlePlaceJson.isNull("vicinity"))
            {
                vicinity = googlePlaceJson.getString("vicinity");

            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            googlePlacesMap.put("place_name" , placeName);
            googlePlacesMap.put("vicinity" , vicinity);
            googlePlacesMap.put("lat" , latitude);
            googlePlacesMap.put("lng" , longitude);
            googlePlacesMap.put("reference" , reference);


            Log.d("getPlace", "Putting Places");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlacesMap;
    }


    /**
     * fonction qui va nous permettre de recupere la liste des lieux issu des données Json de google
     * @param jsonArray
     * @returnva  va nous retourner une liste de lieux avec les hashmap
     */
    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String,String> placeMap = null;
        Log.d("Places", "getPlaces");

        for(int i = 0;i<count;i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;

    }

    /**
     * cette fonction va récuperer le texte en String et
     * le transformer en un objet JsonArray qui va être analysé par la suite
     * cette fonction est spécialisé pour les lieux issus de GetNearbyPlacesData
     */
    public List<HashMap<String,String>> parsePlace(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            Log.d("Places", "parse");

            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }

    /**
     * cette fonction va récuperer le texte en String et
     * le transformer en un objet JsonArray qui va être analysé par la suite
     * cette fonction est spécialisé pour le chemin à parcourir issus de GetDirectionsData
     * @param jsonData
     * @return
     */
    public String[] parseDirections(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPaths(jsonArray) ;
    }

    /**
     * cette fonction va récuperer le texte en String et
     * le transformer en un objet JsonArray qui va être analysé par la suite
     * cette fonction est spécialisé pour les information de durée et de distance issus de GetNearbyPlacesData
     * @param jsonData
     * @return
     */
    public HashMap<String,String> parserDureeDistance(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getDuration(jsonArray);
    }

    /**
     * un chemin est en réalité l'assemblement de plusieurs chemins, cette fonction va nous permettre
     * à l'aide d'un Json, la position de chaque chemin et de nous retourner une liste des différents segment
     * qui compose une route.
     * @param googleStepsJson
     * @return
     */
    public String[] getPaths(JSONArray googleStepsJson )
    {
        int count = googleStepsJson.length();
        String[] polylines = new String[count];

        for(int i = 0;i<count;i++)
        {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return polylines;
    }

    /**
     * la fonction va recuperer du fichier Json la position d'un segment pour tracer la route,
     * et va nous retourner une String contenant la position des points
     * @param googlePathJson
     * @return
     */
    public String getPath(JSONObject googlePathJson)
    {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }




}
