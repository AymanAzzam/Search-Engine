package com.example.crawler;


import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {

    ListView list_view;
    ImageView image_left,image_right;
    TextView first_number,second_number,third_number,fourth_number,fifth_number;
    ArrayList<String> key1List,key2List,key3List;
    int page_number,start_index,end_index,type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        /*** Getting the Parameters from the Previous Activity ***/
        QueryRequest queryRequest = (QueryRequest) getIntent().getExtras().getParcelable("QUERY");
        page_number = Integer.parseInt(getIntent().getStringExtra("EXTRA_PAGE_NUMBER"));
        if(getIntent().getStringExtra("QUERY_TYPE").equals("0"))    type = 0;
        else    type = 1;

        key1List = queryRequest.getKey1();
        key2List = queryRequest.getKey2();
        key3List = queryRequest.getKey3();

        list_view = (ListView) findViewById(R.id.list_view);
        image_left = (ImageView) findViewById(R.id.arrow_left);
        image_right = (ImageView) findViewById(R.id.arrow_right);
        first_number = (TextView) findViewById(R.id.first_number);
        second_number = (TextView) findViewById(R.id.second_number);
        third_number = (TextView) findViewById(R.id.third_number);
        fourth_number = (TextView) findViewById(R.id.fourth_number);
        fifth_number = (TextView) findViewById(R.id.fifth_number);

        /************* Update ListView Indices and Page Number *************/

        start_index = page_number*10;
        end_index = Math.min(start_index + 10,key1List.size());

        /************* Update Arrows *************/
        if(start_index == 0)             image_left.setVisibility(View.GONE);
        else                            image_left.setVisibility(View.VISIBLE);
        if(end_index == key1List.size())  image_right.setVisibility(View.GONE);
        else                            image_right.setVisibility(View.VISIBLE);

        /************* Update Numbers *************/
        if(key1List.size() <= 40)
        {
            first_number.setVisibility(View.GONE);  second_number.setVisibility(View.GONE);    third_number.setVisibility(View.GONE);
            fourth_number.setVisibility(View.GONE); fifth_number.setVisibility(View.GONE);
        }
        else
        {
            // to let the page in the middle of the five numbers
            int first = (int) Math.max(3,Math.min(page_number + 1,Math.ceil(key1List.size()/10.0) - 2));
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
        CustomListView adapter = new CustomListView(this,key1List.subList(start_index,end_index),
                key2List.subList(start_index,end_index),key3List.subList(start_index,end_index),type);
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
                if(getIntent().getStringExtra("ACTIVATE_LINK").equals("0"))
                    return;
                String url = key2List.get(page_number*10+position);
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String urlBackEnd = "http://ec2-54-224-132-31.compute-1.amazonaws.com:8080/IncrementFrequency?Query=" + url + "&Type=" + type;

                StringRequest request = new StringRequest(Request.Method.GET, urlBackEnd,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {}
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {}
                });
                queue.add(request);
                startActivity(browserIntent);
            }
        });

        String locale = this.getResources().getConfiguration().locale.getCountry();
        System.out.println(locale);
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
