package com.example.heum.weather;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.heum.weather.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather currentWeather;
    private ImageView iconImageView;

    double latitude= 53.467502;
    double longitude=-113.522423;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForcast(latitude,longitude);
    }

    private void getForcast(double latitude, double longitude) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);

        // final TextView textView = findViewById(R.id.humidityLabel);


        String API = "9380533ffb7a0065ff908d487db74bce";

        String forecast = "https://api.darksky.net/forecast/"
                +API+"/"+latitude+","+longitude;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(forecast).build();

        iconImageView = findViewById(R.id.iconImageView);

        Call call = client.newCall(request);


        if(isNeworkAvailable()) {
            //Asynchronous call
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    try {
                        String JSONdata = response.body().string();
                        Log.v(TAG, JSONdata);
                        if (response.isSuccessful()) {
                            currentWeather = getCurrentDetail(JSONdata);

                            final CurrentWeather displayWeather = new CurrentWeather(
                                    currentWeather.getLocationLabel(),
                                    currentWeather.getIcon(),
                                    currentWeather.getTime(),
                                    currentWeather.getTemperature(),
                                    currentWeather.getHumidity(),
                                    currentWeather.getPrecipChance(),
                                    currentWeather.getSummary(),
                                    currentWeather.getTimezone());

                            binding.setWeather(displayWeather);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    iconImageView.setImageDrawable(drawable);
                                }
                            });



                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "the error is ", e);
                    } catch (JSONException e){
                        Log.e(TAG, "JSON exception found: ",e);
                    }
                }
            });
        }
    }

    private CurrentWeather getCurrentDetail(String jsoNdata) throws JSONException {
        JSONObject forecast = new JSONObject(jsoNdata);

        String timezone = forecast.getString("timezone");
        Log.i(TAG,timezone);

        JSONObject currently = new JSONObject(forecast.getString("currently"));

        CurrentWeather currentWeather = new CurrentWeather();

        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel("Edmonton, CA");
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setTimezone(timezone);

        Log.i(TAG,currentWeather.getFormattedDate());
        Log.i(TAG,String.valueOf(currentWeather.getTemperature()));
        return currentWeather;

    }

    private boolean isNeworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo!=null && networkInfo.isConnected()){
            isAvailable = true;
        }else{
            AlertDialogFragment fragment = new AlertDialogFragment();
            fragment.show(getFragmentManager(),"Network not available!");

           //Toast.makeText(this, R.string.error_network_unavailable,Toast.LENGTH_SHORT).show();
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.show(getFragmentManager(), "error_dialog" );
    }

    public void refreshForcast(View view){
        Toast.makeText(this, "Refreshing Weather Forcast...", Toast.LENGTH_LONG).show();
        getForcast(latitude,longitude);
    }
}
