package com.example.crawler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView search_view;
    Button btNormalSearch,btImageSearch,btTrends;
    SearchManager search_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /******************* Asking for location permission *********************/
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] arr = new String[2];
            arr[0] = Manifest.permission.ACCESS_COARSE_LOCATION;    arr[1] = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this,arr,0);
            System.out.println("Asking for Permissions");
        }

        search_manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search_view = (SearchView)(findViewById(R.id.search_phrase));
        btNormalSearch = (Button) findViewById(R.id.crawler_search);
        btImageSearch = (Button) findViewById(R.id.image_search);
        btTrends = (Button) findViewById(R.id.trend_button);

        /******************* Pressing Trends Button *********************/
        btTrends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TrendsActivity.class);
                startActivity(intent);
            }
        });

        /************* Pressing Normal Search Button Action *************/
        btNormalSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { searchAction(search_view.getQuery().toString(),1); }
        });

        /************* Pressing Image Search Button Action *************/
        btImageSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { searchAction(search_view.getQuery().toString(),0); }
        });

        /*** set the searchable configuration ***/
        search_view.setSearchableInfo(search_manager.getSearchableInfo(getComponentName()));

        search_view.setOnQueryTextListener(this);
        search_view.setQueryRefinementEnabled(true);    //To Enable Search Suggestions
        search_view.setSubmitButtonEnabled(true);       //Add a "submit" button

    }

    /************* Pressing Search Action *************/
    @Override
    public boolean onQueryTextSubmit(String query) {
        //searchAction(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    void searchAction(String query, int type)
    {
        Intent intent = new Intent(MainActivity.this,SearchableActivity.class);
        if(query.length() == 0)    return; /*** Do Nothing if Empty String ***/

        if(type == 0)    intent.putExtra("type","Image");
        else                        intent.putExtra("type","Normal");

        /*** Checking GPS is Enabled or not ***/
        LocationManager manager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            System.out.println("GPS isn't Enabled");
            new AlertDialog.Builder(MainActivity.this).setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", null).show();
            return;
        }

        /*** start the Searchable Activity ***/
        intent.putExtra("query", query);
        startActivity(intent);
    }
}
