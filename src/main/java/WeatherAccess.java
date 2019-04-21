import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherAccess {
    URL url;
    InputStream inputStream = null;
    String searchUrl;
    WeatherData weatherData;
    String language = null;
    Credentials creds = Credentials.getInstance();

    public void WeatherData(String city, String lang) throws IOException {
        try {
            if (lang.equals( null )){
                searchUrl = "http://api.openweathermap.org/data/2.5/find?q="
                        + city
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }
            else{
                language = lang;
                searchUrl = "http://api.openweathermap.org/data/2.5/find?q="
                        + city
                        + "&lang=" + language
                        + "&units=metric&type=like&APPID=" + creds.getAPPID();
            }

            url = new URL( searchUrl );
            System.out.println( searchUrl );
            inputStream = url.openStream();
            Reader reader = new InputStreamReader( inputStream, "UTF-8" );

            Gson gson = new Gson();
            this.weatherData = gson.fromJson( reader, WeatherData.class );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.getStackTrace();
            }
        }
    }
}
