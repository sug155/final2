package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class LocationActivity :AppCompatActivity() {

    private lateinit var mapview:MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        mapview=findViewById(R.id.mapview)
        mapview.setTileSource(TileSourceFactory.MAPNIK)
        mapview.setMultiTouchControls(true)
        mapview.controller.setZoom(15.0)
        mapview.controller.setCenter(org.osmdroid.util.GeoPoint(9.8821,78.0816))
    }
}

