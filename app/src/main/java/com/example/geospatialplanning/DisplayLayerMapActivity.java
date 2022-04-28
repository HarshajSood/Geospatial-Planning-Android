package com.example.geospatialplanning;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

public class DisplayLayerMapActivity extends AppCompatActivity{
    private Spinner dropdown;
    private ArrayAdapter<String> adapter1;
    private ImageView imview, legendview;
    private TextView details;
    private String lat_imported, lng_imported;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayer);
        dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"LULC", "Slope", "Drainage", "Geomorphology", "Hydrology", "Land Degradation"};
        adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter1);
        imview=(ImageView) findViewById(R.id.imageView);
        details= (TextView) findViewById(R.id.textView3);

        Intent intent=getIntent();
        lat_imported=this.getIntent().getExtras().getString("Final_lat");
        lng_imported=this.getIntent().getExtras().getString("Final_lng");
    }
    public void getMap(View view){
        String text = dropdown.getSelectedItem().toString();
        String lyrs = "lulc:KA_LULC50K_1516";
        String url1 = "https://bhuvan-vec2.nrsc.gov.in/bhuvan";
        String url2 = "https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service";
        String url3 = "https://bhuvan-app3.nrsc.gov.in/gswbis/wbis";
        Double lat = Double.parseDouble(lat_imported);
        Double lng = Double.parseDouble(lng_imported);
        String urlused = url1;

        //LULC
        if(text == "LULC")
        {
            lyrs = "lulc:KA_LULC50K_1516";
            urlused = url1;
        }
        //Slope
        else if (text == "Slope")
        {
            lyrs = "v3:slope_ka";
            urlused = url2;
        }
        //Drainage
        else if (text == "Drainage")
        {
            lyrs = "v3:drainage_ka";
            urlused = url2;
        }
        //Geomorphology
        else if (text == "Geomorphology")
        {
            lyrs = "geomorphology:KA_GM50K_0506";
            urlused = url1;
        }
        //Hydrology
        else if (text == "Hydrology")
        {
            //lyrs = "v3:basin_ka";
            lyrs = "wbis:canal";
            urlused = url3;
        }
        //Land Degradation
        else if (text == "Land Degradation")
        {
            lyrs = "ld:KA_LD50K_1516";
            urlused = url1;
        }
        //System.out.println(text);
        String urlLink=urlused+"/wms?request=getMap&bbox="+String.valueOf(lng-0.001)+
                ","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+
                String.valueOf(lat+0.001)+"&CRS=EPSG:4326&width=1000&height=1000&layers="+lyrs +
                "&format=image/png&TRANSPARENT=TRUE";
        //System.out.println("\n"+urlLink+"\n");
        LoadImage loadImage = new LoadImage(imview);
        loadImage.execute(urlLink);
    }
    public void getLegend(View view){
        String text = dropdown.getSelectedItem().toString();
        View view2;
        view2=getLayoutInflater().inflate(R.layout.lulcimage_layout, null);
        legendview= (ImageView) view2.findViewById(R.id.popuplegend);
        if(text == "LULC") {
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.lulcimage_layout
                    , null));
            settingsDialog.show();
        }else if(text == "Slope"){
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.slopeimage_layout
                    , null));
            settingsDialog.show();
        }else if(text == "Drainage"){
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.drainimage_layout
                    , null));
            settingsDialog.show();
        }else if(text == "Geomorphology"){
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.geomorphimage_layout
                    , null));
            settingsDialog.show();
        }else if(text == "Land Degradation"){
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.landdegimage_layout
                    , null));
            settingsDialog.show();
        }else{
            Dialog settingsDialog = new Dialog(this);
            settingsDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            settingsDialog.setContentView(getLayoutInflater().inflate(R.layout.defaultimage_layout
                    , null));
            settingsDialog.show();

        }
    }

    private class LoadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView iv;
        public LoadImage(ImageView imageView){
            this.iv=imageView;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlLink=strings[0];
            Bitmap bitmap=null;
            try{
                InputStream inputStream = new java.net.URL(urlLink).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }catch(IOException e){
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            imview.setImageBitmap(bitmap);
        }
    }
}
