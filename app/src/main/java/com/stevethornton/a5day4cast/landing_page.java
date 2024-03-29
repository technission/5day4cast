package com.stevethornton.a5day4cast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class landing_page extends AppCompatActivity {
    String dateIfRounded = " ";
    location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);
        TextView txt_Currently = findViewById(R.id.txt_Current);
        TextView txt_Degrees = findViewById(R.id.tempCorF);
        TextView txt_Description = findViewById(R.id.text_Description);
        TextView txt_Temp = findViewById(R.id.txt_Temp);
        ImageView img_Weather = findViewById(R.id.img_Weather);
        TextView txt_Condition = findViewById(R.id.txt_Condition);
        TextView txt_Date = findViewById(R.id.txt_Date);
        EditText edt_Location = findViewById(R.id.txt_Location);
        edt_Location.setInputType(InputType.TYPE_CLASS_NUMBER);
        edt_Location.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        Button btn_GO = findViewById(R.id.btn_Go);
        myLocation = new location();


        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get the zipcode from user
                String zipcode = edt_Location.getText().toString();
                System.out.println("zipcode entered: " +zipcode);
                getCity(zipcode);
                //delay setting the city and state in the text view
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String cityState = myLocation.getCity() + ", " +myLocation.getState();
                        txt_Currently.setText(cityState);
                    }
                }, 1000);


                //connect to the api
                String key = getResources().getString(R.string.API_KEY);
                String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"+zipcode +"?unitGroup=us&key=" +key +"&contentType=json";





                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onResponse(JSONObject response) {

                                Log.v("WEATHER", "Response: " + response.toString());
                                try {

                                    //this gives us an array of objects for 15 days weather
                                    JSONArray daysArray = response.getJSONArray("days");


                                    //create an object for each day
                                    JSONObject day1Object = daysArray.getJSONObject(0); //this pulls data for the first day
                                    JSONObject day2Object = daysArray.getJSONObject(1); //this pulls data for the second day
                                    JSONObject day3Object = daysArray.getJSONObject(2); //this pulls data for the third day
                                    JSONObject day4Object = daysArray.getJSONObject(3); //this pulls data for the fourth day
                                    JSONObject day5Object = daysArray.getJSONObject(4); //this pulls data for the fifth day


                                    //each days date needs to be set for comparison to api date
                                    //date format is yyyy-MM-dd
                                    String day1Date = day1Object.getString("datetime");
                                    String day2Date = day2Object.getString("datetime");
                                    String day3Date = day3Object.getString("datetime");
                                    String day4Date = day4Object.getString("datetime");
                                    String day5Date = day5Object.getString("datetime");


                                    //each days hours need to be set using an array
                                    JSONArray hoursDay1Array = day1Object.getJSONArray("hours");
                                    System.out.println("hours day 1 array "+hoursDay1Array);
                                    JSONArray hoursDay2Array = day2Object.getJSONArray("hours");
                                    JSONArray hoursDay3Array = day3Object.getJSONArray("hours");
                                    JSONArray hoursDay4Array = day4Object.getJSONArray("hours");
                                    JSONArray hoursDay5Array = day5Object.getJSONArray("hours");

                                    //need to pull just the hours out of the array this will be the final time we touch the hours object
                                    JSONObject hoursDay1Object = hoursDay1Array.getJSONObject(0);
                                    JSONObject hoursDay2Object = hoursDay2Array.getJSONObject(0);
                                    JSONObject hoursDay3Object = hoursDay3Array.getJSONObject(0);
                                    JSONObject hoursDay4Object = hoursDay4Array.getJSONObject(0);
                                    JSONObject hoursDay5Object = hoursDay5Array.getJSONObject(0);

                                    //list out the hours in a day from the api...
                                    List<String> hourList = new ArrayList<>();
                                    for (int i = 0; i < hoursDay1Array.length(); i++) {
                                        JSONObject hoursInDay = hoursDay1Array.getJSONObject(i);
                                        String theHoursInDay = hoursInDay.getString("datetime").split(":")[0];
                                        hourList.add(theHoursInDay);
                                    }
                                    

                                    //loop through the hours in day and compare to the current hour.....
                                    String currentHour = getCurrentHour(); //currently if it rounds up to 4 the 0 is missing...
                                    System.out.println("current hour: " +currentHour);
                                    int indexOfHour = 0;
                                    for (int i = 0; i < hourList.size();i++) {
                                        if (currentHour.equals(hourList.get(i))) {
                                            //now that we found a match find the index....
                                            System.out.println("match found");
                                            indexOfHour = i;
                                        }
                                    }

                                    //if the time is past 11:30pm then we need round up to midnight and make it the next day so we get the correct temp for the next day at midnight
                                    //check to see if the time has been rounded up...if it has then pull data for day2object...
                                    //create a current day object
                                    JSONArray currentDayArray;
                                    if(dateIfRounded.equalsIgnoreCase(day2Date)) {
                                        currentDayArray = day2Object.getJSONArray("hours");
                                        System.out.println("got day 2 object : " +currentDayArray);
                                    } else {
                                        currentDayArray = day1Object.getJSONArray("hours");
                                        System.out.println("got day 1 object : " +currentDayArray);
                                    }

                                    //create an object matching the current hour to the hours weather info from api for the current day....
                                    JSONObject hoursObject = currentDayArray.getJSONObject(indexOfHour);


                                    txt_Date.setText(getTheDate());
                                    txt_Temp.setText(hoursObject.getString("temp").replaceAll("\\..*", ""));
                                    txt_Description.setText(response.getString("description"));
                                    txt_Condition.setText(hoursObject.getString("conditions"));
                                    String conditionIcon = hoursObject.getString("icon");
                                    edt_Location.setText("");

                                    txt_Degrees.setText("°F");
                                    switch (conditionIcon) {
                                        case "snow":
                                            img_Weather.setImageResource(R.drawable.snow);
                                            break;
                                        case "rain":
                                            img_Weather.setImageResource(R.drawable.rain);
                                            break;
                                        case "fog":
                                            img_Weather.setImageResource(R.drawable.fog);
                                            break;
                                        case "wind":
                                            img_Weather.setImageResource(R.drawable.wind);
                                            break;
                                        case "cloudy":
                                            img_Weather.setImageResource(R.drawable.cloudy);
                                            break;
                                        case "partly-cloudy-day":
                                            img_Weather.setImageResource(R.drawable.day_partial_cloud);
                                            break;
                                        case "partly-cloudy-night":
                                            img_Weather.setImageResource(R.drawable.night_half_moon_partial_cloud);
                                            break;
                                        case "clear-day":
                                            img_Weather.setImageResource(R.drawable.day_clear);
                                            break;
                                        case "clear-night":
                                            img_Weather.setImageResource(R.drawable.night_half_moon_clear);
                                            break;
                                        default:
                                            img_Weather.setImageResource(R.drawable.angry_clouds);
                                            break;
                                    }

                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                            }
                        });
                // Access the RequestQueue
                RequestQueue queue = Volley.newRequestQueue(btn_GO.getContext());
                queue.add(jsonObjectRequest);
            }
        });


    }




    private void getCity(String zipcode) {
        String key = getResources().getString(R.string.ZIP_API_KEY);
        String url2 = "https://api.zipcodestack.com/v1/search?apikey=" +key +"&codes="+zipcode +"&country=us";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.v("ZIPS", "Response" + response.toString());
                        try {
                            JSONObject resultsObject = response.getJSONObject("results");
                            JSONArray postalCodeArray = resultsObject.getJSONArray(zipcode);
                            String day1Object = postalCodeArray.getString(0);
                            JSONObject postalObject = postalCodeArray.getJSONObject(0);
                            myLocation.setCity(postalObject.getString("city"));
                            myLocation.setState(postalObject.getString("state"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }

                });

// Access the RequestQueue through your singleton class.
        RequestQueue requestQueue = Volley.newRequestQueue(landing_page.this);
        requestQueue.add(jsonObjectRequest);

    }

    private String getTheDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, MMM dd");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);

    }

    private String getTheTime() {
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("time is: " +time.format(now));
        return time.format(now);
    }

    private String roundDateUp() {
        DateTimeFormatter day = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        System.out.println("date should be: "+day.format(now));
        return day.format(now);
    }

    //this will get the current hour without minutes and seconds
    private String getCurrentHour() {
        int hourAsInt = 0;
        String roundedHour = null;
        //need to round the hour up if it's past 30 minutes
        //if the minutes are below 29 then round the minutes down to nearest hour
        if (Integer.parseInt(getTheTime().split(":")[1]) <= 29) {
            hourAsInt = Integer.parseInt(getTheTime().split(":")[0]);
            System.out.println("minutes is: "+getTheTime().split(":")[1]);
            System.out.println("hours is : " +getTheTime().split(":")[0]);
            if (hourAsInt <= 9) {
                roundedHour = "0" + hourAsInt;
            } else {
                roundedHour = String.valueOf(hourAsInt);
            }
        } else {
            //if the minutes are anything else thats not less than or equal to 29 then round the minutes up to the nearest hour
            hourAsInt = Integer.parseInt(getTheTime().split(":")[0]);
            hourAsInt += 1;
            if (hourAsInt == 24) {
                hourAsInt = 0;
                dateIfRounded = roundDateUp();
                System.out.println("date for api should be: " +dateIfRounded);
                //when this happens we need to adjust the current day to the next day....
            }
            roundedHour = "0" + hourAsInt;
            System.out.println("minutes is: "+getTheTime().split(":")[1]);
            System.out.println("hours is : " +getTheTime().split(":")[0]);
            System.out.println("rounded hour is: " +roundedHour);

        }
        return roundedHour;
    }

public static class location {


    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    private String zipcode, city, state;
}

}