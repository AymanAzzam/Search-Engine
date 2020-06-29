package com.example.crawler;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*** Handling inputs without using the push button ***/
public class SearchableActivity extends AppCompatActivity {

    String query;
    String type;

    @Override
    protected void onStart() {
        super.onStart();

        /*** Getting the query search and the type search ***/
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction()))   query = getIntent().getStringExtra(SearchManager.QUERY);
        else                                                        query = getIntent().getStringExtra("query");
        type = getIntent().getStringExtra("type");
        if(type ==null) type = "Normal";

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
        String url = "http://ec2-54-224-132-31.compute-1.amazonaws.com:8080/GetResult?Query=" + query.replace(" ","+") + "&Type=" + type;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray serverResponse) {
                        ArrayList<String> key1List = new ArrayList<String>();
                        ArrayList<String> key2List = new ArrayList<String>();
                        ArrayList<String> key3List = new ArrayList<String>();
                        JSONObject jObject; JSONArray jArray;
                        String activateLink,key1,key2,key3;

                        /*** Handling the Json Object Keys ***/
                        if(type.equals("Image")) {  key1 = "imageURL"; key2 = "websiteURL"; key3="0";}
                        else { key1 = "headerText";    key2 = "websiteName";   key3 = "summary"; }

                        /*** Extracting the Results from the Json Array ***/
                        try {
                            for (int i = 0; i < serverResponse.length(); i++) {
                                jObject = (JSONObject) serverResponse.get(i);

                                /*** Handling the Image Query. Key1 is the IMAGE_URL and Key2 is the IMAGE_WEBSITE_URL ***/
                                if(key3.equals("0")) {
                                    //System.out.println("Image Search");
                                    jArray = jObject.optJSONArray(key1);
                                    for(int j = 0; j < jArray.length();j++)
                                    {
                                        if(((String)jArray.get(j)).isEmpty())   continue;

                                        key1List.add((String)jArray.get(j));
                                        key2List.add(jObject.optString(key2));
                                        key3List.add("dummy_data");
                                    }
                                }
                                /*** Handling the Normal Query. Key1 is the title, Key2 is the WEBSITE_URL and Key3 is the summary ***/
                                else {
                                    //System.out.println("Normal Search");
                                    key1List.add(jObject.optString(key1));
                                    key2List.add(jObject.optString(key2));
                                    key3List.add(jObject.optString(key3));
                                }

                            }
                        }
                        catch (JSONException e) {   e.printStackTrace();    }

                        /*** Handling when the query doesn't match any document ***/
                        if(key1List.size() == 0) {
                            key1List.add(query+"");    activateLink = "0";
                            key2List.add("Your search did not match any documents\n");
                            key3List.add("Suggestions:\n\nMake sure that all words are spelled correctly.\n\nTry different keywords.\n\nTry more general keywords.\n\nTry fewer keywords.");
                        }
                        else activateLink = "1";

                        /*** start the Results Activity ***/
                        Intent intent = new Intent(SearchableActivity.this,ResultsActivity.class);
                        intent.putExtra("EXTRA_PAGE_NUMBER", "0");
                        intent.putExtra("ACTIVATE_LINK", activateLink);
                        intent.putExtra("QUERY_TYPE", key3);
                        intent.putExtra("QUERY", new QueryRequest(key1List,key2List,key3List));
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
