import alerts.AlertsHandler;
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
import weather.ForecastAccess;
import weather.WeatherAccess;

import java.util.ArrayList;
import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private Users users;
    private String token;
    private String username;
    private String mode = AbilityMessageCodes.MODE_SELECT_CITY;

    TelegramBot() {
        super();
        users = Users.getInstance();

        Credentials credentials = Credentials.getInstance();
        this.token = credentials.getBotToken();
        this.username = credentials.getBotUsername();

        startAlerts();
    }

    private void startAlerts() {
        AlertsHandler alerts = new AlertsHandler() {
            @Override
            public void execute(SendMessage msg) {
                execute( msg );
            }
        };
        alerts.startAlertTimers();
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String mesText = message.getText();

        User currentUser = users.getUser( message.getFrom().getId()
                , message.getFrom().getFirstName()
                , message.getFrom().getLanguageCode()
                , "null"
                , false );

        if (message.hasText()) {
            System.out.println( message.getFrom().getFirstName() + "\t"
                    + currentUser.getUserId() + "\t"
                    + currentUser.getLanguage() + "\t"
                    + currentUser.getLocation() + "\t"
                    + currentUser.getSubscription() + "\t"
                    + "Message: " + mesText );

            String cityRequest = "What city are you interested in?\tFor instance, you can enter: _London_ or _London,GB_."
                    + "\n\nКакой город вас интересует?\tНапример: _Санкт-Петербург_.";

            String greetingsText = "Howdy, *" + message.getFrom().getFirstName()
                    + "*\nI'll be glad to inform you about the weather in your city!"
                    + "\n\nПриветствую, *" + message.getFrom().getFirstName()
                    + "*!\nБуду рад рассказать о погоде в вашем городе!\n\n"
                    + cityRequest;

            String languageChoiceText = "Choose language. _Note_: This option is applicable only to the weather *description*."
                    + "\n\nВыберите язык. _Примечание_: Этот параметр применяется только к *описанию* погоды. ";

            String subscribeText = "Enter the *city* for which you want to receive updates:"
                    + "\n\nВведите *город* для которого хотите получать обновления:";

            String unsubscribeText = "You have successfully *unsubscribed*."
                    + "\n\nВы успешно отписались.\n";

            String subscriptionSuccessText = "*" +currentUser.getFirstName()
                    + "* congrats, you have subscribed to daily updates for the city: *" + mesText
                    + "*\n\n*" + currentUser.getFirstName()
                    + "* поздравляю, Вы подписались на ежедневные обновления для города: *" + mesText + "*";

            if (mesText.equals( "/start" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage greetings = getSendMessage( message, greetingsText );
                keyboardSettings( greetings );
                return;
            }

            if (mesText.equals( "Language" )) {
                mode = AbilityMessageCodes.MODE_SELECT_LANGUAGE;

                SendMessage languageChoice = getSendMessage( message, languageChoiceText );
                languageSettings( languageChoice );
                return;
            }

            //In case of input, not a keyboard option
            if (mesText.equals( "Current" ) || mesText.equals( "city" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage cityInput = getSendMessage( message, cityRequest );
                keyboardSettings( cityInput );
                return;
            }

            //new forecast
            if (mesText.equals( "Forecast" )) {
                mode = AbilityMessageCodes.MODE_SELECT_FORECAST;

                SendMessage forecast = getSendMessage( message, cityRequest );
                keyboardSettings( forecast );
                return;
            }

            if (mesText.equals( "Back" )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                SendMessage back = getSendMessage( message, cityRequest );
                keyboardSettings( back );
                return;
            }

            if (mesText.equals( "Subscribe to daily Updates" )) {
                mode = AbilityMessageCodes.MODE_SELECT_SUBSCRIBE;

                SendMessage subscribe = getSendMessage( message, subscribeText );
                keyboardSettings( subscribe );
                return;
            }

            if (mesText.equals( "Unsubscribe" )) {
                currentUser.setSubscription( false );
                users.saveToDB();

                SendMessage unsubscribe = getSendMessage( message, unsubscribeText );
                keyboardSettings( unsubscribe );
                return;
            }

            //basic mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_CITY )) {
                getWeather( message, mesText, currentUser.getLanguage() );
                return;
            }

            //forecast mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_FORECAST )) {
                getWeatherForecast( message, mesText, currentUser.getLanguage() );
                return;
            }

            //language mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_LANGUAGE )) {
                mode = AbilityMessageCodes.MODE_SELECT_CITY;

                currentUser.setLanguage( mesText );
                users.saveToDB();

                SendMessage modeLang = getSendMessage( message, cityRequest );
                keyboardSettings( modeLang );
                return;
            }

            //subscribe mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_SUBSCRIBE )) {
                WeatherAccess weatherAccess = new WeatherAccess();
                String result = weatherAccess.getWeatherString( mesText, currentUser.getLanguage() );

                if (result.equals( "error" )) {
                    mode = AbilityMessageCodes.MODE_SELECT_SUBSCRIBE;

                    sendMsg( message, "Enter correct city, please." );
                } else {
                    mode = AbilityMessageCodes.MODE_SELECT_CITY;

                    if (!currentUser.getLocation().equals( mesText )) {
                        currentUser.setSubscription( true );
                        currentUser.setLocation( mesText );
                        users.saveToDB();
                    }

                    SendMessage subscriptionSuccess = getSendMessage( message, subscriptionSuccessText );
                    keyboardSettings( subscriptionSuccess );
                }
            }
        }
    }

    private SendMessage getSendMessage(Message message, String text) {
        return new SendMessage()
                .enableMarkdown( true )
                .setChatId( message.getChatId().toString() )
                .setText( text );
    }

    //getting weather result
    private void getWeather(Message message, String mesText, String language) {
        Thread thread = new Thread( () -> {
            WeatherAccess weatherAccess = new WeatherAccess();
            String result = weatherAccess.getWeatherString( mesText, language );

            if (result.equals( "error" )) {
                sendMsg( message, "Sorry, city not found. Try other city." +
                        "\n\nГород не найден. Введите другой." );
                System.out.println( "Нет такого города!" );
            } else {
                sendMsg( message, result );

                String forecastOptText = "Want to learn more on 5 days forecast? Try *forecast* button."
                        + "\n\nХочешь узнать прогноз на 5 дней? Нажми на кнопку *forecast*.";

                SendMessage cityInput = getSendMessage( message, forecastOptText );
                keyboardSettings( cityInput );
            }
        } );
        thread.start();
    }

    //getting forecast result
    private void getWeatherForecast(Message message, String mesText, String language) {
        Thread thread = new Thread( () -> {
            ForecastAccess forecastAccess = new ForecastAccess();
            String result = forecastAccess.getWeatherForecastString( mesText, language );

            if (result.equals( "error" )) {
                sendMsg( message, "Sorry, city not found. Try other city." +
                        "\n\nГород не найден. Введите другой." );
                System.out.println( "Нет такого города!" );
            } else {
                sendMsg( message, result );

                String moreCitiesText = "Want more? Go ahead and type other city!" +
                        "\n\nХочешь больше погоды? Введи другой город.";

                SendMessage forecast = getSendMessage( message, moreCitiesText );
                keyboardSettings( forecast );
            }
        } );
        thread.start();
    }

    //main keyboard
    private void keyboardSettings(SendMessage message) {
        ReplyKeyboardMarkup mainReplyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> mainKeyboard = new ArrayList<>();

        KeyboardRow mainRow1 = new KeyboardRow();
        mainRow1.add( "Current" );
        mainRow1.add( "Forecast" );
        mainRow1.add( "Language" );
        mainKeyboard.add( mainRow1 );

        KeyboardRow mainRow2 = new KeyboardRow();
        mainRow2.add( "Subscribe to daily Updates" );
        mainRow2.add( "Unsubscribe" );
        mainRow2.add( "Help" );
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

    //language keyboard
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
        SendMessage sendMessage = new SendMessage()
                .enableMarkdown( true )
                .setChatId( message.getChatId().toString() )
                .setReplyToMessageId( message.getMessageId() )
                .setText( text );
        try {
            execute( sendMessage );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotUsername() {
        return username;
    }

    public String getBotToken() {
        return token;
    }
}