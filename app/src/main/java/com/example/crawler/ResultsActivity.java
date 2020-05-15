package com.example.crawler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    ListView listView;
    ImageView imageLeft,imageRight;
    TextView firstNumber,secondNumber,thirdNumber;
    ArrayList<String> headers,links,summaries;
    int pageNumber,startIndex,endIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        headers = new ArrayList<String>();
        links = new ArrayList<String>();
        summaries = new ArrayList<String>();

        listView = (ListView) findViewById(R.id.list_view);
        imageLeft = (ImageView) findViewById(R.id.arrow_left);
        imageRight = (ImageView) findViewById(R.id.arrow_right);
        firstNumber = (TextView) findViewById(R.id.first_number);
        secondNumber = (TextView) findViewById(R.id.second_number);
        thirdNumber = (TextView) findViewById(R.id.third_number);

        /************* Input For Testing *************/
        headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");
        headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");

        links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");
        links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");

        summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");
        summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");

        headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");
        headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");

        links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");
        links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");

        summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");
        summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");

        headers.add("Computer Architecture");
        links.add("htttp://en.wikipedia.org");
        summaries.add("In computer Engineering. Computer Architecture is a set of rules and methods that describes the functionality");

        /************* Update ListView Indices and Page Number *************/
        pageNumber = Integer.parseInt(getIntent().getStringExtra("EXTRA_PAGE_NUMBER"));
        //System.out.println(pageNumber);
        startIndex = pageNumber*10;
        endIndex = Math.min(startIndex + 10,headers.size());

        /************* Update Arrows *************/
        if(pageNumber == 0)             imageLeft.setVisibility(View.GONE);
        else                            imageLeft.setVisibility(View.VISIBLE);
        if(endIndex == headers.size())  imageRight.setVisibility(View.GONE);
        else                            imageRight.setVisibility(View.VISIBLE);

        /************* Update Numbers *************/
        if(headers.size() <= 20)
        {   firstNumber.setVisibility(View.GONE);  secondNumber.setVisibility(View.GONE);    thirdNumber.setVisibility(View.GONE);  }
        else
        {
            int first = Math.min(pageNumber + 1,headers.size()/10 - 1);
            firstNumber.setVisibility(View.VISIBLE);    firstNumber.setText(Integer.toString(first));
            secondNumber.setVisibility(View.VISIBLE);   secondNumber.setText(Integer.toString(first + 1));
            thirdNumber.setVisibility(View.VISIBLE);   thirdNumber.setText(Integer.toString(first + 2));

            if(first == pageNumber + 1)     firstNumber.setTextColor(getResources().getColor(R.color.colorBlack));
            else if(first == pageNumber)    secondNumber.setTextColor(getResources().getColor(R.color.colorBlack));
            else                            thirdNumber.setTextColor(getResources().getColor(R.color.colorBlack));
        }

        /************* Update ListView *************/
        CustomListView adapter = new CustomListView(this,headers.subList(startIndex,endIndex),links.subList(startIndex,endIndex),summaries.subList(startIndex,endIndex));
        listView.setAdapter(adapter);

        /************* Right Arrow Action *************/
        imageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber = pageNumber + 1;
                //System.out.println(pageNumber);
                finish();
                overridePendingTransition(0, 0);
                getIntent().putExtra("EXTRA_PAGE_NUMBER",Integer.toString(pageNumber));
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        /************* Left Arrow Action *************/
        imageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber = pageNumber - 1;
                finish();
                overridePendingTransition(0, 0);
                getIntent().putExtra("EXTRA_PAGE_NUMBER",Integer.toString(pageNumber));
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
    }
}
