package weather;

import com.google.gson.Gson;
import credentials.Credentials;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ForecastAccess extends WeatherAbstract {
    private ForecastData currentForecast;
    private Credentials creds = Credentials.getInstance();

    // for forecast data, Date to text formatter
    private static final DateTimeFormatter dateFormatterFromDate = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );

    private EmojiService emoji = new EmojiService();

    private void forecastSearch(String city, String lang) throws IOException {
        try {
            String url = getForecastUrl( city, lang );
            System.out.println( url );

            Reader reader = getWeatherReader( url );

            Gson gsonForecast = new Gson();
            this.currentForecast = gsonForecast.fromJson( reader, ForecastData.class );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private String getForecastUrl(String city, String lang) {
        String searchForecastUrl;
        String language;
        if (lang.equals( "en" )) {
            searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                    + city
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        } else {
            language = lang;
            searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                    + city
                    + "&lang=" + language
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        }
        return searchForecastUrl;
    }

    public String getWeatherForecastString(String mesText, String language) {
        String forecastFound;
        try {
            forecastSearch( mesText, language );

            if (currentForecast.getCod().equals( "404" )) {
                return "Sorry, city not found.";
            }

            if (currentForecast.getCod().equals( "200" )) {
                String cityName = currentForecast.getCity().getName();
                String countryName = currentForecast.getCity().getCountry();

                String emojiCity = emoji.getEmojiForWeather( "globe" ).getUnicode();

                StringBuilder weather = new StringBuilder();
                weather.append( emojiCity ).append( "\tWeather forecast for " )
                        .append( cityName ).append( ", " ).append( countryName ).append( "\n" );

                int countDay1 = 0;
                for (int i = 0; i < currentForecast.getList().size(); i++) {
                    ListForecast weatherForecast = currentForecast.getList().get( i );

                    //getting date from json list
                    LocalDate day = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.systemDefault() ).toLocalDate();
                    //getting 1st date to search for both: 3:00 and 15:00 data
                    LocalDate day1st = Instant.ofEpochSecond( currentForecast.getList().get( 0 ).getDt() ).atZone( ZoneId.systemDefault() ).toLocalDate();
                    //getting time to operate on hourly data - 3:00 and 15:00 UTC
                    LocalDateTime dateTime = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.systemDefault() ).toLocalDateTime();

                    Main curMain = weatherForecast.getMain();
                    List<Weather> curWeather = weatherForecast.getWeather();
                    Wind wind = weatherForecast.getWind();

                    int minTemp = curMain.getTempMin().intValue();
                    int maxTemp = curMain.getTempMax().intValue();
                    String description = curWeather.get( 0 ).getDescription();
                    int windSpeed = wind.getSpeed().intValue();

                    String iconId = curWeather.get( 0 ).getIcon();
                    String emojiWeather = emoji.getEmojiForWeather( iconId ).getUnicode();
                    String emojiDate = emoji.getEmojiForWeather( "diamond" ).getUnicode();

                    if (dateFormatterFromDate.format( day ).equals( dateFormatterFromDate.format( day1st ) )) {
                        if (dateTimeFormatter.format( dateTime ).equals( "03:00" )) {
                            weather.append( "\n" ).append( emojiDate )
                                    .append( "\t" ).append( dateFormatterFromDate.format( day1st ) )
                                    .append( "\nNight: \t" ).append( minTemp ).append( " ºC" )
                                    .append( "\t" ).append( description ).append( "\t" )
                                    .append( emoji == null ? "" : emojiWeather );
                            countDay1++;
                        } else if (countDay1 == 1 && dateTimeFormatter.format( dateTime ).equals( "15:00" )) {
                            weather.append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                                    .append( "\t" ).append( description ).append( "\t" )
                                    .append( emoji == null ? "" : emojiWeather )
                                    .append( "\nWind speed: " ).append( windSpeed ).append( " m/s\n" );
                        } else if (countDay1 == 0 && dateTimeFormatter.format( dateTime ).equals( "15:00" )) {
                            weather.append( "\n" ).append( emojiDate )
                                    .append( "\t" ).append( dateFormatterFromDate.format( day1st ) )
                                    .append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                                    .append( "\t" ).append( description ).append( "\t" )
                                    .append( emoji == null ? "" : emojiWeather )
                                    .append( "\nWind speed: " ).append( windSpeed ).append( " m/s\n" );
                        }
                    } else if (dateTimeFormatter.format( dateTime ).equals( "03:00" )) {
                        weather.append( "\n" ).append( emojiDate )
                                .append( "\t" ).append( dateFormatterFromDate.format( day ) )
                                .append( "\nNight: \t" ).append( minTemp ).append( " ºC" )
                                .append( "\t" ).append( description ).append( "\t" )
                                .append( emoji == null ? "" : emojiWeather );
                    } else if (dateTimeFormatter.format( dateTime ).equals( "15:00" )) {
                        weather.append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                                .append( "\t" ).append( description ).append( "\t" )
                                .append( emoji == null ? "" : emojiWeather )
                                .append( "\nWind speed: " ).append( windSpeed ).append( " m/s\n" );
                    }
                }
                forecastFound = weather.toString();

                return forecastFound;
            }
        } catch (IOException e) {
            // e.printStackTrace();
            System.out.println( "Нет такого города!" );
            return "Sorry, city not found.";
        }
        return "error";
    }
}
