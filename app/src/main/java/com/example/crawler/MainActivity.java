package com.example.crawler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import cz.msebera.android.httpclient.Header;
import opennlp.tools.stemmer.PorterStemmer;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)(findViewById(R.id.search_phrase));
        button = (Button) findViewById(R.id.crawler_search);

        /************* Search-Button Action *************/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ResultsActivity.class);
                i.putExtra("EXTRA_PAGE_NUMBER", "0");
                startActivity(i);
            }
        });
    }

    @Override
    protected void onPause ()  {
        super.onPause();

        /************* Test Steaming *************/
        System.out.println(editText.getText());
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stem(editText.getText().toString());
        System.out.println("The stem of " + editText.getText().toString() + " is " + stem);

    }
}
