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
        /*
        System.out.println(editText.getText());
        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stem(editText.getText().toString());
        System.out.println("The stem of " + editText.getText().toString() + " is " + stem);

        String USER_AGENT = "Mozilla/5.0";

        String POST_URL = "http://text-processing.com/api/stem/";

        String POST_PARAMS = "text=processing";

        URL obj = null;
        HttpURLConnection urlConnection = null;
        try { obj = new URL(POST_URL);} catch (MalformedURLException e) { e.printStackTrace();}
        try { urlConnection = (HttpURLConnection) obj.openConnection();} catch (IOException e) {e.printStackTrace();}
        try {urlConnection.setRequestMethod("POST");} catch (ProtocolException e) {e.printStackTrace();}
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);


        // For POST only - START
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        OutputStream os = null;
        int responseCode = 0;
        try
        {

            os = new BufferedOutputStream(urlConnection.getOutputStream());

            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();

            responseCode = urlConnection.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK)  //success
            {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {response.append(inputLine);}
                in.close();

                // print result
                System.out.println(response.toString());
            }
            else System.out.println("POST request not worked");

        } catch (IOException e) {e.printStackTrace();}
        // For POST only - END
*/

        String url = "http://text-processing.com/api/stem/";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("text", "processing");

        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {}

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {}

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}

            @Override
            public void onRetry(int retryNo) {}

            @Override
            public void onFinish() {
                // Completed the request (either success or failure)
                System.out.println("Done Request");
            }});



    }
}
