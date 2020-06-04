package com.example.crawler;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class QueryRequest implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public QueryRequest createFromParcel(Parcel in) {
            return new QueryRequest(in);
        }

        public QueryRequest[] newArray(int size) {
            return new QueryRequest[size];
        }
    };
    private ArrayList<String> summary;
    private ArrayList<String> title;
    private ArrayList<String> websites;

    public QueryRequest(ArrayList<String> title, ArrayList<String> websites, ArrayList<String> summary)
    {
        this.summary = summary;
        this.title = title;
        this.websites = websites;
    }

    public ArrayList<String> getSummaries() { return summary;    }
    public ArrayList<String> getTitles() { return title;    }
    public ArrayList<String> getWebsites() { return websites;    }

    public QueryRequest(Parcel in){
        this.title = new ArrayList<String>();
        this.websites = new ArrayList<String>();
        this.summary = new ArrayList<String>();
        in.readList( this.title, String.class.getClassLoader());
        in.readList( this.websites, String.class.getClassLoader());
        in.readList( this.summary, String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList( this.title);
        dest.writeList( this.websites);
        dest.writeList( this.summary);
    }
}
