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
    Button button;
    SearchManager search_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search_view = (SearchView)(findViewById(R.id.search_phrase));
        button = (Button) findViewById(R.id.crawler_search);

        /************* Pressing Search-Button Action *************/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { searchAction(search_view.getQuery().toString()); }
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

    void searchAction(String query)
    {
        if(query.length() == 0)    return; /*** Do Nothing if Empty String ***/

        /*** start the Searchable Activity ***/
        Intent i = new Intent(MainActivity.this,SearchableActivity.class);
        i.putExtra("query", query);
        startActivity(i);
    }
}
