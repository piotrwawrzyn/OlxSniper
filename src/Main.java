import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class Main {
    private final static String BOT_TOKEN_SECRET = "api_secret";
    private final static String BOT_USERNAME = "bot_username";
    private final static String CHAT_ID = "chat_id";

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();

        ScanningBot olxSniperBot = new OlxScanningBot(BOT_TOKEN_SECRET, BOT_USERNAME, CHAT_ID);

        try {
            botsApi.registerBot(olxSniperBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
