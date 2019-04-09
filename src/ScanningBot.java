import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ScanningBot extends TelegramLongPollingBot {
    protected String BOT_TOKEN_SECRET;
    protected String BOT_USERNAME;
    protected String CHAT_ID;

    protected WebClient webClient;
    protected ScanningLoop scanningRunnable;
    protected Thread scanningThread;
    protected BotSetup setup;

    public ScanningBot(String BOT_TOKEN_SECRET, String BOT_USERNAME, String CHAT_ID) {
        this.BOT_TOKEN_SECRET = BOT_TOKEN_SECRET;
        this.BOT_USERNAME = BOT_USERNAME;
        this.CHAT_ID = CHAT_ID;

        // Set up WebClient
        webClient = new WebClient(BrowserVersion.FIREFOX_60);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);

        // Initialize bot setup
        setup = new BotSetup();

        // Initialize Scanning Loop
        scanningRunnable = new ScanningLoop();
        scanningThread = new Thread(scanningRunnable);
        scanningThread.start();
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN_SECRET;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText().toLowerCase();
            String[] messageSplit = message.split(" ");
            String command = messageSplit[0];

            if (isCommand(command, "/stop")) {
                if (scanningRunnable.isRunning()) {
                    scanningRunnable.pause();
                    sendMessage("Scanner stopped.");
                } else {
                    sendMessage("Scanner has already been stopped.");
                }
            }

            if (isCommand(command, "/start")) {
                if (setup.isSetUp() == false)
                    sendMessage("Please set up your city first with <b>/setup city [city]</b>.");
                else if (scanningRunnable.isRunning())
                    sendMessage("Scanner is already running!");
                else {
                    scanningRunnable.unpause();
                    sendMessage("Scanner has started.");
                }
            }

            if (isCommand(command, "/setup")) {
                if (messageSplit.length < 3) {
                    sendMessage("Invalid syntax. Please use <b>/setup [type] [value]</b>. Eg. /setup city Warsaw. Get a list of all possible commands with <b>/help</b>.");
                    return;
                }

                String commandType = messageSplit[1];
                String commandArgument = String.join(" ", Arrays.copyOfRange(messageSplit, 2, messageSplit.length));

                // Normalize argument
                String normalizedCommandArgument = StringUtils.stripAccents(commandArgument).replaceAll(" ", "-");

                switch (commandType) {
                    case "city": {
                        String city = StringUtils.capitalize(commandArgument);

                        if (!this.validateCity(normalizedCommandArgument))
                            sendMessage("The city \"" + city + "\" does not exist or is not supported.");
                        else {
                            setup.setCity(normalizedCommandArgument);
                            sendMessage("The city has been successfuly changed to " + city + ".");
                        }
                        break;
                    }

                    case "pricefrom": {
                        String priceFrom = messageSplit[2];
                        if (priceFrom.equals("-") || priceFrom == null) {
                            setup.setPriceFrom(null);
                            sendMessage("Minimal price filter successfuly removed.");
                        } else if (messageSplit.length != 3 || !StringUtils.isNumeric(priceFrom))
                            sendMessage("Invalid argument. Please follow this example: <b>/setup priceFrom 500</b> in order to set a minimal price filter on 500zł.");
                        else {
                            setup.setPriceFrom(Integer.parseInt(priceFrom));
                            sendMessage("Minimal price filter successfuly set on " + priceFrom + "zł.");
                        }
                        break;
                    }

                    case "priceto": {
                        String priceTo = messageSplit[2];
                        if (priceTo.equals("-") || priceTo == null) {
                            setup.setPriceTo(null);
                            sendMessage("Maximum price filter successfully removed.");
                        } else if (messageSplit.length != 3 || !StringUtils.isNumeric(priceTo))
                            sendMessage("Invalid argument. Please follow this example: <b>/setup priceTo 2000</b> in order to set a maximum price filter on 2000zł.");
                        else {
                            setup.setPriceTo(Integer.parseInt(priceTo));
                            sendMessage("Maximum price filter successfully set on " + priceTo + "zł.");
                        }
                        break;
                    }

                    default: {
                        sendMessage("Word \"" + commandType + "\" is not a valid argument for command + ");
                        break;
                    }
                }
            }

            if (isCommand(command, "/say")) {
                // Additional command - say something as a bot
                deleteMessage(update.getMessage().getMessageId());
                String messageToSend = String.join(" ", Arrays.copyOfRange(messageSplit, 1, messageSplit.length));
                sendMessage(messageToSend);
            }

            if (isCommand(command, "/help")) {
                List<String> commands = new ArrayList<>();
                commands.add("<b>/start</b> - Starts the infinite scanning loop");
                commands.add("<b>/stop</b> - Stops the infinite scanning loop");
                commands.add("<b>/setup city [city]</b> - Changes the destination city");
                commands.add("<b>/setup priceFrom [price]</b> - Sets up minimum price filter");
                commands.add("<b>/setup priceTo [price]</b> - Sets up maximum price filter");
                commands.add("<b>/say [message]</b> - Say something as a bot");
                commands.add("<b>/help</b> - Retrieves a list of all possible commands");

                StringBuilder messageBuilder = new StringBuilder();

                for (String commandDescription : commands) {
                    messageBuilder.append(commandDescription + "\n");
                }
                sendMessage(messageBuilder.toString());
            }
        }
    }

    public void deleteMessage(int messageId) {
        DeleteMessage dm = new DeleteMessage();
        dm.setChatId(CHAT_ID);
        dm.setMessageId(messageId);

        try {
            execute(dm);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String text) {
        SendMessage message = new SendMessage();
        message.setChatId(this.CHAT_ID);
        message.setParseMode("HTML");
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean isCommand(String commandGiven, String command) {
        return commandGiven.equals(command) || commandGiven.equals(command + "@" + BOT_USERNAME.toLowerCase());
    }

    protected abstract void scan(ScanningLoop scanningRunnable);

    protected abstract String getScanningUrl();

    protected abstract boolean validateCity(String city);

    public class ScanningLoop implements Runnable {
        private int scanDelaySeconds = 15;
        private volatile boolean paused = true;

        public boolean isRunning() {
            return !this.paused;
        }

        public void pause() {
            this.paused = true;
        }

        public void unpause() {
            this.paused = false;
        }


        public void run() {
            while (true) {
                while (!paused) {
                    scan(this);

                    try {
                        Thread.sleep(1000 * scanDelaySeconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Clear cache of webclient after each scan to avoid caching errors

                    webClient.getCache().clear();
                }

                // Wait a bit before checking if the scanning loop has been unpaused

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}




