import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;

public abstract class ScanningBot extends TelegramLongPollingBot {
    protected String BOT_TOKEN_SECRET;
    protected String BOT_USERNAME;

    protected List<Long> allChats;
    protected Map<Long, BotSetup> setups;
    protected Map<Long, Thread> loops;
    protected Map<Long, Boolean> isAfterFirstScan = new HashMap<>();


    public ScanningBot(String BOT_TOKEN_SECRET, String BOT_USERNAME) {
        this.BOT_TOKEN_SECRET = BOT_TOKEN_SECRET;
        this.BOT_USERNAME = BOT_USERNAME;

        // Initialize bot setups
        setups = new HashMap<>();

        // Initialize loops map
        loops = new HashMap<>();

        // Initialize all chats list
        allChats = new ArrayList<>();
    }

    public void addChat(Long chatId) {
        // Initialize new chat to cover
        allChats.add(chatId);
        setups.put(chatId, new BotSetup());
        ScanningLoop scanningRunnable = new ScanningLoop(chatId);
        Thread scanningThread = new Thread(scanningRunnable);
        loops.put(chatId, scanningThread);
        scanningThread.start();
    }

    public void stopScanning(Long chatId) {
        // Stop the scanner
        loops.get(chatId).interrupt();
    }

    public void removeChat(Long chatId) {
        // Clear all the information about the chat
        allChats.remove(chatId);
        loops.put(chatId, null);
        setups.put(chatId, null);
        Offer.seenOffers.put(chatId, null);
        isAfterFirstScan.put(chatId, null);
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
            Long chatId = update.getMessage().getChatId();
            if (!allChats.contains(chatId)) addChat(chatId);

            String message = update.getMessage().getText().toLowerCase();
            String[] messageSplit = message.split(" ");
            String command = messageSplit[0];

            BotSetup setup = setups.get(chatId);

            if (isCommand(command, "/start")) {
                if (setup.isSetUp() == false)
                    sendMessage("Please set up your city first with <b>/setup city [city]</b>.", chatId);
                else if (setup.isStarted())
                    sendMessage("Scanner is already running!", chatId);
                else {
                    setup.setStarted(true);
                    sendMessage("Scanner has started.", chatId);
                }
            }

            if (isCommand(command, "/stop")) {
                if (setup.isStarted()) {
                    setup.setStarted(false);
                    sendMessage("Scanner stopped.", chatId);
                } else {
                    sendMessage("Scanner has already been stopped.", chatId);
                }
            }

            if (isCommand(command, "/setup")) {
                if (messageSplit.length < 3) {
                    sendMessage("Invalid syntax. Please use <b>/setup [type] [value]</b>. Eg. /setup city Warsaw. Get a list of all possible commands with <b>/help</b>.", chatId);
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
                            sendMessage("The city \"" + city + "\" does not exist or is not supported.", chatId);
                        else {
                            setup.setCity(normalizedCommandArgument);
                            sendMessage("The city has been successfuly changed to " + city + ".", chatId);
                        }
                        break;
                    }

                    case "pricefrom": {
                        String priceFrom = messageSplit[2];
                        if (priceFrom.equals("-") || priceFrom == null) {
                            setup.setPriceFrom(null);
                            sendMessage("Minimal price filter successfuly removed.", chatId);
                        } else if (messageSplit.length != 3 || !StringUtils.isNumeric(priceFrom))
                            sendMessage("Invalid argument. Please follow this example: <b>/setup priceFrom 500</b> in order to set a minimal price filter on 500zł.", chatId);
                        else {
                            setup.setPriceFrom(Integer.parseInt(priceFrom));
                            sendMessage("Minimal price filter successfuly set on " + priceFrom + "zł.", chatId);
                        }
                        break;
                    }

                    case "priceto": {
                        String priceTo = messageSplit[2];
                        if (priceTo.equals("-") || priceTo == null) {
                            setup.setPriceTo(null);
                            sendMessage("Maximum price filter successfully removed.", chatId);
                        } else if (messageSplit.length != 3 || !StringUtils.isNumeric(priceTo))
                            sendMessage("Invalid argument. Please follow this example: <b>/setup priceTo 2000</b> in order to set a maximum price filter on 2000zł.", chatId);
                        else {
                            setup.setPriceTo(Integer.parseInt(priceTo));
                            sendMessage("Maximum price filter successfully set on " + priceTo + "zł.", chatId);
                        }
                        break;
                    }

                    default: {
                        sendMessage("Word \"" + commandType + "\" is not a valid argument for command + ", chatId);
                        break;
                    }
                }
            }

            if (isCommand(command, "/say")) {
                // Additional command - say something as a bot
                deleteMessage(update.getMessage().getMessageId(), chatId);
                String messageToSend = String.join(" ", Arrays.copyOfRange(messageSplit, 1, messageSplit.length));
                sendMessage(messageToSend, chatId);
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
                sendMessage(messageBuilder.toString(), chatId);
            }
        }
    }

    public void deleteMessage(int messageId, Long chatId) {
        DeleteMessage dm = new DeleteMessage();
        dm.setChatId(chatId);
        dm.setMessageId(messageId);

        try {
            execute(dm);
        } catch (TelegramApiException e) {
            System.out.println("Not enough rights to delete the message or out of group.");
        }
    }

    public void sendMessage(String text, Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode("HTML");
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Not enough rights to send the message or out of group.");
            stopScanning(chatId);
        }
    }

    public boolean isCommand(String commandGiven, String command) {
        return commandGiven.equals(command) || commandGiven.equals(command + "@" + BOT_USERNAME.toLowerCase());
    }

    protected abstract void scan(Long chatId, WebClient webClient);

    protected abstract String getScanningUrl(BotSetup setup);

    protected abstract boolean validateCity(String city);

    public class ScanningLoop implements Runnable {
        private int scanDelaySeconds = 5;
        private Long chatId;
        protected WebClient webClient;
        volatile boolean isRunning = true;

        public ScanningLoop(Long chatId) {
            this.chatId = chatId;
        }

        public void run() {
            BotSetup setup;
            p:while (isRunning) {
                while (setups.get(chatId) != null && setups.get(chatId).isStarted()) {

                    // Set up WebClient
                    webClient = new WebClient(BrowserVersion.FIREFOX_52);
                    webClient.getOptions().setCssEnabled(false);
                    webClient.getOptions().setJavaScriptEnabled(false);
                    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                    webClient.getOptions().setThrowExceptionOnScriptError(false);

                    setup = setups.get(chatId);

                    if (setup.isSetUp() && setup.isStarted())
                        scan(chatId, webClient);

                    try {
                        Thread.sleep(1000 * scanDelaySeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break p;
                    }

                }

                // Wait a bit before checking if bot has been started
                try {
                    Thread.sleep(1000 * 20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break p;
                }
            }

            removeChat(chatId);
        }
    }

}




