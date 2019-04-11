import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    private final static String BOT_TOKEN_SECRET = "api_secret";
    private final static String BOT_USERNAME = "bot_username";

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        ScanningBot olxSniperBot = new OlxScanningBot(BOT_TOKEN_SECRET, BOT_USERNAME);

        try {
            botsApi.registerBot(olxSniperBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}