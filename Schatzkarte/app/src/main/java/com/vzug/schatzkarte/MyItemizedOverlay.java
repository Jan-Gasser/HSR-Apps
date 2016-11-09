package com.vzug.schatzkarte;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

/**
 * Created by jan-gasser on 29.10.16.
 */
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private ArrayList<OverlayItem> overlayItems;

    public MyItemizedOverlay(Drawable pDefaultMarker) {
        super(pDefaultMarker);
        overlayItems = new ArrayList<OverlayItem>();
    }

    public void addItem(String title, String snippet, GeoPoint p) {
        OverlayItem overlayItem = new OverlayItem(title, snippet, p);
        overlayItems.add(overlayItem);
        populate();
    }

    public void clearItems() {
        overlayItems.clear();
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return overlayItems.get(i);
    }

    @Override
    public int size() {
        return overlayItems.size();
    }

    @Override
    public boolean onSnapToItem(int i, int i1, Point point, IMapView iMapView) {
        return false;
    }
}
