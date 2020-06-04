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
    private ArrayList<String> key1, key2, key3;

    public QueryRequest(ArrayList<String> key1, ArrayList<String> key2, ArrayList<String> key3)
    {   this.key3 = key3;   this.key1 = key1;   this.key2 = key2;   }

    public ArrayList<String> getKey1() { return key1;    }
    public ArrayList<String> getKey2() { return key2;    }
    public ArrayList<String> getKey3() { return key3;    }

    public QueryRequest(Parcel in){
        this.key1 = new ArrayList<String>();
        this.key2 = new ArrayList<String>();
        this.key3 = new ArrayList<String>();
        in.readList( this.key1, String.class.getClassLoader());
        in.readList( this.key2, String.class.getClassLoader());
        in.readList( this.key3, String.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList( this.key1);
        dest.writeList( this.key2);
        dest.writeList( this.key3);
    }
}
