import alerts.CustomTimerTask;
import alerts.TimerExecutor;
import credentials.Credentials;
import database.User;
import database.Users;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import weather.*;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private Users users;
    private String token;
    private String username;

    private String mode = AbilityMessageCodes.MODE_SELECT_CITY;

    // for forecast data, Date to text formatter
    private static final DateTimeFormatter dateFormatterFromDate = DateTimeFormatter.ofPattern( "dd/MM/yyyy" );
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern( "HH:mm" );

    private EmojiService emoji = new EmojiService();

    TelegramBot() {
        super();

        users = new Users();

        Credentials creds = Credentials.getInstance();
        this.token = creds.getBotToken();
        this.username = creds.getBotUsername();

        startAlertTimers();
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String mesText = message.getText();
        String cityRequest = "What city are you interested in?\tFor instance, you can enter: \"London\" or \"London,GB\"."
                + "\n\nКакой город вас интересует?\tНапример: \"Санкт-Петербург\".";

        User currentUser = users.getUser( message.getFrom().getId()
                , message.getFrom().getFirstName()
                , message.getFrom().getLanguageCode()
                , "null"
                , false );

        if (message.hasText() && message.hasText()) {
            System.out.println( message.getFrom().getFirstName() + "\t"
                    + currentUser.getUserId() + "\t"
                    + currentUser.getLanguage() + "\t"
                    + currentUser.getLocation() + "\t"
                    + currentUser.getSubscription() + "\t"
                    + "Message: " + mesText );

            if (mesText.equals( "/start" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage greetings = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Howdy, " + message.getFrom().getFirstName()
                                + "\nI'll be glad to inform you about the weather in your city!"
                                + "\n\nПриветствую, " + message.getFrom().getFirstName()
                                + "!\nБуду рад рассказать о погоде в вашем городе!\n\n"
                                + cityRequest );

                keyboardSettings( greetings );
                return;
            }

            if (mesText.equals( "Language" )) {
                mode = AbilityMessageCodes.MODE_SELECT_LANGUAGE;

                SendMessage languageChoice = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Choose language. NOTE: This option is applicable only to the weather description."
                                + "\n\nВыберите язык. ПРИМЕЧАНИЕ: Этот параметр применяется только к описанию погоды. " );

                languageSettings( languageChoice );

                return;
            }

            //In case of input, not a keyboard option
            if (mesText.equals( "weather.City" ) || mesText.equals( "city" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage cityInput = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( cityInput );

                return;
            }

            //new forecast
            if (mesText.equals( "Forecast" )) {
                mode = AbilityMessageCodes.MODE_SELECT_FORECAST;

                SendMessage forecast = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( forecast );

                return;
            }

            if (mesText.equals( "Back" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage back = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( back );

                return;
            }

            if (mesText.equals( "Subscribe to daily Updates" )) {
                mode = AbilityMessageCodes.MODE_SELECT_SUBSCRIBE;

                SendMessage subscribe = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Enter the city for which you want to receive updates:"
                                + "\n\nВведите город для которого хотите получать обновления:" );

                keyboardSettings( subscribe );

                return;
            }

            if (mesText.equals( "Unsubscribe" )) {
                currentUser.setSubscription( false );
                users.saveToDB();

                SendMessage unsubscribe = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "You have successfully unsubscribed."
                                + "\n\nВы успешно отписались.\n" );

                keyboardSettings( unsubscribe );

                return;
            }

            if (mode.equals( AbilityMessageCodes.MODE_SELECT_LANGUAGE )) {
                currentUser.setLanguage( mesText );
                users.saveToDB();

                SendMessage modeLang = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( modeLang );
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                return;
            }

            if (mode.equals( AbilityMessageCodes.MODE_SELECT_CITY )) {
                try {
                    getWeather( message, mesText, currentUser.getLanguage() );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            //new forecast mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_FORECAST )) {
                try {
                    getWeatherForecast( message, mesText, currentUser.getLanguage() );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            if (mode.equals( AbilityMessageCodes.MODE_SELECT_SUBSCRIBE )) {
                try {
                    getWeather( message, mesText, currentUser.getLanguage() );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ( getWeatherString( mesText, currentUser.getLanguage())
                        .equals( "Sorry, city not found." ) ) {
                    sendMsg( message, "Enter correct city, please." );

                    mode = AbilityMessageCodes.MODE_SELECT_SUBSCRIBE;
                } else {
                    if (!currentUser.getLocation().equals( mesText )) {
                        currentUser.setSubscription( true );
                        currentUser.setLocation( mesText );
                        users.saveToDB();
                    }

                    mode = AbilityMessageCodes.MODE_SELECT_CITY;

                    SendMessage subscription = new SendMessage()
                            .setChatId( message.getChatId().toString() )
                            .setText( currentUser.getFirstName()
                                    + " congrats, you have subscribed to daily updates for the city: "
                                    + mesText
                                    + "\n\n" + currentUser.getFirstName()
                                    + " поздравляю, Вы подписались на ежедневные обновления для города: "
                                    + mesText );

                    keyboardSettings( subscription );
                }
            }
        }
    }

    private void getWeather(Message message, String mesText, String language) throws IOException {
        String result = getWeatherString( mesText, language );

        if (result.equals( "error" )) {
            sendMsg( message, "Sorry, city not found." );
            System.out.println( "Нет такого города!" );
        } else {
            sendMsg( message, result );
        }
    }

    private String getWeatherString(String mesText, String language) {
        String cityFound;
        try {
            WeatherAccess weatherAccess = new WeatherAccess();
            weatherAccess.WeatherSearch( mesText, language );

            if ( weatherAccess.weatherData.getList().size() == 0 ) {
                return "Sorry, city not found.";
            }

            if ( weatherAccess.weatherData.getCod().equals( "200" )
                    && !( weatherAccess.weatherData.getList().size() == 0) ) {
                //accessing weather data
                ListWeather weather = weatherAccess.weatherData.getList().get( 0 );
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
                String description = curWeather.get( 0 ).getDescription();

                //setting emoji
                String iconId = curWeather.get( 0 ).getIcon();
                String emojiWeather = emoji.getEmojiForWeather( iconId ).getUnicode();
                String emojiCity = emoji.getEmojiForWeather( "globe" ).getUnicode();

                cityFound = emojiCity + "\tCurrent weather for " + cityName + ", " + countryName
                        + "\nMin: \t" + minTemp + " ºC"
                        + "\nMax: \t" + maxTemp + " ºC"
                        + "\nDescription: \t" + description + "\t" + (emoji == null ? "" : emojiWeather)
                        + "\nWind speed: \t" + windSpeed + " m/s";
                return cityFound;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println( "Нет такого города!" );
            return "Sorry, city not found.";
        }
        return "error";
    }

    private void getWeatherForecast(Message message, String mesText, String language) throws IOException {
        String result = getWeatherForecastString( mesText, language );

        if (result.equals( "error" )) {
            sendMsg( message, "Sorry, city not found." );
            System.out.println( "Нет такого города!" );
        } else {
            sendMsg( message, result );
        }
    }

    private String getWeatherForecastString(String mesText, String language) {
        String forecastFound;
        try {
            ForecastAccess forecastAccess = new ForecastAccess();
            forecastAccess.ForecastSearch( mesText, language );

            if ( forecastAccess.forecastData.getCod().equals( "404" ) ){
                return "Sorry, city not found.";
            }

            if ( forecastAccess.forecastData.getCod().equals( "200" ) ) {
                String cityName = forecastAccess.forecastData.getCity().getName();
                String countryName = forecastAccess.forecastData.getCity().getCountry();

                String emojiCity = emoji.getEmojiForWeather( "globe" ).getUnicode();

                StringBuilder weather = new StringBuilder();
                weather.append( emojiCity ).append( "\tWeather forecast for " )
                        .append( cityName ).append( ", " ).append( countryName ).append( "\n" );

                int countDay1 = 0;
                for (int i = 0; i < forecastAccess.forecastData.getList().size(); i++) {
                    ListForecast weatherForecast = forecastAccess.forecastData.getList().get( i );

                    //getting date from json list
                    LocalDate day = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.systemDefault() ).toLocalDate();
                    //getting 1st date to search for both: 3:00 and 15:00 data
                    LocalDate day1st = Instant.ofEpochSecond( forecastAccess.forecastData.getList().get( 0 ).getDt() ).atZone( ZoneId.systemDefault() ).toLocalDate();
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
            e.printStackTrace();
            System.out.println( "Нет такого города!" );
            return "Sorry, city not found.";
        }
        return "error";
    }

    private void keyboardSettings(SendMessage message) {
        ReplyKeyboardMarkup mainReplyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> mainKeyboard = new ArrayList<>();

        KeyboardRow mainRow1 = new KeyboardRow();
        mainRow1.add( "Language" );
        mainRow1.add( "Forecast" );
        mainKeyboard.add( mainRow1 );

        KeyboardRow mainRow2 = new KeyboardRow();
        mainRow2.add( "Subscribe to daily Updates" );
        mainRow2.add( "Unsubscribe" );
        mainKeyboard.add( mainRow2 );

        mainReplyKeyboardMarkup.setKeyboard( mainKeyboard )
                .setResizeKeyboard( true )
                .setOneTimeKeyboard( true );

        message.setReplyMarkup( mainReplyKeyboardMarkup );
        try {
            execute( message );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void languageSettings(SendMessage message) {
        ReplyKeyboardMarkup langReplyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> langKeyboard = new ArrayList<>();

        KeyboardRow langRow1 = new KeyboardRow();
        langRow1.add( "en" );
        langRow1.add( "fr" );
        langRow1.add( "ru" );
        langKeyboard.add( langRow1 );

        KeyboardRow langRow2 = new KeyboardRow();
        langRow2.add( "de" );
        langRow2.add( "es" );
        langRow2.add( "Back" );
        langKeyboard.add( langRow2 );

        langReplyKeyboardMarkup.setKeyboard( langKeyboard )
                .setResizeKeyboard( true );

        message.setReplyMarkup( langReplyKeyboardMarkup );
        try {
            execute( message );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown( true );
        sendMessage.setChatId( message.getChatId().toString() );
        sendMessage.setReplyToMessageId( message.getMessageId() );
        sendMessage.setText( text );
        try {
            execute( sendMessage );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void startAlertTimers() {
        final LocalDateTime localNow = LocalDateTime.now( Clock.systemUTC());

        TimerExecutor currentTimer = new TimerExecutor();
        currentTimer.startExecutionEveryDayAt( new CustomTimerTask() {
            @Override
            public void execute() {
                sendAlerts();
            }
        }, localNow.getHour(), localNow.getMinute(), localNow.getSecond() );
    }

    private void sendAlerts() {
        List<User> allSubs = users.getUsersWithSubscription();

        for (User sub : allSubs) {
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait( 35 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println( "Update for " + sub.getFirstName() + " location = " + sub.getLocation() + " lang = " + sub.getLanguage() );

            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown( true );
            sendMessage.setChatId( String.valueOf( sub.getUserId() ) );
            sendMessage.setText( "Update for your subscription:\n\n" + getWeatherForecastString( sub.getLocation(), sub.getLanguage() ));
            try {
                execute( sendMessage );
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return username;
    }

    public String getBotToken() {
        return token;
    }
}
