package com.vzug.schatzkarte;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createMap();
    }

    private void createMap() {
        MapView mapview = (MapView) findViewById(R.id.hsrMap);

        SimpleRegisterReceiver simpleRegisterReceiver = new SimpleRegisterReceiver(this);
        XYTileSource treasureMapTileSource = new XYTileSource("mbtiles", 1, 20, 256, ".png", new String[]{ });
        mapview.setTileSource(treasureMapTileSource);
        mapview.setMultiTouchControls(true);
        mapview.setBuiltInZoomControls(true);
        mapview.getController().setZoom(18);

        File file = new File(Environment.getExternalStorageDirectory(), "osmdroid/hsr.mbtiles");
        //Crasht hier
        //IArchiveFile[] files = {MBTilesFileArchive.getDatabaseFileArchive(file)};
    }

}
