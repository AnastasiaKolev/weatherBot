import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

public class MainClass {

    public static void main(String[] args) throws IOException {
        Credentials creds = Credentials.getInstance();
        creds.read();

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
