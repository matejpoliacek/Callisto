package com.chocolateam.galileopvt;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;


/**
 * Created by Cedric on 27/03/2018.
 */

public class CellID extends Fragment{
    private double[] mReferenceLocation = null;
    private int cellID, cellLAC, cellCID, cellMCC, cellMNC;
    private static final String URL_ADDRESS = "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBoM9XAux_KtdzRNB1-prxOsA7yuRcA_io";
    private Context context;
    //public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            run();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setmReferenceLocation(double lat, double lng, double alt){
        if (mReferenceLocation == null) {
            mReferenceLocation = new double[3];
        }
        mReferenceLocation = new double[3];
        mReferenceLocation[0] = lat;
        mReferenceLocation[1] = lng;
        mReferenceLocation[2] = alt;
    }

    private void run() throws IOException, JSONException {
        // Update cellCID, cellMCC, cellMNC, cellID, cellLAC from Telephony API

        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        cellID = cellLocation.getCid();
        cellLAC = cellLocation.getLac();
        Log.e("All cell info", String.valueOf(telephonyManager.getAllCellInfo()));
        List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        CellInfoLte cellInfoLte = (CellInfoLte) cellInfoList.get(0);

        cellCID = cellInfoLte.getCellIdentity().getCi();
        cellMCC = cellInfoLte.getCellIdentity().getMcc();
        cellMNC = cellInfoLte.getCellIdentity().getMnc();

        Log.e("THERE BE ID", String.valueOf(cellID));
        Log.e("THERE BE LAC", String.valueOf(cellLAC));
        Log.e("THERE BE CI", String.valueOf(cellCID));
        Log.e("THERE BE MCC", String.valueOf(cellMCC));
        Log.e("THERE BE MNC", String.valueOf(cellMNC));

        locateCell();
    }

    private void locateCell() throws JSONException, IOException {

        //DO NOT USE, the Google API to get cell location doesn't walk!
        // First we need to create the json to be sent
        JSONArray cellTowers = new JSONArray();
        JSONObject cellTower_1 = new JSONObject();
        final JSONObject jsonQuery = new JSONObject();
        OutputStream out = null;
        cellTower_1.put("cellId", cellCID);
        cellTower_1.put("locationAreaCode", cellLAC);
        cellTower_1.put("mobileCountryCode", cellMCC);
        cellTower_1.put("mobileNetworkCde", cellMNC);

        cellTowers.put(cellTower_1);
        jsonQuery.put("cellTowers", cellTowers);

        // Maybe decompose the sending in multiple functions
        sendPost(jsonQuery);
    }

    public void sendPost(final JSONObject jsonObject){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL googleURL = new URL(URL_ADDRESS);
                    HttpURLConnection urlConnection = (HttpURLConnection) googleURL.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    urlConnection.setRequestProperty("Accept","application/json");

                    DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
//                    outputStream.writeBytes(URLEncoder.encode(jsonObject.toString(), "UTF-8"));
                    outputStream.writeBytes(jsonObject.toString());

                    outputStream.flush();

                    StringBuilder sb = new StringBuilder();
                    int httpResult = urlConnection.getResponseCode();
                    if (httpResult == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(
                                new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                    }

                    /*
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    */

                    Log.i("JSON", jsonObject.toString());
                    /*DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
                    outputStream.writeBytes(jsonObject.toString());

                    outputStream.flush();
                    outputStream.close();
                    */


                    Log.i("STATUS CODE", String.valueOf(urlConnection.getResponseCode()));
                    Log.i("MSG JSON QUERY", String.valueOf(urlConnection.getErrorStream()));
                    Log.i("HEY", "HEY");

                    urlConnection.disconnect();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

