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

        Credentials creds = Credentials.getInstance();
        this.token = creds.getBotToken();
        this.username = creds.getBotUsername();

        startAlerts();
    }

    private void startAlerts(){
        AlertsHandler alerts = new AlertsHandler(){
            @Override
            public void execute(SendMessage msg) {
                execute(msg);
            }
        };
        alerts.startAlertTimers();
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

        if (message.hasText()) {
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

            //basic mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_CITY )) {
                getWeather( message, mesText, currentUser.getLanguage() );

                return;
            }

            //language mode
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

            //forecast mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_FORECAST )) {
                getWeatherForecast( message, mesText, currentUser.getLanguage() );

                return;
            }

            //subscribe mode
            if (mode.equals( AbilityMessageCodes.MODE_SELECT_SUBSCRIBE )) {
                WeatherAccess weatherAccess = new WeatherAccess();
                String result = weatherAccess.getWeatherString( mesText, currentUser.getLanguage() );

                if (result.equals( "Sorry, city not found." )) {
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

    //getting weather result
    private void getWeather(Message message, String mesText, String language) {
        WeatherAccess weatherAccess = new WeatherAccess();
        String result = weatherAccess.getWeatherString( mesText, language );

        if (result.equals( "error" )) {
            sendMsg( message, "Sorry, city not found." );
            System.out.println( "Нет такого города!" );
        } else {
            sendMsg( message, result );
        }
    }

    //getting forecast result
    private void getWeatherForecast(Message message, String mesText, String language) {
        ForecastAccess forecastAccess = new ForecastAccess();
        String result = forecastAccess.getWeatherForecastString( mesText, language );

        if (result.equals( "error" )) {
            sendMsg( message, "Sorry, city not found." );
            System.out.println( "Нет такого города!" );
        } else {
            sendMsg( message, result );
        }
    }

    //main keyboard
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

    public String getBotUsername() {
        return username;
    }

    public String getBotToken() {
        return token;
    }
}
