package com.example.crawler;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomListView extends ArrayAdapter<String> {
    @NonNull

    Activity context;
    ArrayList<String> headers;
    ArrayList<String> links;
    ArrayList<String> summaries;

    CustomListView(@NonNull Activity context, ArrayList<String>  headers, ArrayList<String>  links, ArrayList<String>  summary)
    {
        super(context,R.layout.row,headers);
        this.context = context;     this.headers = headers;     this.links = links;     this.summaries = summary;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if ( view== null ){
            view = context.getLayoutInflater().inflate(R.layout.row,null);
        }

        TextView header = (TextView) view.findViewById(R.id.page_header);
        TextView link = (TextView) view.findViewById(R.id.page_link);
        TextView summary = (TextView) view.findViewById(R.id.page_summary);

        header.setText(headers.get(position));
        link.setText(links.get(position));
        summary.setText(summaries.get(position));

        return view;
    }
}
