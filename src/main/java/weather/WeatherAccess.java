package weather;

import com.google.gson.Gson;
import credentials.Credentials;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.List;

public class WeatherAccess extends WeatherAbstract {
    private Credentials creds = Credentials.getInstance();
    private EmojiService emoji = new EmojiService();

    public WeatherData weatherSearch(String city, String lang)  {
        try {
            String url = getWeatherUrl(city, lang);
            System.out.println(url);

            Reader reader = getWeatherReader(url);
            Gson gsonWeather = new Gson();
            return gsonWeather.fromJson(reader, WeatherData.class);
        } catch (IOException e) {
            throw new RuntimeException("can't get WeatherData");
        }
    }

    private String getWeatherUrl(String city, String lang) {
        String searchWeatherUrl;
        if (lang.equals("en")) {
            searchWeatherUrl = "http://api.openweathermap.org/data/2.5/find?q="
                    + city
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        } else {
            searchWeatherUrl = "http://api.openweathermap.org/data/2.5/find?q="
                    + city
                    + "&lang=" + lang
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        }
        return searchWeatherUrl;
    }


    public String formatWeatherData(WeatherData currentWeather) {
        String cityFound;
        if (currentWeather.getList().size() == 0) {
            return "error";
        }

        if (currentWeather.getCod().equals("200")
                && !(currentWeather.getList().size() == 0)) {
            //accessing weather data
            ListWeather weather = currentWeather.getList().get(0);
            Sys currentSys = weather.getSys();
            Main currentMain = weather.getMain();
            List<Weather> curWeather = weather.getWeather();
            Wind wind = weather.getWind();

            //getting weather data
            String cityName = weather.getName();
            String countryName = currentSys.getCountry();
            int minTemp = currentMain.getTempMin().intValue();
            int maxTemp = currentMain.getTempMax().intValue();
            int windSpeed = wind.getSpeed().intValue();
            String description = curWeather.get(0).getDescription();

            //setting emoji
            String iconId = curWeather.get(0).getIcon();
            String emojiWeather = emoji.getEmojiForWeather(iconId);
            String emojiCity = emoji.getEmojiForWeather("globe");

            cityFound = emojiCity + "\tCurrent weather for *" + cityName + ", " + countryName
                    + "*\n_Min_: \t" + minTemp + " ºC"
                    + "\n_Max_: \t" + maxTemp + " ºC"
                    + "\n_Description_: \t" + description + "\t" + (emoji == null ? "" : emojiWeather)
                    + "\n_Wind speed_: \t" + windSpeed + " m/s";
            return cityFound;
        }
        return "error";
    }
}
