import credentials.Credentials;
import database.Users;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MainClass {

    public static void main(String[] args) {
        Credentials credentials = Credentials.getInstance();
        credentials.read();
        Users users = Users.getInstance();
        users.init();

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            TelegramBot bot = new TelegramBot();
            telegramBotsApi.registerBot( bot );
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
