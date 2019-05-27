package com.example.android.quakereport;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {
    private static final String LOG_TAG=EarthquakeAdapter.class.getName();

    public EarthquakeAdapter(Context context, List<Earthquake> earthquakes){
        super(context,0,earthquakes);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if(listItemView==null){
            listItemView= LayoutInflater.from(getContext()).inflate(
                    R.layout.earthquake_list_item,parent,false);
        }
        Earthquake earthquake=getItem(position);
        TextView mag=listItemView.findViewById(R.id.mag);
        TextView locationOffset=listItemView.findViewById(R.id.location_offset);
        TextView location=listItemView.findViewById(R.id.location);
        TextView date=listItemView.findViewById(R.id.date);
        TextView time=listItemView.findViewById(R.id.time);

        mag.setText(formatMag(earthquake.getmMag()));

        // Get the appropriate background color based on the current earthquake magnitude
        GradientDrawable magCircle=(GradientDrawable) mag.getBackground();
        magCircle.setColor(getMagnitudeColor(earthquake.getmMag()));

        location.setText(earthquake.getmLocation());
        long timeInmilliseconds=earthquake.getmTimeInMilliseconds();
        date.setText(formatDate(timeInmilliseconds));
        time.setText(formatTime(timeInmilliseconds));

        String[] locationSplit=splitLocation(earthquake.getmLocation());
        if(locationSplit.length==2) {
            locationOffset.setText(locationSplit[0]+"of");
            location.setText(locationSplit[1]);
        }
        else{
            locationOffset.setText(locationSplit[0]);
            location.setText(earthquake.getmLocation());
        }


        return listItemView;
    }
    private String formatDate(long timeInMilliseconds){
        Date date=new Date(timeInMilliseconds);
        SimpleDateFormat simpleDateFormatter=new SimpleDateFormat("MMM dd, yyyy");
        String formattedDate=simpleDateFormatter.format(date).toString();
        Log.i(LOG_TAG,"TEST : In formatDate, string= "+formattedDate);
        return  formattedDate;

    }
    private String formatTime(long timeInMilliseconds){
        Date date=new Date(timeInMilliseconds);
        SimpleDateFormat simpleDateFormatter=new SimpleDateFormat("h:mm a");
        String formattedTime=simpleDateFormatter.format(date);
        return formattedTime;
    }

    private String[] splitLocation(String location){
        if(location.contains(" of "))
            return location.split("of ");
        return new String[]{"Near of"};
    }
    private String formatMag(Double mag){
        DecimalFormat decimalFormatter=new DecimalFormat("0.0");
        return decimalFormatter.format(mag);
    }
    private int getMagnitudeColor(Double mag){

        int magnitudeColorResourceId;
        int magnitudeFloor = (int) Math.floor(mag);
        switch (magnitudeFloor) {
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;
                break;
        }
        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
    }
}
