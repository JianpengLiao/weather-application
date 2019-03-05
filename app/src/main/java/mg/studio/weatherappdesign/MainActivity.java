package mg.studio.weatherappdesign;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        boolean isNetworkAvailable =checkNetwork();
        if(isNetworkAvailable){
            new DownloadUpdate().execute();
        }
    }


    private void init(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        ((TextView) findViewById(R.id.tv_date)).setText(simpleDateFormat.format(date));
        Calendar calendar=Calendar.getInstance();
        String[] dayArry=new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        String[] DAYArry=new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        int day=calendar.get(Calendar.DAY_OF_WEEK);
        ((TextView) findViewById(R.id.tv_week)).setText(dayArry[day-1]);
        ((TextView) findViewById(R.id.tv_day1)).setText(DAYArry[(day)%7]);
        ((TextView) findViewById(R.id.tv_day2)).setText(DAYArry[(day+1)%7]);
        ((TextView) findViewById(R.id.tv_day3)).setText(DAYArry[(day+2)%7]);
        ((TextView) findViewById(R.id.tv_day4)).setText(DAYArry[(day+3)%7]);
    }


    public void btnClick(View view) {
        boolean isNetworkAvailable =checkNetwork();
        if(isNetworkAvailable){
            new DownloadUpdate().execute();
        }
    }


    //Check if the current network is available
    public boolean checkNetwork(){
        //Get the current network connection service
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager==null) {
            return false;
        }
        //Get active network connection information
        NetworkInfo networkInfo  = connectivityManager.getActiveNetworkInfo();
        if (networkInfo  == null || !networkInfo.isAvailable()) {
            //No activated network connection
            midToast("Network Unavailable!", Toast.LENGTH_SHORT);
            return false;
        }
        else {
            //Have an activated network connection
            return true;
        }
    }


    //show Toast
    public void midToast(String str, int showTime)
    {
        Toast toast = Toast.makeText(MainActivity.this, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL , 0, 0);  //set the display location
        //TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        //v.setTextColor(getResources().getColor(R.color.messageTextClor));     //设置字体颜色
        toast.show();
    }



    private class DownloadUpdate extends AsyncTask<String, Void, String> {

        int Temperature=9999;
        int flag1=0;
        int flag2=0;
        int ClearRatio=0;
        int RainRatio=0;
        int CloudsRatio=0;
        String strDate1="0000";
        String strDate2="0000";
        String[] WeatherArry=new String[5];
        int[] MaxTemperature=new int[5];
        int[] MinTemperature=new int[5];

        @Override
        protected String doInBackground(String... strings) {
            String stringUrl="http://api.openweathermap.org/data/2.5/forecast?q=Chongqing,cn&mode=json&APPID=aa3d744dc145ef9d350be4a80b16ecab\n";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mainly needed for debugging
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //The temperature
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        //Get the temperature of now and the weather of 5 day.
        private void getWeather(String strjson) {
            try {
                int jumpStep=0;
                int flag=0;
                Calendar cal = Calendar.getInstance();
                int hour=cal.get(Calendar.HOUR_OF_DAY);
                if(hour<3){
                    jumpStep=2;
                    flag=1;
                }

                else if(hour<5)
                {
                    jumpStep=1;
                    flag=1;
                }
                else
                {
                    jumpStep=0;
                    flag=0;
                }

                int temperature=28;
                JSONObject jsonObject1 = new JSONObject(strjson);
                JSONArray jsonArray = jsonObject1.getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                    // get dt_txt
                    String dt_txt=jsonObject.getString("dt_txt");

                    //get Temperature of now
                    JSONObject jsonObjectMain = jsonObject.getJSONObject("main");
                    double dwDegreeKelvin=jsonObjectMain.getDouble("temp");
                    temperature=(int)Math.round(dwDegreeKelvin-273.15);

                    //get weather
                    JSONArray jarrayWeather = jsonObject.getJSONArray("weather");
                    JSONObject jsonObjectWeather=(JSONObject)jarrayWeather.get(0);
                    String strWeather=jsonObjectWeather.getString("main");

                    if(jumpStep==2){
                        jumpStep--;
                    }
                    else if(jumpStep==1){
                        jumpStep--;
                    }
                    else {
                        getMaxMinTemperature(dt_txt,temperature);
                        getWeatherOfEachDay(dt_txt, strWeather);

                        if(i==2)
                            Temperature=temperature;
                    }
                }
                if(flag==1)
                    setTheFinialDayWeather();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        //get the max temperature and the min temperature of each day
        private void getMaxMinTemperature(String dt_txt, int temperature){
            if(flag1>=5)
                return;
            String tempDate=dt_txt.substring(0,10);
            if(strDate1.equals("0000")){
                strDate1=tempDate;
                flag1=0;
                MaxTemperature[flag1]=temperature;
                MinTemperature[flag1]=temperature;
            }
            else {
               if(tempDate.equals(strDate1)){
                   if(temperature>MaxTemperature[flag1])
                       MaxTemperature[flag1]=temperature;
                   else if(temperature<MinTemperature[flag1])
                       MinTemperature[flag1]=temperature;
                   else {
                       return;
                   }
               }
               else {
                   if(flag1==4){
                       flag1=flag1+1;
                       strDate1="0000";
                       return;
                   }
                   flag1=flag1+1;
                   strDate1=tempDate;
                   MaxTemperature[flag1]=temperature;
                   MinTemperature[flag1]=temperature;
               }

            }
        }


        //get the weather of each five day
        private void getWeatherOfEachDay(String dt_txt, String strWeather){
            if(flag2>=5)
                return;
            String tempDate=dt_txt.substring(0,10);
            if(strDate2.equals("0000")){
                strDate2=tempDate;
                flag2=0;
                updateRatio(strWeather);
            }
            else {
                if(tempDate.equals(strDate2)){
                    updateRatio(strWeather);
                }
                else {
                    if(flag2==5){
                        flag2=flag2+1;
                        strDate2="0000";
                        return;
                    }
                    int temp=CloudsRatio;
                    if(ClearRatio>=temp){
                        temp=ClearRatio;
                        if(RainRatio>=temp)
                            WeatherArry[flag2]="Rain";
                        else
                            WeatherArry[flag2]="Clear";
                    }
                    else if(RainRatio>=temp)
                        WeatherArry[flag2]="Rain";
                    else
                        WeatherArry[flag2]="Clouds";

                    flag2=flag2+1;
                    strDate2=tempDate;
                    ClearRatio=0;
                    RainRatio=0;
                    CloudsRatio=0;
                    updateRatio(strWeather);
                }

            }
        }

        //Count the Ratio of various weather in a day
        private void updateRatio(String strWeather){
            switch (strWeather) {
                case "Clear":
                    ClearRatio++;
                    break;
                case "Rain":
                    RainRatio++;
                    break;
                case "Clouds":
                    CloudsRatio++;
                    break;
            }
        }
        //In some cases, you need to set the weather for the last day separately.
        private void setTheFinialDayWeather(){
            int temp=CloudsRatio;
            if(ClearRatio>=temp){
                temp=ClearRatio;
                if(RainRatio>=temp)
                    WeatherArry[flag2]="Rain";
                else
                    WeatherArry[flag2]="Clear";
            }
            else if(RainRatio>=temp)
                WeatherArry[flag2]="Rain";
            else
                WeatherArry[flag2]="Clouds";
        }


        //update the icon of the weather
        private void updateWeatherIcon(){
            int[] imgIdList=new int[]{R.id.img_weather_condition, R.id.img_day1, R.id.img_day2, R.id.img_day3, R.id.img_day4};

            for(int i=0;i<5;i++) {
                switch (WeatherArry[i]) {
                    case "Clear":
                        ((ImageView) findViewById(imgIdList[i])).setImageDrawable(
                                ContextCompat.getDrawable(getApplicationContext(), R.drawable.sunny_small));
                        break;
                    case "Rain":
                        ((ImageView) findViewById(imgIdList[i])).setImageDrawable(
                                ContextCompat.getDrawable(getApplicationContext(), R.drawable.rainy_small));
                        break;
                    case "Clouds":
                        ((ImageView) findViewById(imgIdList[i])).setImageDrawable(
                                ContextCompat.getDrawable(getApplicationContext(), R.drawable.partly_sunny_small));
                        break;
                    default:
                        break;
                }
            }
        }


        //update the Temperature of each five day
        private void updateTemperature(){
            int[] tvIdList=new int[]{R.id.tv_day0Tem,R.id.tv_day1Tem, R.id.tv_day2Tem, R.id.tv_day3Tem, R.id.tv_day4Tem};

            for(int i=0;i<5;i++){
                String strTem=MinTemperature[i]+"～"+MaxTemperature[i]+"°C";
                ((TextView) findViewById(tvIdList[i])).setText(strTem);
            }
        }


        @Override
        protected void onPostExecute(String strBuffer) {
            getWeather(strBuffer);

            //Update the temperature displayed
            if(Temperature!=9999){
                ((TextView) findViewById(R.id.temperature_of_the_day)).setText(Integer.toString(Temperature));
            }
            updateWeatherIcon();
            updateTemperature();
            String time =getCurTime();
            midToast("Successfully updated the weather!\n  "+time, Toast.LENGTH_SHORT);
            flag1=0;
            strDate1="0000";
            strDate2="0000";
        }


        private String getCurTime(){
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.CHINA);
            Date date = new Date(System.currentTimeMillis());
            String time=simpleDateFormat1.format(date);
            return time;
        }


    }



}
