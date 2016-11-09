package com.vzug.schatzkarte;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements android.location.LocationListener {


    private LocationManager locationManager;
    private String provider;
    private MyItemizedOverlay overlay;
    private final static String SHARED_PREF_NAME = "LOCATIONS";
    private final static int FACTOR              = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
        checkGPSActivated();
        centerCurrentLocation();

        SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME, 0);
        HashMap<String,Integer> map = (HashMap<String, Integer>) settings.getAll();
        if (!map.isEmpty()) {
            for (int i = 1; i <= (map.size() / 2); i++) {
                GeoPoint geopoint = new GeoPoint((double)map.get("lat"+i) / FACTOR,(double)map.get("lon"+i) / FACTOR);
                overlay.addItem("Point","New",geopoint);
            }
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuinf = getMenuInflater();
        menuinf.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
        }
        locationManager.requestLocationUpdates(provider, 500, 1, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestcode, String[] perms, int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this, perms[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, perms[1]) == PackageManager.PERMISSION_GRANTED) {
            createMap();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (item.getItemId()) {
            case R.id.send:
                try {
                    JSONObject eintrag = log();
                    Intent intent = new Intent("ch.appquest.intent.LOG");

                    if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
                        Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
                    }
                    else {
                        intent.putExtra("ch.appquest.logmessage", eintrag.toString());
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.pinCurrentLocation:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                }
                Location location = locationManager.getLastKnownLocation(provider);
                overlay.addItem("Location","Snippet",new GeoPoint(location.getLatitude(),location.getLongitude()));

                editor.putInt("lon"+overlay.size(),(int)(location.getLongitude() * FACTOR));
                editor.putInt("lat"+overlay.size(),(int)(location.getLatitude() * FACTOR));
                editor.commit();
                return true;
            case R.id.clear:
                editor.clear();
                editor.commit();
                overlay.clearItems();
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(context);
    }

    private void checkPermissions() {
        String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(this, perms[0]) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this, perms[1]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, perms, 1);
        } else {
            createMap();
        }
    }

    private void createMap() {
        MapView mapview = (MapView) findViewById(R.id.hsrMap);

        SimpleRegisterReceiver simpleRegisterReceiver = new SimpleRegisterReceiver(this);
        XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", 1, 20, 256, ".png", new String[]{});
        mapview.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapview.setMultiTouchControls(true);
        mapview.setBuiltInZoomControls(true);
        mapview.getController().setZoom(18);

        File file = new File(Environment.getExternalStorageDirectory(), "osmdroid/hsr.mbtiles");
        //Crasht hier
        IArchiveFile[] files = {MBTilesFileArchive.getDatabaseFileArchive(file)};
        MapTileModuleProviderBase treasureMapProvbase = new MapTileFileArchiveProvider(simpleRegisterReceiver, treasureMapTileSource, files);
        MapTileProviderBase mtproviderbase = new MapTileProviderArray(treasureMapTileSource, null, new MapTileModuleProviderBase[]{treasureMapProvbase});
        TilesOverlay treasuremapTilesOverlay = new TilesOverlay(mtproviderbase, getBaseContext());

        addOverlay();

        treasuremapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapview.getOverlays().add(treasuremapTilesOverlay);
    }

    private void addOverlay() {
        Drawable drawable = getDrawable(R.drawable.location);
        drawable.setBounds(0, (int)(drawable.getIntrinsicHeight() * 0.2), (int)(drawable.getIntrinsicWidth() * 0.2), 0);

        overlay = new MyItemizedOverlay(drawable);
        MapView mapview = (MapView)findViewById(R.id.hsrMap);
        mapview.getOverlays().add(overlay);
    }

    private void centerCurrentLocation() {
        MapView mapview = (MapView) findViewById(R.id.hsrMap);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, perms, 1);
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        else {
            Toast.makeText(this, "Null Location", Toast.LENGTH_LONG);
        }
    }

    private void checkGPSActivated() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private JSONObject log() throws JSONException {
        SharedPreferences settings = getSharedPreferences(SHARED_PREF_NAME, 0);
        HashMap<String,Integer> logs = (HashMap<String,Integer>)settings.getAll();
        Iterator it = logs.entrySet().iterator();
        try {
            JSONObject logEintrag = new JSONObject();
            logEintrag.put("task", "Schatzkarte");
            JSONArray pointsList = new JSONArray();
            for(int i = 1; i <= overlay.size(); i++) {
                JSONObject coord = new JSONObject();
                coord.put("lat", settings.getInt("lat", settings.getInt("lat"+i,0)));
                coord.put("lon", settings.getInt("lon", settings.getInt("lon"+i,0)));
                pointsList.put(coord);
            }
            logEintrag.put("points",pointsList);
            return logEintrag;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        MapView mapview = (MapView)findViewById(R.id.hsrMap);
        GeoPoint point = new GeoPoint(location.getLatitude(),location.getLongitude());
        mapview.getController().setCenter(point);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Do nothing
        return;
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("onProviderEnabled","New Provider enabled <" + provider + ">");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("onProviderDisabled","Provider disabled <" + provider + ">");
    }
}
