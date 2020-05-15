package com.example.crawler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    ListView list_view;
    ImageView image_left,image_right;
    TextView first_number,second_number,third_number,fourth_number,fifth_number;
    ArrayList<String> headers,links,summaries;
    int page_number,start_index,end_index;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        headers = new ArrayList<String>();
        links = new ArrayList<String>();
        summaries = new ArrayList<String>();

        list_view = (ListView) findViewById(R.id.list_view);
        image_left = (ImageView) findViewById(R.id.arrow_left);
        image_right = (ImageView) findViewById(R.id.arrow_right);
        first_number = (TextView) findViewById(R.id.first_number);
        second_number = (TextView) findViewById(R.id.second_number);
        third_number = (TextView) findViewById(R.id.third_number);
        fourth_number = (TextView) findViewById(R.id.fourth_number);
        fifth_number = (TextView) findViewById(R.id.fifth_number);

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

        headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");headers.add("1");
        headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");headers.add("2");

        links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");links.add("3");
        links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");links.add("4");

        summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");summaries.add("5");
        summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");summaries.add("6");

        headers.add("Computer Architecture");
        links.add("http://en.wikipedia.org");
        summaries.add("In computer Engineering. Computer Architecture is a set of rules and methods that describes the functionality");

        /************* Update ListView Indices and Page Number *************/
        page_number = Integer.parseInt(getIntent().getStringExtra("EXTRA_PAGE_NUMBER"));
        //System.out.println(pageNumber);
        start_index = page_number*10;
        end_index = Math.min(start_index + 10,headers.size());

        /************* Update Arrows *************/
        if(start_index == 0)             image_left.setVisibility(View.GONE);
        else                            image_left.setVisibility(View.VISIBLE);
        if(end_index == headers.size())  image_right.setVisibility(View.GONE);
        else                            image_right.setVisibility(View.VISIBLE);

        /************* Update Numbers *************/
        if(headers.size() <= 40)
        {
            first_number.setVisibility(View.GONE);  second_number.setVisibility(View.GONE);    third_number.setVisibility(View.GONE);
            fourth_number.setVisibility(View.GONE); fifth_number.setVisibility(View.GONE);
        }
        else
        {
            // to let the page in the middle of the five numbers
            int first = Math.max(3,Math.min(page_number + 1,headers.size()/10 - 1));
            first_number.setVisibility(View.VISIBLE);    first_number.setText(Integer.toString(first - 2));
            second_number.setVisibility(View.VISIBLE);   second_number.setText(Integer.toString(first - 1));
            third_number.setVisibility(View.VISIBLE);    third_number.setText(Integer.toString(first));
            fourth_number.setVisibility(View.VISIBLE);   fourth_number.setText(Integer.toString(first + 1));
            fifth_number.setVisibility(View.VISIBLE);    fifth_number.setText(Integer.toString(first + 2));

            if(first - 3 == page_number)            first_number.setTextColor(getResources().getColor(R.color.colorBlack));
            else if(first - 2 == page_number)       second_number.setTextColor(getResources().getColor(R.color.colorBlack));
            else if (first - 1 == page_number)      third_number.setTextColor(getResources().getColor(R.color.colorBlack));
            else if (first  == page_number)         fourth_number.setTextColor(getResources().getColor(R.color.colorBlack));
            else                                    fifth_number.setTextColor(getResources().getColor(R.color.colorBlack));
        }

        /************* Update ListView *************/
        CustomListView adapter = new CustomListView(this,headers.subList(start_index,end_index),links.subList(start_index,end_index),summaries.subList(start_index,end_index));
        list_view.setAdapter(adapter);

        /************* Pressing Right Arrow Action *************/
        image_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(page_number + 1);}
        });

        /************* Pressing Left Arrow Action *************/
        image_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(page_number - 1);   }
        });

        /************* Pressing first-number Action *************/
        first_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(Integer.parseInt(first_number.getText().toString()) - 1); }
        });

        /************* Pressing second-number Action *************/
        second_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(Integer.parseInt(second_number.getText().toString()) - 1); }
        });

        /************* Pressing third-number Action *************/
        third_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(Integer.parseInt(third_number.getText().toString()) - 1); }
        });

        /************* Pressing fourth-number Action *************/
        fourth_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {newPage(Integer.parseInt(fourth_number.getText().toString()) - 1);}
        });

        /************* Pressing fifth-number Action *************/
        fifth_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { newPage(Integer.parseInt(fifth_number.getText().toString()) - 1); }
        });

        /************* Pressing Item Action *************/
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String url = links.get(page_number*10+position);
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

    void newPage(int newPageNumber)
    {
        page_number =  newPageNumber;
        finish();
        overridePendingTransition(0, 0);
        getIntent().putExtra("EXTRA_PAGE_NUMBER",Integer.toString(page_number));
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }
}
