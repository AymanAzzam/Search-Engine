package com.example.crawler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)(findViewById(R.id.search_phrase));
    }

    @Override
    protected void onPause ()  {
        super.onPause();

        System.out.println(editText.getText());
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stem(editText.getText().toString());
        System.out.println("The stem of " + editText.getText().toString() + " is " + stem);

    }
}
