package com.example.crawler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import opennlp.tools.stemmer.PorterStemmer;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    SearchView search_view;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_view = (SearchView)(findViewById(R.id.search_phrase));
        button = (Button) findViewById(R.id.crawler_search);

        /************* Pressing Search-Button Action *************/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { searchAction(search_view.getQuery().toString()); }
        });

        search_view.setOnQueryTextListener(this);
        search_view.setQueryRefinementEnabled(true);    //To Enable Search Suggestions
        search_view.setSubmitButtonEnabled(true);       //Add a "submit" button
    }

    @Override
    protected void onPause ()  {
        super.onPause();

        /************* Test Steaming *************/
        System.out.println(search_view.getQuery());
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stem(search_view.getQuery().toString());
        System.out.println("The stem of " + search_view.getQuery().toString() + " is " + stem);

    }

    /************* Pressing Search Action *************/
    @Override
    public boolean onQueryTextSubmit(String query) {
        searchAction(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    void searchAction(String query)
    {
        if(query.length() == 0)    return; /*** Do Nothing if Empty String ***/
        Intent i = new Intent(MainActivity.this,ResultsActivity.class);
        i.putExtra("EXTRA_PAGE_NUMBER", "0");
        startActivity(i);
    }
}
