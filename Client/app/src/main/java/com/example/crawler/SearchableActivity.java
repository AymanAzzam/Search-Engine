package com.example.crawler;

import android.app.SearchManager;
import android.content.Intent;
import android.provider.SearchRecentSuggestions;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

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

        /*** start the Results Activity ***/
        Intent i = new Intent(SearchableActivity.this,ResultsActivity.class);
        i.putExtra("EXTRA_PAGE_NUMBER", "0");
        startActivity(i);
        finish();
    }
}
