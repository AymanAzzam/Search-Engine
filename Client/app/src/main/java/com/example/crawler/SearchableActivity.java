package com.example.crawler;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*** Handling inputs without using the push button ***/
public class SearchableActivity extends AppCompatActivity {

    String query;

    @Override
    protected void onStart() {
        super.onStart();

        /*** Getting the query search ***/
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))   query = getIntent().getStringExtra(SearchManager.QUERY);
        else                                                        query = getIntent().getStringExtra("query");

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
        String url = "http://ec2-54-90-197-233.compute-1.amazonaws.com:8080/GetResult?Query=" + query.replace(" ","+");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray serverResponse) {
                        ArrayList<String> summary = new ArrayList<String>();
                        ArrayList<String> title = new ArrayList<String>();
                        ArrayList<String> websites = new ArrayList<String>();
                        JSONObject jObject;
                        String activateLink;

                        /*** Extracting the Results from the Json Array ***/
                        try {
                            for (int i = 0; i < serverResponse.length(); i++) {
                                jObject = (JSONObject) serverResponse.get(i);
                                summary.add(jObject.optString("summary"));
                                websites.add(jObject.optString("websiteName"));
                                title.add(jObject.optString("headerText"));
                            }
                        }
                        catch (JSONException e) {   e.printStackTrace();    }

                        /*** Handling when the query doesn't match any document ***/
                        if(title.size() == 0) {
                            title.add(query+"");    activateLink = "0";
                            websites.add("Your search did not match any documents\n");
                            summary.add("Suggestions:\n\nMake sure that all words are spelled correctly.\n\nTry different keywords.\n\nTry more general keywords.\n\nTry fewer keywords.");
                        }
                        else activateLink = "1";

                        /*** start the Results Activity ***/
                        Intent intent = new Intent(SearchableActivity.this,ResultsActivity.class);
                        intent.putExtra("EXTRA_PAGE_NUMBER", "0");
                        intent.putExtra("Activate_Link", activateLink);
                        intent.putExtra("queryRequest", new QueryRequest(title,websites,summary));
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast errorToast = Toast.makeText(SearchableActivity.this, "Server Failed to Response with URL = "+url, Toast.LENGTH_SHORT);
                errorToast.show();
                finish();
            }
        });
        queue.add(jsonArrayRequest);
    }
}
