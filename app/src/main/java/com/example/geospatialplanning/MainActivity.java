package com.example.geospatialplanning;

import static androidx.fragment.app.FragmentManager.TAG;

import static com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.location.Locations;
import com.tomtom.online.sdk.map.AnimationDuration;
import com.tomtom.online.sdk.map.ApiKeyType;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapConstants;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MapProperties;
import com.tomtom.online.sdk.map.MarkerAnchor;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.search.api.SearchError;
import com.tomtom.online.sdk.search.api.fuzzy.FuzzySearchResultListener;
import com.tomtom.online.sdk.search.data.common.Poi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQuery;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResult;
import com.tomtom.online.sdk.search.extensions.SearchService;
import com.tomtom.online.sdk.search.extensions.SearchServiceConnectionCallback;
import com.tomtom.online.sdk.search.extensions.SearchServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/*to do list
* display litho map
* pois in new activity
* ui stylizing (optional)
* */

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.geospatialplanning.MESSAGE";
    private GpsTracker gpsTracker;
    private EditText latitude1, longitude1, searchEditText;
    private Button sbutton1, sbutton2, sbutton3;
    private ImageView imview, legendview;
    private Spinner dropdown;
    private String lulc_descr1, lulc_descr2, slope_class, slope_range, geomorphology_soil, altitude;
    private String drainage_category, basin_name, subbasin_name,  landdeg_descr1, landdeg_descr2, litho_class;
    private TextView details, temporary;
    private ListView resultlist;
    private ArrayAdapter adapter;
    private Double Lat_F, Lng_F;
    ArrayAdapter<String> adapter1;
    private List addressAuto;
    TomtomMap tomtomMap;
    SearchService tomtomSearch;
    ImmutableList<FuzzySearchResult> lastSearchResult;

    //ResultListAdapter adapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //home layout
        //resultlist = findViewById(R.id.resultlist);
        addressAuto= new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, addressAuto);
        //resultlist.setAdapter(adapter);
        latitude1=(EditText) findViewById(R.id.editTextTextLatitude);
        longitude1=(EditText) findViewById(R.id.editTextTextlongitude);
        //searchEditText = findViewById(R.id.searchText);
        sbutton3= (Button) findViewById(R.id.button3);



        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getAsyncMap(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull TomtomMap map) {
                tomtomMap = map;
                tomtomMap.setMyLocationEnabled(true);
            }
        });
        ServiceConnection serviceConnection = SearchServiceManager.createAndBind(getBaseContext(), new SearchServiceConnectionCallback() {
                    @Override
                    public void onBindSearchService(SearchService searchService) {
                        tomtomSearch = searchService;
                    }
        });



    }

    /*public void inputonclick(View view){
        setContentView(getLayoutInflater().inflate(R.layout.home_layout, null));
    }
    public void maplayersonclick(View view){
        setContentView(R.layout.map_layer_layout);
        dropdown.setAdapter(adapter1);
    }
    public void resultsonclick(View view){
        setContentView(getLayoutInflater().inflate(R.layout.results_layout, null));
    }
     */
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
    public void getlocationfrommap(View view){
        LatLng ltln= tomtomMap.getCenterOfMap();
        latitude1.setText(ltln.getLatitudeAsString());
        longitude1.setText(ltln.getLongitudeAsString());
    }
    public void viewlayermaps(View view){
        Intent intent = new Intent(MainActivity.this, DisplayLayerMapActivity.class);
        intent.putExtra("Final_lat", latitude1.getText().toString());
        intent.putExtra("Final_lng", longitude1.getText().toString());
        startActivity(intent);
        startActivity(intent);
    }


    /*
    public void getMap(View view){
        String text = dropdown.getSelectedItem().toString();
        String lyrs = "lulc:KA_LULC50K_1516";
        String url1 = "https://bhuvan-vec2.nrsc.gov.in/bhuvan";
        String url2 = "https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service";
        String url3 = "https://bhuvan-app3.nrsc.gov.in/gswbis/wbis";
        Double lat = Double.parseDouble(latitude1.getText().toString());
        Double lng = Double.parseDouble(longitude1.getText().toString());
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
        String urlLink=urlused+"/wms?request=getMap&bbox="+String.valueOf(lng-0.01)+
                ","+String.valueOf(lat-0.01)+","+String.valueOf(lng+0.01)+","+
                String.valueOf(lat+0.01)+"&CRS=EPSG:4326&width=1000&height=1000&layers="+lyrs +
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
    */

    public void getLayerData(View view){
        //setContentView(R.layout.activity_display_result);
        sbutton3.setText("Loading...");
        String lyrs = "lulc:KA_LULC50K_1516";
        String url1 = "https://bhuvan-vec2.nrsc.gov.in/bhuvan";
        String url2 = "https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service";
        Double lat = Double.parseDouble(latitude1.getText().toString());
        Double lng = Double.parseDouble(longitude1.getText().toString());
        String urlused = url1;
        //LULC
        lyrs = "lulc:KA_LULC50K_1516";
        urlused=url1;
        String urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
        String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        LoadInfoLULC loadInfo1 = new LoadInfoLULC();
        loadInfo1.execute(urllink);
        //Geomorphology
        urlused=url1;
        lyrs = "geomorphology:KA_GM50K_0506";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoGeomorphology loadInfo3 = new LoadInfoGeomorphology();
        loadInfo3.execute(urllink);
        //Slope
        urlused=url2;
        lyrs = "v3:slope_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoSlope loadInfo2 = new LoadInfoSlope();
        loadInfo2.execute(urllink);
        //Drainage
        urlused=url2;
        lyrs = "v3:drainage_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoDrainage loadInfo4 = new LoadInfoDrainage();
        loadInfo4.execute(urllink);
        //Hydrology
        lyrs = "v3:subbasin_ka";
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
                "&layers="+lyrs+"&query_layers="+lyrs;
        //System.out.println(urllink);
        LoadInfoHydrology loadInfo5 = new LoadInfoHydrology();
        loadInfo5.execute(urllink);
        //topology
        urllink="https://api.opentopodata.org/v1/etopo1?locations="+String.valueOf(lat)+","+String.valueOf(lng);
        LoadInfoTopology loadInfo6 =new LoadInfoTopology();
        loadInfo6.execute(urllink);

        //Litho
        String epsgurl="https://epsg.io/trans?x="+String.valueOf(lng)+"&y="+String.valueOf(lat)+"&s_srs=4326&t_srs=32644";
        urllink = "https://bhukosh.gsi.gov.in/arcgis/services/Geology/Geology_50K/MapServer/WmsServer?request=getFeatureinfo&CRS=EPSG:32644" +
                "&width=100&height=100&layers=7&query_layers=7&info_format=text/html&TRANSPARENT=TRUE&VERSION=1.3.0&STYLES=default&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001);
        //System.out.println(epsgurl);
        LoadInfolitho loadInfo7 = new LoadInfolitho();
        loadInfo7.execute(epsgurl);

        //Land degradation
        lyrs="ld:KA_LD50K_1516";
        urlused=url1;
        urllink = urlused+"/wms?request=getFeatureinfo&info_format=application/json&width=100&height=100&x=50&y=50&bbox=" +
                String.valueOf(lng-0.001)+","+String.valueOf(lat-0.001)+","+String.valueOf(lng+0.001)+","+String.valueOf(lat+0.001)+
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

    public void suggestions(View view){
        LatLng mapcords=tomtomMap.getCenterOfMap();
        latitude1.setText(mapcords.getLatitudeAsString());
        longitude1.setText(mapcords.getLongitudeAsString());
        LatLng mapCenter = new LatLng(Double.parseDouble(latitude1.getText().toString()),Double.parseDouble(longitude1.getText().toString()));

        FuzzySearchQuery searchQuery = FuzzySearchQueryBuilder.create(searchEditText.getText().toString())
                .withPosition(mapCenter)
                .build();
        tomtomSearch.search(searchQuery, new FuzzySearchResultListener() {
            private ImmutableList<FuzzySearchResult> lastSearchResult;

            @Override
            public void onSearchResult(FuzzySearchResponse fuzzySearchResponse) {
                ImmutableList<FuzzySearchResult> results = fuzzySearchResponse.getResults();
                showSearchResults(results);
            }

            void showSearchResults(ImmutableList<FuzzySearchResult> resultList)
            {
                //Log.i(TAG, resultList.toString());
                this.lastSearchResult = resultList;
                adapter.notifyDataSetChanged();
                System.out.println(lastSearchResult);
                tomtomMap.clear();
                if(this.lastSearchResult.size() == 0)
                {
                    Toast.makeText(getBaseContext(), "No locations found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                for(int i=0;i<lastSearchResult.size();++i)
                {
                    LatLng geoposition = lastSearchResult.get(i).getPosition();
                    Poi poi = lastSearchResult.get(i).getPoi();
                    String addressstring=lastSearchResult.get(i).getAddress().getFreeformAddress();
                    addressAuto.add(addressstring);
                    adapter.notifyDataSetChanged();
                    MarkerBuilder markerBuilder = new MarkerBuilder(geoposition)
                            .icon(Icon.Factory.fromResources(getBaseContext(),
                                    R.drawable.location_icon))
                            .markerBalloon(new SimpleMarkerBalloon(poi.getName()))
                            .tag(lastSearchResult.get(i).getAddress())
                            .iconAnchor(MarkerAnchor.Bottom)
                            .decal(true);
                    tomtomMap.addMarker(markerBuilder);
                }
            }

            @Override
            public void onSearchError(SearchError searchError) {
            }
        });


    }



    /*
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
    */
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
                //System.out.println(data);
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
                    //System.out.println(slope_class+" "+slope_range);
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

    private class LoadInfolitho extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String urlink=strings[0];
            String data = "", response= "", translatedurl="";
            try{
                URL urlchng = new URL(urlink);
                HttpURLConnection httpURLConnectionch = (HttpURLConnection) urlchng.openConnection();
                InputStream inputStream1 = httpURLConnectionch.getInputStream();
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
                String line1;

                while((line1 =bufferedReader1.readLine()) != null){
                    response = response + line1;
                }
                if(!response.isEmpty()){
                    JSONObject jsonObject = new JSONObject(response);
                    String x=jsonObject.getString("x");
                    String y=jsonObject.getString("y");
                    translatedurl= "https://bhukosh.gsi.gov.in/arcgis/services/Geology/Geology_50K/MapServer/WmsServer?request=getFeatureinfo&CRS=EPSG:32644" +
                            "&width=100&height=100&layers=7&query_layers=7&info_format=text/html&TRANSPARENT=TRUE&VERSION=1.3.0&STYLES=default&bbox=" +
                            String.valueOf(Double.parseDouble(x)-100)+","+String.valueOf(Double.parseDouble(y)-100)+","+String.valueOf(Double.parseDouble(x)+100)+","+String.valueOf(Double.parseDouble(y)+100);
                    //System.out.println(translatedurl);
                }
                System.out.println(translatedurl);
                URL url = new URL(translatedurl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line =bufferedReader.readLine()) != null){
                    data = data + line;
                }

                if(!data.isEmpty()){
                    URL uri= new URL("https://html2json.com/api/v1");
                    HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setChunkedStreamingMode(0);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();
                    conn.connect();

                    String result;
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    int result2 = bis.read();
                    while(result2 != -1) {
                        buf.write((byte) result2);
                        result2 = bis.read();
                    }
                    result = buf.toString();
                    if(!result.isEmpty()){
                        JSONObject jsonObject = new JSONObject(result);
                        litho_class = jsonObject.getJSONObject("data").getJSONArray("tables").getJSONObject(0).getJSONArray("rows").getJSONObject(1).getJSONArray("cols").getJSONObject(3).getString("nodeValue");
                        //System.out.println(geomorphology_soil);
                    }
                    //System.out.println(result);
                    System.out.println(litho_class);
                    /*OkHttpClient client = new OkHttpClient().newBuilder()
                            .build();
                    MediaType mediaType = MediaType.parse("text/html");
                    RequestBody body = RequestBody.create(mediaType, "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\r\n<html xmlns:esri_wms=\"http://www.esri.com/wms\" xmlns=\"http://www.esri.com/wms\">\r\n\r\n<head>\r\n\t<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\r\n\t</meta>\r\n\t<style type=\"text/css\">\r\n\t\ttable,\r\n\t\tth,\r\n\t\ttd {\r\n\t\t\tborder: 1px solid #e5e5e5;\r\n\t\t\tborder-collapse: collapse;\r\n\t\t\tfont-family: arial;\r\n\t\t\tfont-size: 80%;\r\n\t\t\tcolor: #333333\r\n\t\t}\r\n\r\n\t\tth,\r\n\t\ttd {\r\n\t\t\tvalign: top;\r\n\t\t\ttext-align: center;\r\n\t\t}\r\n\r\n\t\tth {\r\n\t\t\tbackground-color: #aed7ff\r\n\t\t}\r\n\t</style>\r\n</head>\r\n\r\n<body>\r\n\t<h5>FeatureInfoCollection - layer name: 'Lithology'</h5>\r\n\t<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"1\">\r\n\t\t<tbody>\r\n\t\t\t<tr>\r\n\t\t\t\t<th>OBJECTID</th>\r\n\t\t\t\t<th>INPUT CENTER CODE</th>\r\n\t\t\t\t<th>TOPOSHEET NUMBER</th>\r\n\t\t\t\t<th>AGE</th>\r\n\t\t\t\t<th>SUPER GROUP</th>\r\n\t\t\t\t<th>GROUP NAME</th>\r\n\t\t\t\t<th>FORMATION</th>\r\n\t\t\t\t<th>MEMBER</th>\r\n\t\t\t\t<th>LITHOLOGICAL UNIT</th>\r\n\t\t\t\t<th>SUB GROUP</th>\r\n\t\t\t\t<th>NEWGEOM_ID</th>\r\n\t\t\t\t<th>UID_NOTATION_NEW</th>\r\n\t\t\t\t<th>INTRUSIVE</th>\r\n\t\t\t\t<th>SCRIPT</th>\r\n\t\t\t\t<th>NOTATION</th>\r\n\t\t\t\t<th>STRATIGRAPHY</th>\r\n\t\t\t\t<th>Shape</th>\r\n\t\t\t</tr>\r\n\t\t\t<tr>\r\n\t\t\t\t<td>188024</td>\r\n\t\t\t\t<td>JAI</td>\r\n\t\t\t\t<td>41J10</td>\r\n\t\t\t\t<td>LATE CRETACEOUS-PALAEOCENE</td>\r\n\t\t\t\t<td>DECCAN TRAP</td>\r\n\t\t\t\t<td>SAURASTRA</td>\r\n\t\t\t\t<td>KHAMBHALIYA VOLCANICS</td>\r\n\t\t\t\t<td>Null</td>\r\n\t\t\t\t<td>COMPOUND BASALT</td>\r\n\t\t\t\t<td>Null</td>\r\n\t\t\t\t<td>4774530811093185</td>\r\n\t\t\t\t<td>1110</td>\r\n\t\t\t\t<td>Null</td>\r\n\t\t\t\t<td>βK2Pg1dskv9</td>\r\n\t\t\t\t<td>βK&lt;SUB&gt;2&lt;/SUB&gt;Pg&lt;SUB&gt;1&lt;/SUB&gt;dskv9</td>\r\n\t\t\t\t<td>4774530811093185,LATE CRETACEOUS-PALAEOCENE , COMPOUND BASALT,1110</td>\r\n\t\t\t\t<td>Polygon</td>\r\n\t\t\t</tr>\r\n\t\t</tbody>\r\n\t</table>\r\n</body>\r\n\r\n</html>");
                    Request request = new Request.Builder()
                            .url("https://html2json.com/api/v1")
                            .method("POST", body)
                            .addHeader("Content-Type", "text/html")
                            .addHeader("Cookie", "PHPSESSID=d8ea22a91fd703b361b466709c4309e6")
                            .build();
                    Response response = client.newCall(request).execute();*/
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
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
                    //System.out.println(basin_name);
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
                    //System.out.println(altitude);
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
            intent.putExtra("Final_lat", latitude1.getText().toString());
            intent.putExtra("Final_lng", longitude1.getText().toString());
            intent.putExtra("LULC_1",lulc_descr1);
            intent.putExtra("LULC_2",lulc_descr2);
            intent.putExtra("SLOPE_1",slope_class);
            intent.putExtra("SLOPE_2",slope_range);
            intent.putExtra("GEOMORPH",geomorphology_soil);
            intent.putExtra("DRAINAGE",drainage_category);
            intent.putExtra("BASIN",basin_name);
            intent.putExtra("SUBBASIN",subbasin_name);
            intent.putExtra("LDEG_1",landdeg_descr1);
            intent.putExtra("LDEG_2",landdeg_descr2);
            intent.putExtra("ALT",altitude);
            intent.putExtra("LITHO", litho_class);

            startActivity(intent);

        }

    }
}