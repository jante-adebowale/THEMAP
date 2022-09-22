package com.softcorridor.themap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, MapsContract.View, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private Double lat, lng;
    String addressName;

    LocationManager locationManager;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;


    private List<LatLng> latLngs = new ArrayList<>();
    private boolean started = false;
    List<LatLng> polygonList = new ArrayList<LatLng>();
    private Location mLastLocation;
    private Polygon polygon;
    private Polyline polyline;
    private boolean measurementStarted;
    private JSONArray coordinates = new JSONArray();
    private Button reset;
    private FloatingActionButton addBtn;
    private boolean mapIsReady;
    private LinearLayout coordinateLayout;
    LayoutInflater inflater = null;
    private Button calculateArea,closeBtn;
    private String estimatedArea;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        reset = (Button) findViewById(R.id.reset);
        calculateArea = (Button) findViewById(R.id.calculateArea);
        closeBtn = (Button) findViewById(R.id.close);
        context = this;

        addBtn = (FloatingActionButton)findViewById(R.id.addBtn);
        coordinateLayout = (LinearLayout) findViewById(R.id.coordinator_layout);
        this.inflater = LayoutInflater.from(context);

        Intent intent = getIntent();

        lat = 9.6544;
        //locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lat = intent.getDoubleExtra(JsonConstants.LONGITUDE, 0);
        lng = intent.getDoubleExtra(JsonConstants.LATITUDE, 0);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        lat = 9.6544;
        lng = 7.000;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mapIsReady){
                    try{
                        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        if (mLastLocation != null) {
                            double latitude = mLastLocation.getLatitude();
                            double longitude = mLastLocation.getLongitude();
                            ToastUtil.showToast(context, "Latitude: " + latitude + ", Longitude: " + longitude);

                            LinearLayout board = (LinearLayout)inflater.inflate(R.layout.coordinate, null);
                            TextView latitudeLbl = (TextView)board.findViewById(R.id.latitudeLbl);
                            TextView longitudeLbl = (TextView)board.findViewById(R.id.longitudeLbl);

                            latitudeLbl.setText("LAT: " + latitude);
                            longitudeLbl.setText("LONG: " + longitude);

                            coordinateLayout.addView(board);

                            //Add to exist list
                            LatLng address = new LatLng(latitude, longitude);
                            latLngs.add(address);
                            JSONObject coordinate = new JSONObject();
                            coordinate.put(JsonConstants.LATITUDE, latitude);
                            coordinate.put(JsonConstants.LONGITUDE, longitude);
                            coordinates.put(coordinate);


                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(latLngs).geodesic(true);
                            polylineOptions.width(2).color(Color.BLUE);
                            polyline = mMap.addPolyline(polylineOptions);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    ToastUtil.showToast(context,"Map is not ready yet!");
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(coordinateLayout.getChildCount() > 0){
                        coordinateLayout.removeAllViews();


                        coordinates = new JSONArray();
                        if (!mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        }
                        latLngs.clear();
                        started = true;

                        if (polygon != null) {
                            polygon.remove();
                        }

                        if (polyline != null) {
                            polyline.remove();
                            polyline = null;
                        }

                    }else{
                        ToastUtil.showToast(context,"Empty!!!");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //if(latLngs.size() < 2) {
                        finish();
//                    }else if(latLngs.size() > 2){
//
//                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        calculateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latLngs.isEmpty() || latLngs.size() <= 1) {
                    ToastUtil.showToast(context, "No distance recorded");
                } else {
                        if(latLngs.size() > 2) {

                            if (mGoogleApiClient.isConnected()) {
                                mGoogleApiClient.disconnect();
                                mMap.stopAnimation();
                            }

                            started = false;

                            if (polygon != null) {
                                polygon.remove();
                                polygon = null;
                            }

                            if (polyline != null) {
                                polyline.remove();
                                polyline = null;
                            }


                            polygon = mMap.addPolygon(new PolygonOptions()
                                    .clickable(true)
                                    .addAll(latLngs));
                            polygon.setTag("beta");
                            stylePolygon(polygon);

                            String calculatedArea = String.format("%.3f", SphericalUtil.computeArea(latLngs));
                            estimatedArea = calculatedArea;
                            doneDialog(calculatedArea);

                            ToastUtil.showToast(context, "Area: " + calculatedArea);
                        }else {
                            ToastUtil.showToast(context,"Not enough point(s) for a proper shape!");
                        }
                }
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double latitude = 0.00;
        double longitude = 0.00;
        if (mLastLocation != null) {
             latitude = mLastLocation.getLatitude();
             longitude = mLastLocation.getLongitude();
        }
        LatLng address = new LatLng(latitude, longitude);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
//        mMap.addMarker(new MarkerOptions().
//                        position(address).
//                        title(addressName)).
//                setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        ToastUtil.showToast(context,"Map is ready!!! With LAT : " + latitude + "  &  LONG : " + +longitude  );

        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(address, 15));


    }




    @Override
    protected void onStart() {
        super.onStart();
        if (started) {
            return;
        }

        initGeoLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (started) {
            return;
        }
        discontinueGeoLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //LocationServices.FusedLocationApi
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("here", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(final Location location) {
        try {
            //  mLastUpdateTime =
            LatLng address = new LatLng(location.getLatitude(), location.getLongitude());
            if (location != null) {
                if (!started) {
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(address, 15));
                }

                //Log.d("here", "Printed");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((latLngs.size() == 2)) {
                            ToastUtil.showToast(context, "Start moving....");
                        }

                    }
                });

                if (!started) {
                    return;
                }

//                latLngs.add(address);
//                JSONObject coordinate = new JSONObject();
//                coordinate.put(JsonConstants.LATITUDE, location.getLatitude());
//                coordinate.put(JsonConstants.LONGITUDE, location.getLongitude());
//                coordinates.put(coordinate);
//
//
//                PolylineOptions polylineOptions = new PolylineOptions();
//                polylineOptions.addAll(latLngs).geodesic(true);
//                polylineOptions.width(2).color(Color.BLUE);
//                polyline = mMap.addPolyline(polylineOptions);

            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void loadMap() {

    }

    @Override
    public void showLocationPermissionNeeded() {

    }

    @Override
    public void addMarkerToMap(MarkerOptions options, LatLng latLng) {
        //Log.d("here", "options");
    }

    @Override
    public void setPresenter(MapsContract.Presenter presenter) {

    }

    @Override
    public Activity getViewActivity() {
        return null;
    }

    private void displayLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            LatLng address = new LatLng(latitude, longitude);

            ToastUtil.showToast(context, "Latitude: " + latitude + ", Longitude: " + longitude);

            if(latitude > 0.00){
                mapIsReady = true;
            }else{
                mapIsReady = false;
            }

           // LatLng address = new LatLng(latitude, longitude);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
            mMap.addMarker(new MarkerOptions().
                            position(address).
                            title(addressName)).
                    setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            ToastUtil.showToast(context, "@@Map is ready!!! With LAT : " + latitude + "  &  LONG : " + +longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(address, 20));


        } else {

        }
    }

    private void MoveMapAppropriately(){
        try{
            if(mMap != null) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double latitude = 0.00;
                double longitude = 0.00;
                if (mLastLocation != null) {
                    latitude = mLastLocation.getLatitude();
                    longitude = mLastLocation.getLongitude();
                }
                LatLng address = new LatLng(latitude, longitude);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);
                mMap.addMarker(new MarkerOptions().
                                position(address).
                                title(addressName)).
                        setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                ToastUtil.showToast(context, "@@Map is ready!!! With LAT : " + latitude + "  &  LONG : " + +longitude);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
                mMap.setMyLocationEnabled(true);
                mMap.setBuildingsEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(address, 15));
            }else{
                ToastUtil.showToast(context,"Not initiated!!!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    private static final int POLYGON_STROKE_WIDTH_PX = 2;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    //private static final List<PatternItem> PATTERN_POLYGON_BETA = Arrays.asList(DOT, GAP, DASH, GAP);

    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_GREEN_ARGB;
                fillColor = COLOR_PURPLE_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                // pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_ORANGE_ARGB;
                fillColor = COLOR_BLUE_ARGB;
                break;
        }

        //polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(Color.BLUE);
        polygon.setFillColor(fillColor);
    }

    public void doneDialog(final String value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = String.format("The estimated area of the measured land space is: %s", value);
        builder.setMessage(message)
                .setTitle("Alert").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("result", estimatedArea);
                        intent.putExtra(JsonConstants.COORDINATES, coordinates.toString());
                        setResult(200, intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void actionButton(View view) {
        if (view.getId() == R.id.calculateArea) {

            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
                mMap.stopAnimation();
            }

            started = false;

            if (polygon != null) {
                polygon.remove();
                polygon = null;
            }

            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }


            if (latLngs.isEmpty()||latLngs.size()<=1) {
                ToastUtil.showToast(context, "No distance recorded");
                reset.setText("Start measuring");
                reset.setClickable(true);
                return;
            }

            polygon = mMap.addPolygon(new PolygonOptions()
                    .clickable(true)
                    .addAll(latLngs));
            polygon.setTag("beta");
            stylePolygon(polygon);

            String calculatedArea = String.format("%.3f", SphericalUtil.computeArea(latLngs));
            doneDialog(calculatedArea);

            ToastUtil.showToast(context, "Area: " + calculatedArea);



            reset.setText("Start measuring");
            reset.setClickable(true);
        } else if (view.getId() == R.id.reset) {
            coordinates = new JSONArray();
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
            latLngs.clear();
            started = true;

            if (polygon != null) {
                polygon.remove();
            }

            if (polyline != null) {
                polyline.remove();
                polyline = null;
            }

            reset.setText("Measuring....");
            reset.setClickable(false);
        }

        else if(view.getId()==R.id.close){
            finish();
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)&&started) {
            ToastUtil.showToast(context, "Active process ongoing. Please end first");
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void initGeoLocation(){
        if(started){
            return;
        }
        mGoogleApiClient.connect();
    }

    public void discontinueGeoLocation(){
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}