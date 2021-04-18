package com.example.googlemaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener {

    private GoogleMap mapa;
    private LatLng miCasa;
    Button btnRuta;
    EditText txtLatitud, txtLongitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnRuta = findViewById(R.id.btnRuta);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);


        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.map, mapFragment)
                .commit();
        mapFragment.getMapAsync(MainActivity.this);
        btnRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapa.clear();
                miCasa = new LatLng(Double.parseDouble(txtLatitud.getText().toString()), Double.parseDouble(txtLongitud.getText().toString()));

                latitudOrigen = Double.parseDouble(String.valueOf(mapa.getMyLocation().getLatitude()));
                longitudOrigen = mapa.getMyLocation().getLongitude();
                if (actualPosicion) {
                    LatLng yo = new LatLng(miCasa.latitude, miCasa.longitude);
                    mapa.addMarker(new MarkerOptions()
                            .position(yo)
                            .title("Mi casa"));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(miCasa)
                            .zoom(10)
                            .build();
                    mapa.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latitudOrigen + "%2C" + longitudOrigen + "&destination=" + miCasa.latitude + "%2C" + miCasa.longitude + "&key=AIzaSyB3JqzJKWgk6GSrYVCv0t6i6LNN2nXUaYI";

                    RequestQueue queue = Volley.newRequestQueue(getBaseContext());
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                jsonObject = new JSONObject(response);
                                Log.i("JSONSI", response);
                                trazarRuta(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("JSON", error.toString());
                        }
                    });

                    queue.add(stringRequest);
                }
            }
        });


    }

    Boolean actualPosicion = true;
    JSONObject jsonObject;
    Double longitudOrigen, latitudOrigen;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mapa.setMyLocationEnabled(true);
    }

    public void trazarRuta(JSONObject jsonObject) {
        JSONArray jsonRoutes;
        JSONArray jsonLegs;
        JSONArray jSteps;
        try {
            jsonRoutes=jsonObject.getJSONArray("routes");
            for (int i=0; i<jsonRoutes.length();i++){
                jsonLegs=((JSONObject)(jsonRoutes.get(i))).getJSONArray("legs");
                for (int j=0; j<jsonLegs.length();j++){
                    jSteps=((JSONObject)jsonLegs.get(j)).getJSONArray("steps");
                    for(int k=0; k<jSteps.length();k++){
                        String polyline=""+((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        Log.i("poly",""+polyline);
                        List<LatLng> lista= PolyUtil.decode(polyline);
                        mapa.addPolyline(new PolylineOptions().addAll(lista).color(Color.rgb(0,170,228)).width(5));
                    }
                }
            }
        }catch (Exception e){}
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        Toast.makeText(getBaseContext(),"Clickaste",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Toast.makeText(getBaseContext(),"Clickaste",Toast.LENGTH_SHORT).show();
    }
}