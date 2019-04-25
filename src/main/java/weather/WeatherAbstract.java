package weather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WeatherAbstract {
    public Reader getWeatherReader(String rawUrl) throws IOException {
        URL url = new URL( rawUrl );
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        return new InputStreamReader( inputStream, StandardCharsets.UTF_8 );
    }
}
