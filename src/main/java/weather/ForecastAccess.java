package weather;

import com.google.gson.Gson;
import credentials.Credentials;

import java.io.IOException;
import java.io.Reader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class ForecastAccess extends WeatherAbstract {
    private Credentials creds = Credentials.getInstance();
    private EmojiService emoji = new EmojiService();

    // for forecast data, Date to text formatter
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern( "HH:mm" );

    public ForecastData forecastSearch(String city, String lang) {
        try {
            String url = getForecastUrl( city, lang );
            System.out.println( url );

            Reader reader = getWeatherReader( url );

            Gson gsonForecast = new Gson();
            return gsonForecast.fromJson( reader, ForecastData.class );
        } catch (IOException e) {
            throw new RuntimeException("can't get ForecastData");
        }
    }

    private String getForecastUrl(String city, String lang) {
        String searchForecastUrl;
        if (lang.equals( "en" )) {
            searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                    + city
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        } else {
            searchForecastUrl = "http://api.openweathermap.org/data/2.5/forecast?q="
                    + city
                    + "&lang=" + lang
                    + "&units=metric&type=like&APPID=" + creds.getAPPID();
        }
        return searchForecastUrl;
    }

    public String formatWeatherForecast(ForecastData currentForecast) {
        String forecastFound;
        if (currentForecast.getCod().equals( "404" )) {
            return "error";
        }

        if (currentForecast.getCod().equals( "200" )) {
            String cityName = currentForecast.getCity().getName();
            String countryName = currentForecast.getCity().getCountry();
            String emojiCity = emoji.getEmojiForWeather( "globe" );

            StringBuilder weather = new StringBuilder();

            weather.append( emojiCity ).append( "\tWeather forecast for *" )
                    .append( cityName ).append( ", " ).append( countryName ).append( "*\n" );

            int countDay1 = 0;
            for (int i = 0; i < currentForecast.getList().size(); i++) {
                ListForecast weatherForecast = currentForecast.getList().get( i );

                //getting date and time to operate on hourly data
                ZonedDateTime date = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.of( "UTC" ) );
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.of( "UTC" ) );

                //getting 1st date to search for both: 3:00 and 15:00 data
                ZonedDateTime date1 = Instant.ofEpochSecond( currentForecast.getList().get( 0 ).getDt() ).atZone( ZoneId.of( "UTC" ) );

                Main curMain = weatherForecast.getMain();
                List<Weather> curWeather = weatherForecast.getWeather();
                Wind wind = weatherForecast.getWind();

                int minTemp = curMain.getTempMin().intValue();
                int maxTemp = curMain.getTempMax().intValue();
                String description = curWeather.get( 0 ).getDescription();
                int windSpeed = wind.getSpeed().intValue();

                String iconId = curWeather.get( 0 ).getIcon();
                String emojiWeather = emoji.getEmojiForWeather( iconId );
                String emojiDate = emoji.getEmojiForWeather( "diamond" );

                if (dateFormat.format( date ).equals( dateFormat.format( date1 ) )) {
                    if (timeFormat.format( zonedDateTime ).equals( "00:00" )) {
                        weather.append( "\n" ).append( emojiDate )
                                .append( "\t*" ).append( dateFormat.format( date1 ) )
                                .append( "*\n_Night_: \t" ).append( minTemp ).append( " ºC" )
                                .append( "\t" ).append( description ).append( "\t" )
                                .append( emoji == null ? "" : emojiWeather );
                        countDay1++;
                    } else if (countDay1 == 1 && timeFormat.format( zonedDateTime ).equals( "12:00" )) {
                        weather.append( "\n_Day_: \t" ).append( maxTemp ).append( " ºC" )
                                .append( "\t" ).append( description ).append( "\t" )
                                .append( emoji == null ? "" : emojiWeather )
                                .append( "\n_Wind speed_: " ).append( windSpeed ).append( " m/s\n" );

                    } else if (countDay1 == 0 && timeFormat.format( zonedDateTime ).equals( "12:00" )) {
                        weather.append( "\n" ).append( emojiDate )
                                .append( "\t*" ).append( dateFormat.format( date1 ) )
                                .append( "*\n_Day_: \t" ).append( maxTemp ).append( " ºC" )
                                .append( "\t" ).append( description ).append( "\t" )
                                .append( emoji == null ? "" : emojiWeather )
                                .append( "\n_Wind speed_: " ).append( windSpeed ).append( " m/s\n" );
                    }
                } else if (timeFormat.format( zonedDateTime ).equals( "00:00" )) {
                    weather.append( "\n" ).append( emojiDate )
                            .append( "\t*" ).append( dateFormat.format( date ) )
                            .append( "*\n_Night_: \t" ).append( minTemp ).append( " ºC" )
                            .append( "\t" ).append( description ).append( "\t" )
                            .append( emoji == null ? "" : emojiWeather );
                } else if (timeFormat.format( zonedDateTime ).equals( "12:00" )) {
                    weather.append( "\n_Day_: \t" ).append( maxTemp ).append( " ºC" )
                            .append( "\t" ).append( description ).append( "\t" )
                            .append( emoji == null ? "" : emojiWeather )
                            .append( "\n_Wind speed_: " ).append( windSpeed ).append( " m/s\n" );
                }
            }
            forecastFound = weather.toString();

            return forecastFound;
        }
        return "error";
    }
}
