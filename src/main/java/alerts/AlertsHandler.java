package alerts;

import database.User;
import database.Users;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import weather.EmojiService;
import weather.ForecastAccess;
import weather.ForecastData;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class AlertsHandler {
    private Users users;
    private EmojiService emoji = new EmojiService();
    private ForecastAccess forecastAccess = new ForecastAccess();

    protected AlertsHandler() {
        super();

        users = Users.getInstance();
    }

    public void startAlertTimers() {
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

            System.out.println( "Alert for " + sub.getFirstName() + " location = " + sub.getLocation() + " lang = " + sub.getLanguage() );

            ForecastData forecastData = forecastAccess.forecastSearch(sub.getLocation(), sub.getLanguage());
            String result = forecastAccess.formatWeatherForecast(forecastData);
            String eAlert = emoji.getEmojiForWeather( "alert" );

            SendMessage sendMessage = new SendMessage()
                    .enableMarkdown( true )
                    .setChatId( String.valueOf( sub.getUserId() ) )
                    .setText( eAlert + " Subscription alert:\n\n" + result );

            executeAlert( sendMessage );
        }
    }

    public void executeAlert(SendMessage msg) {

    }
}
