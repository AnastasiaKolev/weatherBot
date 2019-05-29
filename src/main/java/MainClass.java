import alerts.AlertsHandler;
import credentials.Credentials;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MainClass {

    public static void main(String[] args) {
        Credentials credentials = Credentials.getInstance();
        credentials.read();

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            TelegramBot bot = new TelegramBot();
            telegramBotsApi.registerBot( bot );

            startAlerts(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void startAlerts(TelegramBot bot) {
        AlertsHandler alerts = new AlertsHandler() {
            @Override
            public void executeAlert(SendMessage msg) {
                try {
                    bot.execute( msg );
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        };
        alerts.startAlertTimers();
    }
}
