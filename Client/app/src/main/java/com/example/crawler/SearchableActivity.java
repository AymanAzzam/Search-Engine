package com.example.crawler;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.SearchRecentSuggestions;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/*** Handling inputs without using the push button ***/
public class SearchableActivity extends AppCompatActivity {

    String query;

    @Override
    protected void onStart() {
        super.onStart();

        /*** Getting the query search ***/
        query = getIntent().getStringExtra(SearchManager.QUERY);

        /*** Save the Query for Suggestion ***/
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        suggestions.saveRecentQuery(query, null);

        /*** Checking the internet Connection before send GET Request ***/
        Toast errorToast = Toast.makeText(SearchableActivity.this, "No internet Connection !", Toast.LENGTH_SHORT);
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != NetworkInfo.State.CONNECTED &&
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != NetworkInfo.State.CONNECTED) {
            errorToast.show();
            finish();
            return;
        }

        /*** Send GET Request ***/
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.google.ru/?q=" + query;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("Response is: "+ response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast errorToast = Toast.makeText(SearchableActivity.this, "Failed to send Get Request with url = "+url, Toast.LENGTH_SHORT);
                errorToast.show();
            }
        });
        queue.add(stringRequest);

        /*** start the Results Activity ***/
        Intent i = new Intent(SearchableActivity.this,ResultsActivity.class);
        i.putExtra("EXTRA_PAGE_NUMBER", "0");
        startActivity(i);
        finish();
    }
}
