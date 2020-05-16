package com.example.crawler;

import android.app.SearchManager;
import android.content.Intent;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

public class SearchableActivity extends AppCompatActivity {

    SearchView search_view;

    @Override
    protected void onStart() {
        super.onStart();

        search_view = (SearchView)(findViewById(R.id.search_phrase));

        /*** Handling the Voice Input ***/ /*
        Intent intent = getIntent();
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        search_view.setQuery(query,false);
        System.out.println(query);*/
        finish();
    }
}
