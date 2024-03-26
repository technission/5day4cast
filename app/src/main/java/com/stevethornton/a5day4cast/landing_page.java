package com.stevethornton.a5day4cast;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class landing_page extends AppCompatActivity {

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


        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zipcode = edt_Location.getText().toString();

                //connect to the api
                String key = getResources().getString(R.string.API_KEY);
                String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"+zipcode +"?unitGroup=us&key=" +key +"&contentType=json";



                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onResponse(JSONObject response) {

                                Log.v("WEATHER", "Response: " + response.toString());


                                //make sure to hide your api key before submitting to google play....

                                try {
                                    //this gives us an array of objects
                                    JSONArray daysArray = response.getJSONArray("days");


                                    //loop through the daysArray until we find a match based on date not time
                                    JSONObject day1Object = null;
                                    for (int i = 0; i <= 4; i++) {
                                        day1Object = daysArray.getJSONObject(0); //this pulls data for the first day



                                    }

                                    //each days hours need to be set
                                    JSONArray hoursDay1 = day1Object.getJSONArray("hours");


                                    //loop through the hours array

                                    for (int i = 0; i < hoursDay1.length(); i++) {
                                        JSONObject hoursObject = hoursDay1.getJSONObject(i);



                                        if (roundTimeByNearestHour().equalsIgnoreCase(hoursObject.getString("datetime"))) {


                                            String matchedTime = hoursObject.getString("datetime");

                                            String matchedTemp = hoursObject.getString("temp");
                                            String snowFallPercent = hoursObject.getString("snow");
                                            String cloudCoverPercent = hoursObject.getString("cloudcover");
                                            String rainFallPercent = hoursObject.getString("precipprob");
                                            String conditionIcon = hoursObject.getString("icon");

                                            String currentCondition = hoursObject.getString("conditions");

                                            //get the sunrise and sunset
                                            String[] sunRise = day1Object.getString("sunrise").split(":");
                                            int sunUp = Integer.valueOf(sunRise[0]);
                                            String[] sunSet = day1Object.getString("sunset").split(":");
                                            int sunDown = Integer.valueOf(sunSet[0]);

                                            //if matchedTime is night time make sure the weather icon shows nightime icons

                                            String[] time=matchedTime.split(":");
                                            int hours = Integer.valueOf(time[0]);




                                            //set the condition currently under the weather icon

                                            txt_Condition.setText(currentCondition);

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
                                            //this removes the decimal and everything after it for the temp
                                            txt_Temp.setText(matchedTemp.replaceAll("\\..*", ""));
                                            edt_Location.setText("");

                                        }
                                    }



                                    txt_Date.setText(getCurrentDate());


                                    String description = response.getString("description");
                                    txt_Description.setText(description);

                                    txt_Currently.setText("Currently");
                                    txt_Degrees.setText("°F");
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
    private String getCurrentDate () {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

    private String getDateForAPI () {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateForAPI = dateFormat.format(calendar.getTime());
        return dateForAPI;
    }

    private String roundTimeByNearestHour () {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        //this shows full time such as 14:30:20
        String unRoundedTime = dateFormat.format(currentTime.getTime());
        String[] roundedTimeArray = unRoundedTime.split(":");
        String hours = roundedTimeArray[0];
        String minutes = roundedTimeArray[1];
        String fullTime = null;
        //want the seconds to always be 00
        String seconds = "00";
        String roundedHour = null;
        if (Integer.valueOf(minutes) <= 29) {
            //round down to the hour
            minutes = "00";
            fullTime = hours +":" +minutes + ":" +seconds;
        } else {
            minutes = "00";
            if (Integer.parseInt(hours) < 10) {
                int hour = Integer.parseInt(hours);
                hour += 1;
                roundedHour = "0" + hour;
                fullTime = roundedHour +":" +minutes +":" +seconds;
            } else {
                int hour = Integer.parseInt(hours);
                hour += 1;
                roundedHour = String.valueOf(hour);
                fullTime = roundedHour +":" +minutes +":" +seconds;
            }
        }
        return fullTime;


    }

}