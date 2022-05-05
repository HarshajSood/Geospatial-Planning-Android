package com.example.geospatialplanning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DisplayResultActivity extends AppCompatActivity {
    private GpsTracker gpsTracker;
    private TextView hospitaltv,schooltv, policetv, queryresult, culturename1, culturename2, culturedist1, culturedist2, resultclassification, textView;
    private EditText poisearch;
    int[][] colorgridarr;
    String value1, value2, value3, valueq, valuen, valueq1, valuen1;
    private String lulc_descr1, lulc_descr2, slope_class, slope_range, geomorphology_soil, altitude;
    private String drainage_category, basin_name, subbasin_name,  landdeg_descr1, landdeg_descr2, litho_desc, lat_loc, lng_loc;
    Integer index;
    DecimalFormat formatter, grdfrt;
    private Button gridbtn;
    private ImageView gridimg;
    private RadioGroup rg;
    private RadioButton rb, disable1, disable2, disable3, disable4, disable5;
    TomtomMap tomtomMap1;
    SearchService tomtomSearch1;
    gridtask gtask;
    private int radioselectnum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);
        Integer final_classification=0, lulc_val=2, slope_val=2, drainage_val=2, geomorph_val=2, hydrology_val=2, landdeg_val=2;

        Intent intent=getIntent();
        String message=intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        lulc_descr1=this.getIntent().getExtras().getString("LULC_1");
        lulc_descr2=this.getIntent().getExtras().getString("LULC_2");
        slope_class=this.getIntent().getExtras().getString("SLOPE_1");
        slope_range=this.getIntent().getExtras().getString("SLOPE_2");
        geomorphology_soil=this.getIntent().getExtras().getString("GEOMORPH");
        drainage_category=this.getIntent().getExtras().getString("DRAINAGE");
        basin_name=this.getIntent().getExtras().getString("BASIN");
        subbasin_name=this.getIntent().getExtras().getString("SUBBASIN");
        landdeg_descr1=this.getIntent().getExtras().getString("LDEG_1");
        landdeg_descr2=this.getIntent().getExtras().getString("LDEG_2");
        altitude=this.getIntent().getExtras().getString("ALT");
        lat_loc=this.getIntent().getExtras().getString("Final_lat");
        lng_loc=this.getIntent().getExtras().getString("Final_lng");
        litho_desc=this.getIntent().getExtras().getString("LITHO");

        textView = findViewById(R.id.textView4);
        hospitaltv=(TextView) findViewById(R.id.hospitaltextview);
        schooltv=(TextView) findViewById(R.id.schooltextview);
        policetv=(TextView) findViewById(R.id.policetextview);
        queryresult=(TextView) findViewById(R.id.querytextView);
        culturename1=(TextView) findViewById(R.id.culturename1);
        culturename2=(TextView) findViewById(R.id.culturename2);
        culturedist1=(TextView) findViewById(R.id.culturetextview1);
        culturedist2=(TextView) findViewById(R.id.culturetextview2);
        poisearch=(EditText) findViewById(R.id.poitext);
        resultclassification=(TextView) findViewById(R.id.textView6);
        //gridbtn= (Button) findViewById(R.id.generategridbtn);
        gridimg= (ImageView) findViewById(R.id.gridviewimage);
        rg= (RadioGroup) findViewById(R.id.rdgroup);
        disable1 = (RadioButton) findViewById(R.id.radioButton);
        disable2 = (RadioButton) findViewById(R.id.radioButton2);
        disable3 = (RadioButton) findViewById(R.id.radioButton3);
        disable4 = (RadioButton) findViewById(R.id.radioButton4);
        disable5 = (RadioButton) findViewById(R.id.radioButton5);

        formatter = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.DOWN );

        //textView.setText(message);
        //textView.setText(message+lulc_descr1);
        //System.out.println("----------------------------------------------------------------------------------------------*****&&&&&&&&******");
        //System.out.println(lulc_descr1);
        //System.out.println("landdeg:-------------"+landdeg_descr1+" "+landdeg_descr2+"end");

        colorgridarr = new int[10][10];
        for(int i=0; i<10; i++)
        {
            for(int j=0; j<10; j++)
            {
                colorgridarr[i][j]=2;
            }
        }
        //color coding temp experiment

        /*for(int i=0; i<10; i++)
            {
                for(int j=0; j<10; j++)
                {
                    colorgridarr[i][j]=(i+j)%3;
                    makegrid(i,j);
                }
            }
        */
        //maps
        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map_fragment1);
        //Log.d(TAG, "Request map from map fragment");
        mapFragment.getAsyncMap(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull TomtomMap map) {
                ////Log.d(TAG, "Map retrieved");
                tomtomMap1 = map;
                tomtomMap1.setMyLocationEnabled(true);
                LatLng position=new LatLng(Double.parseDouble(lat_loc),Double.parseDouble(lng_loc));

                //Set the map view somewhere near San Jose
                tomtomMap1.centerOn(CameraPosition.builder().focusPosition(position).zoom(13).build());
            }

        });

        ServiceConnection serviceConnection = SearchServiceManager.createAndBind(getBaseContext(), new SearchServiceConnectionCallback() {
            @Override
            public void onBindSearchService(SearchService searchService) {
                //Log.d(TAG,"Search service retrieved");
                tomtomSearch1 = searchService;
            }
        });


        try{
            //lulc
            if((lulc_descr2.equals("Urban"))||(lulc_descr2.equals("Rural"))){
                lulc_val=2;
                lulc_descr1="<font color=#6EAD46>"+lulc_descr1+"</font>";
                lulc_descr2="<font color=#6EAD46>"+lulc_descr2+"</font>";
            }
            else
            {
                lulc_val=0;
                lulc_descr1="<font color=#FF0000>"+lulc_descr1+"</font>";
                lulc_descr2="<font color=#FF0000>"+lulc_descr2+"</font>";
            }
            //slope
            if(slope_class.equals("Level to nearly level")||slope_class.equals("Very gently sloping")||slope_class.equals("Gently sloping")){
                slope_val=2;
                slope_class="<font color=#6EAD46>"+slope_class+"</font>";
                slope_range="<font color=#6EAD46>"+slope_range+"</font>";

            }
            else if(slope_class.equals("Moderately sloping")||slope_class.equals("Moderately steeply sloping")){
                slope_val=1;
                slope_class="<font color=#4E79C8>"+slope_class+"</font>";
                slope_range="<font color=#4E79C8>"+slope_range+"</font>";
            }else{
                slope_val=0;
                slope_class="<font color=#FF0000>"+slope_class+"</font>";
                slope_range="<font color=#FF0000>"+slope_range+"</font>";
            }
            //drainage
            if(drainage_category==null){
                drainage_val=2;
                drainage_category="<font color=#6EAD46>"+drainage_category+"</font>";
            }
            else{
                drainage_val=0;
                drainage_category="<font color=#6EAD46>"+drainage_category+"</font>";
            }
            //geomorphology
            if(geomorphology_soil.equals("Denudational Origin-Low Dissected Lower Plateau"))
            {
                geomorphology_soil="<font color=#4E79C8>"+geomorphology_soil+"</font>";
            }
            else if(geomorphology_soil.equals("Structural Origin-Low Dissected Hills and Valleys")||geomorphology_soil.equals("Structural Origin-Low Dissected Upper Plateau")
                    ||geomorphology_soil.equals("Structural Origin-Low Dissected Lower Plateau")||geomorphology_soil.equals("Denudational Origin - Low Dissected Hills and Valleys")
                    ||geomorphology_soil.equals("Denudational Origin-Low Dissected Upper Plateau")||geomorphology_soil.equals("Fluvial Origin-Older Alluvial Plain")
                    ||geomorphology_soil.equals("Fluvial Origin-Older Flood Plain")||geomorphology_soil.equals("Coastal Origin-Offshore Island")
                    ||geomorphology_soil.equals("Anthropogenic Origin-Anthropogenic Terrain") || geomorphology_soil.equals("Denudational Origin-Pediment-PediPlain Complex"))
            {
                geomorphology_soil="<font color=#4E79C8>"+geomorphology_soil+"</font>";
            }
            else{
                geomorphology_soil="<font color=#FF0000>"+geomorphology_soil+"</font>";
            }
            //land deg
            if (landdeg_descr1.equals("Normal")){
                landdeg_descr1="<font color=#6EAD46>"+landdeg_descr1+"</font>";
                landdeg_descr2="<font color=#6EAD46>"+landdeg_descr2+"</font>";
            }
            else if(landdeg_descr1.equals("Others")||landdeg_descr1.equals("Anthropogenic"))
            {
                landdeg_descr1="<font color=#4E79C8>"+landdeg_descr1+"</font>";
                landdeg_descr2="<font color=#4E79C8>"+landdeg_descr2+"</font>";
            }else
            {
                landdeg_descr1="<font color=#FF0000>"+landdeg_descr1+"</font>";
                landdeg_descr2="<font color=#FF0000>"+landdeg_descr2+"</font>";
            }
        }catch (Exception E){
            E.printStackTrace();
        }

        //final classification
        if(lulc_val==0||slope_val==0||drainage_val==0)
        {
            final_classification=0;
        }else if(lulc_val==1||slope_val==1||drainage_val==1)
        {
            final_classification=1;
        }
        else if(lulc_val==2&&slope_val==2&&drainage_val==2)
        {
            final_classification=2;

        }
        switch (final_classification){
            case 0:{
                resultclassification.setText("Not to build Zone");
                resultclassification.setTextColor(Color.parseColor("#FF0000"));
                break;
            }
            case 1:{
                resultclassification.setText("Medium safe to build Zone");
                resultclassification.setTextColor(Color.parseColor("#4E79C8"));
                break;
            }
            case 2:{
                resultclassification.setText("Safe to build Zone");
                resultclassification.setTextColor(Color.parseColor("#6EAD46"));
                break;
            }

        }


        String accesstoken="https://api.tomtom.com/search/2/poiSearch/hospital.json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        LoadnearbyinfoHospital nearby1 = new LoadnearbyinfoHospital();
        nearby1.execute(accesstoken);
        accesstoken="https://api.tomtom.com/search/2/poiSearch/school.json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        LoadnearbyinfoSchool nearby2 = new LoadnearbyinfoSchool();
        nearby2.execute(accesstoken);
        accesstoken="https://api.tomtom.com/search/2/poiSearch/police.json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        LoadnearbyinfoPolice nearby3 = new LoadnearbyinfoPolice();
        nearby3.execute(accesstoken);

        String culturalplaceslink="https://api.tomtom.com/search/2/poiSearch/historic.json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        LoadCulturalplaces culture1 = new LoadCulturalplaces();
        culture1.execute(culturalplaceslink);
    }

    public void detailexpand(View view){
        textView.setText(Html.fromHtml("LULC: "+lulc_descr1+" "+lulc_descr2+"<br>"+"</font>Slope: "+slope_class+" "+slope_range+"</font><br>"+"Drainage: "+drainage_category+"<br>"
                +"Geomorphology: "+geomorphology_soil+"<br>"+"Hydrology: "+basin_name+" "+subbasin_name+"<br>"+"Land Degradation: "+landdeg_descr1+" "+landdeg_descr2+
                "<br>"+"Altitude: "+altitude+"<br>"+"Lithography: "+litho_desc));
    }

    public void getplaceofinterest(View view){
        String query=poisearch.getText().toString();
        index=0;
        String queryurl="https://api.tomtom.com/search/2/poiSearch/"+query+".json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        Loadnearbyinfoquery squery= new Loadnearbyinfoquery();
        squery.execute(queryurl);
    }

    public void getnext(View view){
        index++;
        String query=poisearch.getText().toString();
        String queryurl="https://api.tomtom.com/search/2/poiSearch/"+query+".json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        Loadnearbyinfoquery squery= new Loadnearbyinfoquery();
        squery.execute(queryurl);

    }

    public void getprevious(View view){
        index--;
        if(index<0)
            index=0;
        String query=poisearch.getText().toString();
        String queryurl="https://api.tomtom.com/search/2/poiSearch/"+query+".json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        Loadnearbyinfoquery squery= new Loadnearbyinfoquery();
        squery.execute(queryurl);

    }

    private static class MyTaskParams {
        double lt;
        double ln;
        int i;
        int j;
        int rsnum;

        MyTaskParams(double lt, double ln, int i, int j, int rsnum) {
            this.lt = lt;
            this.ln = ln;
            this.i = i;
            this.j = j;
            this.rsnum=rsnum;
        }
    }

    public void getgridmap(View view){
        gridbtn.setText("Loading...");
        double gridlat=Double.parseDouble(lat_loc);
        double gridlng=Double.parseDouble(lng_loc);
        double startlat, endlat, startlng, endlng, diff,i, j;
        int counti, countj;
        MyTaskParams params;
        startlat=gridlat+0.0009;
        endlat=gridlat-0.0009;
        startlng=gridlng-0.0009;
        endlng=gridlng+0.0009;
        diff=0.0001;
        /*params=new MyTaskParams(gridlat, gridlng);
        gridtask gtask=new gridtask();
        gtask.execute(params);

        */
        //makegrid();
        String gridurl="https://api.tomtom.com/map/1/wms/?request=GetMap&srs=EPSG%3A4326&width=512&height=512&format=image%2Fpng&layers=basic" +
                "&styles=&version=1.1.1&key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&bbox="+String.valueOf(startlng)+","+String.valueOf(startlat)+","+String.valueOf(endlng)+","+String.valueOf(endlat);
        //System.out.println(gridurl);
        LoadImage loadImage = new LoadImage(gridimg);
        loadImage.execute(gridurl);
        for(i=startlat, counti=0; i>=endlat; i=i-2*diff, counti++)
        {
            for(j=startlng, countj=0; j<=endlng+2*diff; j=j+2*diff, countj++)
            {
                params=new MyTaskParams(i, j, counti, countj, 5);
                gridtask gtask=new gridtask();
                gtask.execute(params);

            }
        }

        //makegrid();

    }

    public void newactivityguidelines(View view)
    {
        Intent intent = new Intent(DisplayResultActivity.this, guidelines.class);
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

        startActivity(intent);
    }
    public void makegrid(int i, int j){
        String name="grid"+Integer.toString(i)+Integer.toString(j);
        int id=getResources().getIdentifier(name, "id", getPackageName());
        TextView temp=(TextView)findViewById(id);
        switch(colorgridarr[i][j]){
            case 0:{
                temp.setBackgroundColor(Color.RED);
                break;
            }
            case 1:{
                temp.setBackgroundColor(Color.BLUE);
                break;
            }
            case 2:{
                temp.setBackgroundColor(Color.GREEN);
                break;
            }
        }
    }
    public void cleargrid(){
        for(int i=0; i<10; i++)
        {
            for(int j=0; j<10; j++)
            {
                String name="grid"+Integer.toString(i)+Integer.toString(j);
                int id=getResources().getIdentifier(name, "id", getPackageName());
                TextView temp=(TextView)findViewById(id);
                temp.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    public void radioheadselection(View view){
        rg.setEnabled(false);
        disable1.setEnabled(false);
        disable2.setEnabled(false);
        disable3.setEnabled(false);
        disable4.setEnabled(false);
        disable5.setEnabled(false);
        cleargrid();
        //gtask.cancel(true);
        /*if(gtask.getStatus().equals(AsyncTask.Status.RUNNING))
        {
            gtask.cancel(true);
        }*/
        for(int i=0; i<10; i++)
        {
            for(int j=0; j<10; j++)
            {
                colorgridarr[i][j]=2;
            }
        }
        int selectid= rg.getCheckedRadioButtonId();
        rb=(RadioButton) findViewById(selectid);
        String radiotext=rb.getText().toString();
        if(radiotext.equals("LULC"))
        {
            radioselectnum=1;
        }
        else if(radiotext.equals("Slope"))
        {
            radioselectnum=2;
        }
        else if(radiotext.equals("Geomorph."))
        {
            radioselectnum=3;
        }
        else if(radiotext.equals("Land Deg."))
        {
            radioselectnum=4;
        }
        else
        {
            radioselectnum=5;
        }
        double gridlat=Double.parseDouble(lat_loc);
        double gridlng=Double.parseDouble(lng_loc);
        double startlat, endlat, startlng, endlng, diff,i, j;
        int counti, countj;
        MyTaskParams params;
        startlat=gridlat+0.0009;
        endlat=gridlat-0.0009;
        startlng=gridlng-0.0009;
        endlng=gridlng+0.0009;
        diff=0.0001;
        /*params=new MyTaskParams(gridlat, gridlng);
        gridtask gtask=new gridtask();
        gtask.execute(params);

        */
        //makegrid();
        String gridurl="https://api.tomtom.com/map/1/wms/?request=GetMap&srs=EPSG%3A4326&width=512&height=512&format=image%2Fpng&layers=basic" +
                "&styles=&version=1.1.1&key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&bbox="+String.valueOf(startlng)+","+String.valueOf(endlat)+","+String.valueOf(endlng)+","+String.valueOf(startlat);
        //System.out.println(gridurl);
        LoadImage loadImage = new LoadImage(gridimg);
        loadImage.execute(gridurl);
        for(i=startlat, counti=0; i>=endlat; i=i-2*diff, counti++)
        {
            for(j=startlng, countj=0; j<=endlng+2*diff; j=j+2*diff, countj++)
            {
                params=new MyTaskParams(i, j, counti, countj, radioselectnum);
                gtask=new gridtask();
                gtask.execute(params);

            }
        }
    }




    private class LoadnearbyinfoHospital extends AsyncTask<String, Void, TextView> {

        @Override
        protected TextView doInBackground(String... strings) {
            String link=strings[0];
            String keyword="hlthsp";

            //System.out.println(link);
            String data = "";
            try{
                //hospital
                URL url = new URL(link);
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
                    value1 = jsonObject.getJSONArray("results").getJSONObject(0).getString("dist");
                    //System.out.println("DISTANCE HOSPITAAL"+value1);
                }
                //value="123";



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
            Double temp=Double.parseDouble(value1);
            String a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            value1= formatter.format(temp);
            hospitaltv.setText(value1+" "+a);


        }
    }
    private class LoadnearbyinfoSchool extends AsyncTask<String, Void, TextView> {

        @Override
        protected TextView doInBackground(String... strings) {
            String link=strings[0];
            String keyword="hlthsp";

            //System.out.println(link);
            String data = "";
            try{
                //hospital
                URL url = new URL(link);
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
                    value2 = jsonObject.getJSONArray("results").getJSONObject(0).getString("dist");
                    //System.out.println("DISTANCE HOSPITAAL"+value2);
                }
                //value="123";



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
            Double temp=Double.parseDouble(value2);
            String a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            value2= formatter.format(temp);
            schooltv.setText(value2+" "+a);

        }
    }
    private class LoadnearbyinfoPolice extends AsyncTask<String, Void, TextView> {

        @Override
        protected TextView doInBackground(String... strings) {
            String link=strings[0];
            String keyword="hlthsp";

            //System.out.println(link);
            String data = "";
            try{
                //hospital
                URL url = new URL(link);
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
                    value3 = jsonObject.getJSONArray("results").getJSONObject(0).getString("dist");
                    //System.out.println("DISTANCE police"+value3);
                }
                //value="123";
                //hospitaltv.setText(value3+"m");


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
            Double temp=Double.parseDouble(value3);
            String a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            value3= formatter.format(temp);
            policetv.setText(value3+" "+a);

            //policetv.setText(value3+"m");

        }
    }

    private class Loadnearbyinfoquery extends AsyncTask<String, Void, TextView> {

        @Override
        protected TextView doInBackground(String... strings) {
            String link=strings[0];
            //System.out.println(link);
            String data = "";
            try{
                URL url = new URL(link);
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
                    valuen = jsonObject.getJSONArray("results").getJSONObject(index).getJSONObject("poi").getString("name");
                    valueq = jsonObject.getJSONArray("results").getJSONObject(index).getString("dist");
                    //System.out.println("string query::"+valuen+" "+valueq);
                }
                //value="123";




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
            //queryresult.setText(valuen+"\n"+valueq+" metres");
            Double temp=Double.parseDouble(valueq);
            String a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            valueq= formatter.format(temp);
            queryresult.setText(valuen+"\n"+valueq+" "+a);

        }
    }

    private class LoadCulturalplaces extends AsyncTask<String, Void, TextView> {

        @Override
        protected TextView doInBackground(String... strings) {
            String link=strings[0];
            //System.out.println(link);
            String data = "";
            try{
                URL url = new URL(link);
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
                    valuen = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("poi").getString("name");
                    valueq = jsonObject.getJSONArray("results").getJSONObject(0).getString("dist");

                    valuen1 = jsonObject.getJSONArray("results").getJSONObject(1).getJSONObject("poi").getString("name");
                    valueq1 = jsonObject.getJSONArray("results").getJSONObject(1).getString("dist");


                    //System.out.println("string query::"+valuen+" "+valueq);
                }
                //value="123";
                //hospitaltv.setText(value3+"m");



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
            Double temp=Double.parseDouble(valueq);
            String a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            valueq= formatter.format(temp);
            culturename1.setText(valuen);
            culturedist1.setText(valueq+" "+a);
            culturename2.setText(valuen1);
            temp=Double.parseDouble(valueq1);
            a=(temp<1000?"Metres":"Kilometres");;
            temp=(temp>1000?temp/1000:temp);
            valueq1= formatter.format(temp);
            culturedist2.setText(valueq1+" "+a);

            //queryresult.setText(valuen+"\n"+valueq+" metres");

        }
    }

    private class gridtask extends AsyncTask<MyTaskParams, Void, Void>{
        int i;
        int j;
        int rnum;
        double lt, ln, diff;
        String var1, var2;
        int clssify;
        String data, urlink;
        @Override
        protected Void doInBackground(MyTaskParams... params){
            lt=params[0].lt;

            ln=params[0].ln;
            i=params[0].i;
            j=params[0].j;
            rnum=params[0].rsnum;
            diff=0.0001;
            clssify=2;
            switch(rnum){
                case 1:lulcgridmap();
                    break;
                case 2:slopegridmap();
                    break;
                case 3:geomorphgridmap();
                    break;
                case 4:ldeggridmap();
                    break;
                case 5:{
                    lulcgridmap();
                    slopegridmap();
                    ldeggridmap();
                    //geomorphgridmap();
                    break;
                }


            }

            return null;
        }

        Void slopegridmap(){
            data = "";
            urlink="https://bhuvan-panchayat3.nrsc.gov.in/geoserver/gwc/service/wms?request=getFeatureinfo" +
                    "&info_format=application/json&width=100&height=100&x=50&y=50&layers=v3:slope_ka" +
                    "&query_layers=v3:slope_ka&bbox="+String.valueOf(ln-diff)+","+String.valueOf(lt-diff)+","+String.valueOf(ln+diff)+","+String.valueOf(lt+diff);
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
                    var1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("class");
                    System.out.println(var1);
                    var2 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("range");
                    if(var1.equals("Level to nearly level")||var1.equals("Very gently sloping")||var1.equals("Gently sloping")){
                        clssify=2;

                    }
                    else if(var1.equals("Moderately sloping")||var1.equals("Moderately steeply sloping")){
                        clssify=1;

                    }else{
                        clssify=0;

                    }
                    colorgridarr[i][j]=(clssify<colorgridarr[i][j]?clssify:colorgridarr[i][j]);
                    //System.out.println("Grid Value=====----======>>>"+var1+" "+var2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        Void lulcgridmap(){
            //lulc
            data = "";
            urlink="https://bhuvan-vec2.nrsc.gov.in/bhuvan/wms?request=getFeatureinfo" +
                    "&info_format=application/json&width=100&height=100&x=50&y=50&layers=lulc:KA_LULC50K_1516" +
                    "&query_layers=lulc:KA_LULC50K_1516&bbox="+String.valueOf(ln-diff)+","+String.valueOf(lt-diff)+","+String.valueOf(ln+diff)+","+String.valueOf(lt+diff);
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
                    var1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DESCR_1");
                    var2 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DESCR_2");
                    if (var2.equals("Urban")||var2.equals("Rural"))
                    {
                        clssify=2;
                    }
                    else
                    {
                        clssify=0;
                    }
                    colorgridarr[i][j]=(clssify<colorgridarr[i][j]?clssify:colorgridarr[i][j]);
                    //System.out.println("Grid Value=====----======>>>"+var1+" "+var2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        Void ldeggridmap(){
            //Land Degradation
            data = "";
            urlink="https://bhuvan-vec2.nrsc.gov.in/bhuvan/wms?request=getFeatureinfo" +
                    "&info_format=application/json&width=100&height=100&x=50&y=50&layers=ld:KA_LD50K_1516" +
                    "&query_layers=ld:KA_LD50K_1516&bbox="+String.valueOf(ln-diff)+","+String.valueOf(lt-diff)+","+String.valueOf(ln+diff)+","+String.valueOf(lt+diff);
            System.out.println(urlink);
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
                    var1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DS_1516_L1");
                    var2 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("DS_1516_L2");
                    if (var1.equals("Normal")){
                        clssify=2;
                    }
                    else if(var1.equals("Others")||var1.equals("Anthropogenic"))
                    {
                        clssify=1;
                    }else
                    {
                        clssify=0;
                    }
                    colorgridarr[i][j]=(clssify<colorgridarr[i][j]?clssify:colorgridarr[i][j]);
                    //System.out.println("Grid Value=====----======>>>"+var1+" "+var2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
        Void geomorphgridmap(){
            data = "";
            urlink="https://bhuvan-vec2.nrsc.gov.in/bhuvan/wms?request=getFeatureinfo" +
                    "&info_format=application/json&width=100&height=100&x=50&y=50&layers=geomorphology:KA_GM50K_0506" +
                    "&query_layers=geomorphology:KA_GM50K_0506&bbox="+String.valueOf(ln-diff)+","+String.valueOf(lt-diff)+","+String.valueOf(ln+diff)+","+String.valueOf(lt+diff);
            //System.out.println(urlink);
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
                    var1 = jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").getString("Des");
                    if(var1.equals("Denudational Origin-Low Dissected Lower Plateau"))
                    {
                        clssify=2;
                    }
                    else if(var1.equals("Structural Origin-Low Dissected Hills and Valleys")||var1.equals("Structural Origin-Low Dissected Upper Plateau")
                            ||var1.equals("Structural Origin-Low Dissected Lower Plateau")||var1.equals("Denudational Origin - Low Dissected Hills and Valleys")
                            ||var1.equals("Denudational Origin-Low Dissected Upper Plateau")||var1.equals("Fluvial Origin-Older Alluvial Plain")
                            ||var1.equals("Fluvial Origin-Older Flood Plain")||var1.equals("Coastal Origin-Offshore Island")
                            ||var1.equals("Anthropogenic Origin-Anthropogenic Terrain")||var1.equals("Denudational Origin-Pediment-PediPlain Complex"))
                    {
                        clssify=1;
                    }
                    else{
                        clssify=0;
                    }
                    colorgridarr[i][j]=(clssify<colorgridarr[i][j]?clssify:colorgridarr[i][j]);
                    //System.out.println("Grid Value=====----======>>>"+var1+" "+var2);
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {

            for(int i1=0; i1<10; i1++)
            {
                for(int j1=0; j1<10; j1++)
                {
                    System.out.print(colorgridarr[i1][j1]+" ");
                }
            }
            makegrid(i, j);
            if(i==9 && j==9)
            {
                rg.setEnabled(true);
                disable1.setEnabled(true);
                disable2.setEnabled(true);
                disable3.setEnabled(true);
                disable4.setEnabled(true);
                disable5.setEnabled(true);
            }
            //System.out.println();
            super.onPostExecute(unused);
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
            gridimg.setImageBitmap(bitmap);
        }
    }

    public void suggestions(View view){
        String query=poisearch.getText().toString();
        index=0;
        String queryurl="https://api.tomtom.com/search/2/poiSearch/"+query+".json?key=zL1iZrkaAacjmHWGNotkcW9X2CH4tbBy&lat="+String.valueOf(lat_loc)+"&lon="+String.valueOf(lng_loc);
        Loadnearbyinfoquery squery= new Loadnearbyinfoquery();
        squery.execute(queryurl);
        LatLng mapcords=tomtomMap1.getCenterOfMap();
        //latitude1.setText(mapcords.getLatitudeAsString());
        //longitude1.setText(mapcords.getLongitudeAsString());
        LatLng mapCenter = new LatLng(Double.parseDouble(mapcords.getLatitudeAsString()),Double.parseDouble(mapcords.getLongitudeAsString()));

        FuzzySearchQuery searchQuery = FuzzySearchQueryBuilder.create(poisearch.getText().toString())
                .withPosition(mapCenter)
                .build();
        tomtomSearch1.search(searchQuery, new FuzzySearchResultListener() {
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
                //adapter.notifyDataSetChanged();
                System.out.println(lastSearchResult);
                tomtomMap1.clear();
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
                    //addressAuto.add(addressstring);
                    //adapter.notifyDataSetChanged();
                    MarkerBuilder markerBuilder = new MarkerBuilder(geoposition)
                            .icon(Icon.Factory.fromResources(getBaseContext(),
                                    R.drawable.location_icon))
                            .markerBalloon(new SimpleMarkerBalloon(poi.getName()))
                            .tag(lastSearchResult.get(i).getAddress())
                            .iconAnchor(MarkerAnchor.Bottom)
                            .decal(true);
                    tomtomMap1.addMarker(markerBuilder);
                }
            }

            @Override
            public void onSearchError(SearchError searchError) {
            }
        });


    }

}