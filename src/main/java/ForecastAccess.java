import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ForecastAccess {
    URL urlForecast;
    InputStream inputForecastStream = null;
    URLConnection forecastStream;
    String searchForecastUrl;
    ForecastData forecastData;
    String language = null;

    public void ForecastData(String city, String lang) throws IOException {
        try {
            if (lang.equals( null )){
                searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                        + city
                        + "&units=metric&type=like&APPID=d816117a8ef0e7f4f6249e508a2eadfd";
            }
            else{
                language = lang;
                searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                        + city
                        + "&lang=" + language
                        + "&units=metric&type=like&APPID=d816117a8ef0e7f4f6249e508a2eadfd";
            }

            urlForecast = new URL( searchForecastUrl );
            System.out.println( searchForecastUrl );
            forecastStream = urlForecast.openConnection();

            inputForecastStream = forecastStream.getInputStream();
            Reader reader = new InputStreamReader( inputForecastStream, "UTF-8" );

            Gson jsonForecast = new Gson();
            this.forecastData = jsonForecast.fromJson( reader, ForecastData.class );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
