package com.example.crawler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

public class TrendsActivity extends AppCompatActivity {

    private Spinner dropdown;
    private Button btTrends;
    private ListView listView;
    private TrendsListView trendsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trends);

        dropdown = findViewById(R.id.spinner);
        btTrends = findViewById(R.id.trend_button);
        listView = findViewById(R.id.list_view);

        /************* Update ListView *************/
        ArrayList<String> trends = new ArrayList<String>();
        trendsAdapter = new TrendsListView(this,trends);
        listView.setAdapter(trendsAdapter);

        RequestQueue queue = Volley.newRequestQueue(this);

        /******************* List all Countries *********************/
        SortedSet<String> countries = new TreeSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (!TextUtils.isEmpty(locale.getDisplayCountry())) { countries.add(locale.getDisplayCountry()); } }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, countries.toArray(new String[0]));
        dropdown.setAdapter(adapter);

        /******************* Pressing Trends Button *********************/
        btTrends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*** Checking the internet Connection before send GET Request ***/
                Toast errorToast = Toast.makeText(TrendsActivity.this, "No internet Connection !", Toast.LENGTH_SHORT);
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
                    errorToast.show();
                    finish();
                    return;
                }
                String url = "http://ec2-54-224-132-31.compute-1.amazonaws.com:8080/GetTrends?Country=" + dropdown.getSelectedItem() ;
                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray serverResponse) {
                                trends.clear();
                                try {
                                    for (int i = 0; i < serverResponse.length(); i++)
                                        trends.add((String)serverResponse.get(i));
                                    trendsAdapter.notifyDataSetChanged();
                                } catch (JSONException e) { e.printStackTrace(); }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast errorToast = Toast.makeText(TrendsActivity.this, "Server Failed to Response with URL = "+url, Toast.LENGTH_SHORT);
                        errorToast.show();
                        finish();
                    }
                });
                queue.add(jsonArrayRequest);
            }
        });
    }
}
