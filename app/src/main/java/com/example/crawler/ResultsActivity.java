package com.example.crawler;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    ListView listview;
    ArrayList<String> headers;
    ArrayList<String> links;
    ArrayList<String> summaries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        headers = new ArrayList<String>();
        links = new ArrayList<String>();
        summaries = new ArrayList<String>();

        //headers.add("Computer Architecture");
        //links.add("htttp://en.wikipedia.org");
        //summary.add("In computer Engineering. Computer Architecture is a set of rules and methods that describes the functionality");

        headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");
        headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");

        links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");
        links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");

        summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");
        summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");

        listview = (ListView) findViewById(R.id.list_view);

        CustomListView adapter = new CustomListView(this,headers,links,summaries);

        listview.setAdapter(adapter);
    }
}
