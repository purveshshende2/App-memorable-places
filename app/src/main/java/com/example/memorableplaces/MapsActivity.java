package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    public void centerMapOnLocation(Location location,String title){
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude()); // for getting users location
            mMap.clear(); // for clear previous location
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); ////Asking for Permission ////--------------------------
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();

        if (intent.getIntExtra("placeNumber",0) == 0){
            //Zoom in on user location

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location,"Your location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location");
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        } else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);

            centerMapOnLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "";

        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);


            if (listAddresses != null && listAddresses.size() > 0){

                if (listAddresses.get(0).getThoroughfare() != null){
                    if (listAddresses.get(0).getSubThoroughfare() != null){

                        address += listAddresses.get(0).getSubThoroughfare() + " ";

                    }
                    address += listAddresses.get(0).getThoroughfare();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address += sdf.format(new Date());


        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.places.add(address); // for add places in Mainactivity
        MainActivity.locations.add(latLng);

        MainActivity.arrayAdapter.notifyDataSetChanged();// for notify in MainActivity

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);

        try {
            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();

            for (LatLng coord : MainActivity.locations){
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));

            }

            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();

            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();

            sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes)).apply();






        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();



    }
}