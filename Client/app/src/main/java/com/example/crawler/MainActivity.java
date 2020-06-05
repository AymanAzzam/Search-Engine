package com.example.crawler;

import androidx.appcompat.app.AppCompatActivity;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView search_view;
    Button btNormalSearch,btImageSearch;
    SearchManager search_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search_view = (SearchView)(findViewById(R.id.search_phrase));
        btNormalSearch = (Button) findViewById(R.id.crawler_search);
        btImageSearch = (Button) findViewById(R.id.image_search);

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

        /*** start the Searchable Activity ***/
        intent.putExtra("query", query);
        startActivity(intent);
    }
}
