package weather;

import com.google.gson.Gson;
import credentials.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class ForecastAccess {
    URL urlForecast;
    InputStream inputForecastStream = null;
    URLConnection forecastConnection;
    String searchForecastUrl;
    public ForecastData forecastData;
    String language = "en";
    Credentials creds = Credentials.getInstance();

    public void ForecastSearch(String city, String lang) throws IOException {
        try {
            if (lang.equals( "en" )){
                searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                        + city
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }
            else{
                language = lang;
                searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                        + city
                        + "&lang=" + language
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }

            urlForecast = new URL( searchForecastUrl );
            forecastConnection = urlForecast.openConnection();

            System.out.println( searchForecastUrl );

            inputForecastStream = forecastConnection.getInputStream();
            Reader reader = new InputStreamReader( inputForecastStream, StandardCharsets.UTF_8 );

            Gson gsonForecast = new Gson();
            this.forecastData = gsonForecast.fromJson( reader, ForecastData.class );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
