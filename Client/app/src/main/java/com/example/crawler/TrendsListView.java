package com.example.crawler;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class TrendsListView extends ArrayAdapter<String> {

    private Activity context;
    private List<String> trendsList;

    public TrendsListView(@NonNull Activity context, List<String> trends) {
        super(context, R.layout.trend_row,trends);
        this.trendsList = trends;   this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = convertView;

        if ( view== null ){
            view = context.getLayoutInflater().inflate(R.layout.trend_row,null);
        }

        TextView trendName = (TextView) view.findViewById(R.id.trend_name);
        TextView trendNumber = (TextView) view.findViewById(R.id.trend_number);

        trendName.setText(trendsList.get(position));
        trendNumber.setText(Integer.toString(position + 1));

        return view;
    }

}
