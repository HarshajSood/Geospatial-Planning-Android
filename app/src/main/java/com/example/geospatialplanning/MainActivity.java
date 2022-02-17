package com.example.geospatialplanning;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.geospatialplanning.MESSAGE";
    private GpsTracker gpsTracker;
    private EditText latitude1, longitude1;
    private Button sbutton1, sbutton2, sbutton3;
    private ImageView imview, legendview;
    private Spinner dropdown;
    private String lulc_descr1, lulc_descr2, slope_class, slope_range, geomorphology_soil, altitude;
    private String drainage_category, basin_name, subbasin_name,  landdeg_descr1, landdeg_descr2;
    private TextView details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"LULC", "Slope", "Drainage", "Geomorphology", "Hydrology", "Land Degradation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);



        latitude1=(EditText) findViewById(R.id.editTextTextLatitude);
        longitude1=(EditText) findViewById(R.id.editTextTextlongitude);
        imview=(ImageView) findViewById(R.id.imageView);
        details= (TextView) findViewById(R.id.textView3);
        sbutton3= (Button) findViewById(R.id.button3);


    }

    public void getLocation(View view){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            latitude1.setText(String.valueOf(latitude));
            longitude1.setText(String.valueOf(longitude));
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    public void getMap(View view){
        String text = dropdown.getSelectedItem().toString();
        String lyrs = "lulc:KA_LULC50K_1516";
        String url1 = "https://bhuvan-vec2.nrsc.gov.in/bhuvan";
        String url2 = "https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service";
        String url3 = "https://bhuvan-app3.nrsc.gov.in/gswbis/wbis";
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
        System.out.println(text);
        String urlLink=urlused+"/wms?request=getMap&bbox="+String.valueOf(gpsTracker.getLongitude()-0.01)+
                ","+String.valueOf(gpsTracker.getLatitude()-0.01)+","+String.valueOf(gpsTracker.getLongitude()+0.01)+","+
                String.valueOf(gpsTracker.getLatitude()+0.01)+"&CRS=EPSG:4326&width=1000&height=1000&layers="+lyrs +
                "&format=image/png&TRANSPARENT=TRUE";
        System.out.println("\n"+urlLink+"\n");
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


    public void getLayerData(View view){
        sbutton3.setText("Loading...");
        String lyrs = "lulc:KA_LULC50K_1516";
        String url1 = "https://bhuvan-vec2.nrsc.gov.in/bhuvan";
        String url2 = "https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service";
        String urlused = url1;
        //LULC
        lyrs = "lulc:KA_LULC50K_1516";
        urlused=url1;
        String urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
        String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        LoadInfoLULC loadInfo1 = new LoadInfoLULC();
        loadInfo1.execute(urllink);
        //Geomorphology
        urlused=url1;
        lyrs = "geomorphology:KA_GM50K_0506";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoGeomorphology loadInfo3 = new LoadInfoGeomorphology();
        loadInfo3.execute(urllink);
        //Slope
        urlused=url2;
        lyrs = "v3:slope_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoSlope loadInfo2 = new LoadInfoSlope();
        loadInfo2.execute(urllink);
        //Drainage
        urlused=url2;
        lyrs = "v3:drainage_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoDrainage loadInfo4 = new LoadInfoDrainage();
        loadInfo4.execute(urllink);
        //Hydrology
        lyrs = "v3:subbasin_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoHydrology loadInfo5 = new LoadInfoHydrology();
        loadInfo5.execute(urllink);
        //topology
        urllink="https://api.opentopodata.org/v1/etopo1?locations="+String.valueOf(gpsTracker.getLatitude())+","+String.valueOf(gpsTracker.getLongitude());
        LoadInfoTopology loadInfo6 =new LoadInfoTopology();
        loadInfo6.execute(urllink);

        //Land degradation
        lyrs="ld:KA_LD50K_1516";
        urlused=url1;
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(gpsTracker.getLongitude()-0.001)+","+String.valueOf(gpsTracker.getLatitude()-0.001)+","+String.valueOf(gpsTracker.getLongitude()+0.001)+","+String.valueOf(gpsTracker.getLatitude()+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoLandDeg loadInfo9 = new LoadInfoLandDeg();
        loadInfo9.execute(urllink);


        //details.setText("Loading...");
        /*Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                details.setText(lulc_descr1+" "+lulc_descr2+"\n"+slope_class+" "+slope_range+"\n"+ geomorphology_soil+
                        "\n"+drainage_category+"\n"+basin_name+" "+subbasin_name+"\n"+landdeg_descr1+" "+landdeg_descr2);
            }
        }, 10000);*/

        /*Intent intent = new Intent(this, DisplayResultActivity.class);
        intent.putExtra(EXTRA_MESSAGE, lulc_descr1);
        startActivity(intent);*/



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

    private class LoadInfoLULC extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }
                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    lulc_descr1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DESCR_1");
                    lulc_descr2 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DESCR_2");
                    //System.out.println(lulc_descr1+" "+lulc_descr2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }

    }

    private class LoadInfoSlope extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }
                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    slope_class = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("class");
                    slope_range = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("range");
                    System.out.println(slope_class+" "+slope_range);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }
    }

    private class LoadInfoGeomorphology extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }
                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    geomorphology_soil = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("Des");
                    //System.out.println(geomorphology_soil);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }
    }

    private class LoadInfoDrainage extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }

                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    drainage_category = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("category");
                    //System.out.println(drainage_category);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }
    }

    private class LoadInfoHydrology extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }

                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    basin_name= jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("ba_name");
                    subbasin_name= jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("sub_basin");
                    System.out.println(basin_name);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }
    }

    private class LoadInfoTopology extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }
                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    altitude = jsonObject.getJSONArray("results").getJSONObject(0).getString("elevation");
                    System.out.println(altitude);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }

    }

    private class LoadInfoLandDeg extends AsyncTask<String, Void, TextView> {
        /*TextView tv;
        public LoadInfoLandDeg(TextView textView){
            this.tv=textView;
        }*/
        @Override
        protected TextView doInBackground(String... strings) {
            String urlink=strings[0];
            TextView textView=null;
            String data = "";
            try{
                URL url = new URL(urlink);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }
                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    landdeg_descr1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DS_1516_L1");
                    landdeg_descr2 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DS_1516_L2");
                    //System.out.println(lulc_descr1+" "+lulc_descr2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            //System.out.println(data);
            return null;
        }

        @Override
        protected void onPostExecute(TextView textView){
            String message = lulc_descr1+" "+lulc_descr2+"\n"+slope_class+" "+slope_range+"\n"+ geomorphology_soil+
                    "\n"+drainage_category+"\n"+basin_name+" "+subbasin_name+"\n"+landdeg_descr1+" "+landdeg_descr2+"\n"+altitude;
            /*details.setText(lulc_descr1+" "+lulc_descr2+"\n"+slope_class+" "+slope_range+"\n"+ geomorphology_soil+
                    "\n"+drainage_category+"\n"+basin_name+" "+subbasin_name+"\n"+landdeg_descr1+" "+landdeg_descr2+"\n"+altitude);*/
            sbutton3.setText("Calculate Risk Score");
            Intent intent = new Intent(MainActivity.this, DisplayResultActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);

        }

    }
}