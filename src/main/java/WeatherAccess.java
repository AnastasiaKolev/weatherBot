import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WeatherAccess {
    URL urlWeather;
    InputStream inputStream = null;
    URLConnection weatherConnection;
    String searchWeatherUrl;
    WeatherData weatherData;
    String language = "en";
    Credentials creds = Credentials.getInstance();

    public void WeatherData(String city, String lang) throws IOException {
        try {
            if (lang.equals( "en" )){
                searchWeatherUrl = "http://api.openweathermap.org/data/2.5/find?q="
                        + city
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }
            else{
                language = lang;
                searchWeatherUrl = "http://api.openweathermap.org/data/2.5/find?q="
                        + city
                        + "&lang=" + language
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }

            urlWeather = new URL( searchWeatherUrl );
            weatherConnection = urlWeather.openConnection();

            System.out.println( searchWeatherUrl );

            inputStream = weatherConnection.getInputStream();
            Reader reader = new InputStreamReader( inputStream, StandardCharsets.UTF_8 );

            Gson gsonWeather = new Gson();
            this.weatherData = gsonWeather.fromJson( reader, WeatherData.class );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
