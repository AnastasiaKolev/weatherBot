import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private String mode = AbilityMessageCodes.MODE_SELECT_CITY;
    private Users users = new Users();
    private EmojiService emoji = new EmojiService();
    private String token = null;
    private String username = null;

    // for forecast, Date to text formatter
    private static final DateTimeFormatter dateFormatterFromDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    TelegramBot() {
        super();

        Credentials creds = Credentials.getInstance();
        this.token = creds.getBotToken();
        this.username = creds.getBotUsername();

        startAlertTimers();
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String mesText = message.getText();
        String cityRequest = "What city are you interested in?\tFor instance, you can enter: \"London\" or \"London,GB\".\n"
                + "Какой город вас интересует?\tНапример: \"Санкт-Петербург\" или \"Санкт-Петербург,RU\".";

        User currentUser = users.getUser(message.getFrom().getId()
                , message.getFrom().getFirstName()
                , message.getFrom().getLanguageCode()
                , "null"
                , false);

        if ( message.hasText() && message.hasText() ) {
            System.out.println( message.getFrom().getFirstName() + "\t"
                    + currentUser.getUserId() + "\t"
                    + currentUser.getLanguage() + "\t"
                    + currentUser.getLocation() + "\t"
                    + currentUser.getSubscription() + "\t"
                    + "Message: " + mesText);

            if ( mesText.equals( "/start" ) ) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage greetings = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Howdy, " + message.getFrom().getFirstName()
                                + "!\nI'm the weather Master, just kidding."
                                + "\nI'll be glad to inform you about the weather in your city!"
                                + "\nПриветствую, " + message.getFrom().getFirstName()
                                + "!\nЯ - погодный бот. Буду рад рассказать о погоде в вашем городе!\n"
                                + cityRequest );

                keyboardSettings( greetings );
                return;
            }

            if ( mesText.equals( "Language" ) ) {
                mode = AbilityMessageCodes.MODE_SELECT_LANGUAGE;

                SendMessage languageChoice = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Choose language. NOTE: This option is applicable only to the weather description." +
                                "\nВыберите язык. ПРИМЕЧАНИЕ: Этот параметр применяется только к описанию погоды. " );

                languageSettings( languageChoice );

                return;
            }

            //In case of input, not a keyboard option
            if ( mesText.equals( "City" ) || mesText.equals( "city" ) ) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage cityInput = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( cityInput );

                return;
            }

            //new forecast
            if ( mesText.equals( "Forecast" ) ) {
                mode = AbilityMessageCodes.MODE_SELECT_FORECAST;

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( message1 );

                return;
            }

            if ( mesText.equals( "Back" ) ) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( message1 );

                return;
            }

            if ( mesText.equals( "Subscribe to daily Updates" ) ){
                mode = AbilityMessageCodes.MODE_SELECT_SUBSCRIBE;

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Введите город, для которого будете получать обновления: " );

                keyboardSettings( message1 );

                return;
            }

            if ( mesText.equals( "Unsubscribe" ) ){
                currentUser.setSubscription( false );

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( "Вы успешно отписались от ежедневных обновлений!\n"
                                + "Проверяйте погоду в удобное для Вас время, используя опции бота.");

                keyboardSettings( message1 );

                return;
            }

            if ( mode.equals( AbilityMessageCodes.MODE_SELECT_LANGUAGE ) ) {
                currentUser.setLanguage( mesText );

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( cityRequest );

                keyboardSettings( message1 );
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

            if (mode.equals( AbilityMessageCodes.MODE_SELECT_SUBSCRIBE )){
                try {
                    getWeather( message, mesText, currentUser.getLanguage() );
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if ( !currentUser.getSubscription()
                        && !currentUser.getLocation().equals( mesText ) ) {
                    currentUser.setSubscription( true );
                    currentUser.setLocation( mesText );
                    users.saveToDB();
                }

                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage message1 = new SendMessage()
                        .setChatId( message.getChatId().toString() )
                        .setText( currentUser.getFirstName()
                                + " поздравляю, Вы подписались на ежедневные обновления погоды для города: "
                                + mesText);

                keyboardSettings( message1 );

            }
        }
    }

    private void getWeather(Message message, String mesText, String language) throws IOException {
        String result = getWeatherString( mesText, language );

        if (result.equals( "" )) {
            sendMsg( message, "Sorry, couldn't find city.\nИзвините, я не нашел такого города." );
            System.out.println( "Нет такого города!" );
        }

        sendMsg( message, result );
    }

    private String getWeatherString(String mesText, String language) {
        WeatherAccess weatherAccess = new WeatherAccess();
        try {
            weatherAccess.WeatherData( mesText, language );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ( weatherAccess.weatherData.getList().size() == 0 ) {
            return "";
        }

        ListWeather weather = weatherAccess.weatherData.getList().get( 0 );
        Sys currentSys = weather.getSys();
        Main currentMain = weather.getMain();
        List<Weather> curWeather = weather.getWeather();
        Wind wind = weather.getWind();

        String cityName = weather.getName();
        String countryName = currentSys.getCountry();
        int minTemp = currentMain.getTempMin().intValue();
        int maxTemp = currentMain.getTempMax().intValue();
        String windSpeed = wind.getSpeed().toString();
        String description = curWeather.get( 0 ).getDescription();

        String iconId = curWeather.get( 0 ).getIcon();
        String emojiWeather = emoji.getEmojiForWeather(iconId).getUnicode();

        String emojiCity = emoji.getEmojiForWeather( "globe" ).getUnicode();

        return emojiCity + "\tCurrent weather for " + cityName + ", " + countryName
                + "\nMin: \t" + minTemp + " ºC"
                + "\nMax: \t" + maxTemp + " ºC"
                + "\nWind speed: \t" + windSpeed
                + "\nDescription: \t" + description + "\t" + (emoji == null ? "" : emojiWeather);
    }

    private void getWeatherForecast(Message message, String mesText, String language) throws IOException{
        String result = getWeatherForecastString( mesText, language );

        if (result.equals( "" )) {
            sendMsg( message, "Sorry, couldn't find the city.\nИзвините, город не найден." );
            System.out.println( "Нет такого города!" );
        }

        sendMsg( message, result );
    }

    private String getWeatherForecastString(String mesText, String language) throws FileNotFoundException {
        ForecastAccess forecastAccess = new ForecastAccess();
        try {
            forecastAccess.ForecastData( mesText, language );
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println( "Нет такого города!" );
            return "Sorry, couldn't find the city.\nИзвините, город не найден.";
        }

        String cityName = forecastAccess.forecastData.getCity().getName(); //city
        String countryName = forecastAccess.forecastData.getCity().getCountry(); //country

        Emoji emojiCity = EmojiManager.getForAlias( "earth_africa" );
        String forecast;
        StringBuilder weather = new StringBuilder();
        weather.append( emojiCity.getUnicode() ).append( "\tWeather forecast for " )
                .append( cityName ).append( ", " ).append( countryName ).append( "\n" );

        int countDay1 = 0;
        for (int i = 0; i < forecastAccess.forecastData.getList().size(); i++) {
            ListForecast weatherForecast = forecastAccess.forecastData.getList().get( i );

            //getting date from json forecast list
            LocalDate date = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.systemDefault()).toLocalDate();
            //getting 1st date on the list
            LocalDate date1 = Instant.ofEpochSecond( forecastAccess.forecastData.getList().get( 0 ).getDt() ).atZone( ZoneId.systemDefault()).toLocalDate();

            LocalDateTime dateTime = Instant.ofEpochSecond( weatherForecast.getDt() ).atZone( ZoneId.systemDefault() ).toLocalDateTime();

            Main currentMain = weatherForecast.getMain();
            List<Weather> curWeather = weatherForecast.getWeather();
            Wind wind = weatherForecast.getWind();

            int minTemp = currentMain.getTempMin().intValue();
            int maxTemp = currentMain.getTempMax().intValue();
            String description = curWeather.get( 0 ).getDescription();
            String windSpeed = wind.getSpeed().toString();

            String iconId = curWeather.get( 0 ).getIcon();
            String emojiWeather = emoji.getEmojiForWeather(iconId).getUnicode();

            String emojiDate = emoji.getEmojiForWeather( "diamond" ).getUnicode();
            if ( dateFormatterFromDate.format( date ).equals( dateFormatterFromDate.format( date1 ) ) ) {
                if ( dateTimeFormatter.format( dateTime ).equals( "03:00") ) {
                    weather.append( "\n" ).append( emojiDate)
                            .append( "\t" ).append( dateFormatterFromDate.format( date1 ) )
                            .append( "\nNight: \t" ).append( minTemp ).append( " ºC" )
                            .append( "\t" ).append( description ).append( "\t" )
                            .append( emoji == null ? "" : emojiWeather );
                    countDay1++;
                }
                else if ( countDay1 == 1 && dateTimeFormatter.format( dateTime ).equals( "15:00" ) ) {
                    weather.append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                            .append( "\t" ).append( description ).append( "\t" )
                            .append( emoji == null ? "" : emojiWeather )
                            .append( "\nWind speed: " ).append( windSpeed ).append( "\n" );
                }
                else if ( countDay1 == 0 && dateTimeFormatter.format( dateTime ).equals( "15:00" ) ){
                    weather.append( "\n" ).append( emojiDate)
                            .append( "\t" ).append( dateFormatterFromDate.format( date1 ) )
                            .append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                            .append( "\t" ).append( description ).append( "\t" )
                            .append( emoji == null ? "" : emojiWeather )
                            .append( "\nWind speed: " ).append( windSpeed ).append( "\n" );
                }
            }
            else if ( dateTimeFormatter.format( dateTime ).equals( "03:00") ) {
                weather.append( "\n" ).append( emojiDate)
                        .append( "\t" ).append( dateFormatterFromDate.format( date ) )
                        .append( "\nNight: \t" ).append( minTemp ).append( " ºC" )
                        .append( "\t" ).append( description ).append( "\t" )
                        .append( emoji == null ? "" : emojiWeather );
            }
            else if ( dateTimeFormatter.format( dateTime ).equals( "15:00" ) ) {
                weather.append( "\nDay: \t" ).append( maxTemp ).append( " ºC" )
                        .append( "\t" ).append( description ).append( "\t" )
                        .append( emoji == null ? "" : emojiWeather )
                        .append( "\nWind speed: " ).append( windSpeed ).append( "\n" );
            }
        }

        forecast = weather.toString();
        return forecast;
    }

    private void keyboardSettings(SendMessage message1) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add( "Language" );
        row.add( "Forecast" );
        keyboard.add( row );

        row = new KeyboardRow();
        row.add( "Subscribe to daily Updates" );
        row.add( "Unsubscribe" );
        keyboard.add( row );

        replyKeyboardMarkup.setKeyboard( keyboard )
                .setOneTimeKeyboard( true );
        message1.setReplyMarkup( replyKeyboardMarkup );
        try {
            execute( message1 );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void languageSettings(SendMessage message2) {
        ReplyKeyboardMarkup replyKeyboardMarkup1 = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard1 = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add( "en" );
        row1.add( "fr" );
        row1.add( "ru");
        keyboard1.add( row1 );

        row1 = new KeyboardRow();
        row1.add( "de" );
        row1.add( "es" );
        row1.add( "Back" );
        keyboard1.add( row1 );

        replyKeyboardMarkup1.setKeyboard( keyboard1 );
        message2.setReplyMarkup( replyKeyboardMarkup1 );
        try {
            execute( message2 );
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
        TimerExecutor currentTimer = new TimerExecutor();
        currentTimer.startExecutionEveryDayAt(new CustomTimerTask() {
            @Override
            public void execute() {
                sendAlerts();
            }
        }, 0, 0, 1);
    }

    private void sendAlerts() {
        List<User> allSubs = users.getUsersWithSubscription();
        for (User sub : allSubs) {
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(35);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.enableMarkdown(true);
            sendMessage.setChatId(String.valueOf(sub.getUserId()));
            sendMessage.setText(getWeatherString( sub.getLocation(), sub.getLanguage() ));
            try {
                execute(sendMessage);
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
