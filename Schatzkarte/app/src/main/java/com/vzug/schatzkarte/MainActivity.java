package com.vzug.schatzkarte;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] perms = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(this,perms[0]) == PackageManager.PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this,perms[1]) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,perms,1);
        }
        else {
            createMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestcode, String[] perms, int[] grantResults) {
        if (ContextCompat.checkSelfPermission(this,perms[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,perms[1]) == PackageManager.PERMISSION_GRANTED){
            createMap();
        }
    }

    private void createMap() {
        MapView mapview = (MapView) findViewById(R.id.hsrMap);

        SimpleRegisterReceiver simpleRegisterReceiver = new SimpleRegisterReceiver(this);
        XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", 1, 20, 256, ".png", new String[]{ });
        mapview.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapview.setMultiTouchControls(true);
        mapview.setBuiltInZoomControls(true);
        mapview.getController().setZoom(18);

        File file = new File(Environment.getExternalStorageDirectory(), "osmdroid/hsr.mbtiles");
        //Crasht hier
        IArchiveFile[] files = { MBTilesFileArchive.getDatabaseFileArchive(file)};
        MapTileModuleProviderBase treasureMapProvbase = new MapTileFileArchiveProvider(simpleRegisterReceiver,treasureMapTileSource, files);
        MapTileProviderBase mtproviderbase = new MapTileProviderArray(treasureMapTileSource,null,new MapTileModuleProviderBase[] {treasureMapProvbase});
        TilesOverlay treasuremapTilesOverlay = new TilesOverlay(mtproviderbase, getBaseContext());
        treasuremapTilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        mapview.getOverlays().add(treasuremapTilesOverlay);
    }
}
