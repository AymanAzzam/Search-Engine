package com.example.crawler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CustomListView extends ArrayAdapter<String> {

    private Activity context;
    private List<String> key1List,key2List,key3List;
    int type;

    /*** type = 0 means image search and anything else means normal search ***/
    CustomListView(@NonNull Activity context, List<String>  key1List, List<String>  key2List, List<String>  key3List, int type)
    {
        super(context,(type==0)?R.layout.row_image_search:R.layout.row,key1List);

        this.context = context;     this.key1List = key1List;     this.key2List = key2List;     this.key3List = key3List;
        this.type = type;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if ( view== null ){
            view = context.getLayoutInflater().inflate((type==0)?R.layout.row_image_search:R.layout.row,null);
        }

        if(type == 0)
        {
            ImageView image = (ImageView) view.findViewById(R.id.image);

            /*** Getting the BitMap of Image from the Internet ***/
            Bitmap bm = null;
            try {
                URLConnection conn = new URL(key1List.get(position)).openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e("Error", "Error getting bitmap", e);
            }
            image.setImageBitmap(bm);
        }
        else {
            TextView header = (TextView) view.findViewById(R.id.page_header);
            TextView link = (TextView) view.findViewById(R.id.page_link);
            TextView summary = (TextView) view.findViewById(R.id.page_summary);

            header.setText(key1List.get(position));
            link.setText(key2List.get(position));
            summary.setText(key3List.get(position));
        }
        return view;
    }
}
