import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class Offer {
    public static Map<Long, LinkedList<String>> seenOffers = new HashMap<>();

    // Store a limited number of offers in the offers history
    private static final int maxOffers = 1000;

    // Offer details
    private String title;
    private int price;
    private String location;
    private String url;
    private double squareMeters;
    private int additionalCosts;

    public Offer(String title, String price, String location, String additionalCosts, String squareMeters, String url, Long chatId) {
        setTitle(title);
        setPrice(price);
        setLocation(location);
        setUrl(url);
        setAdditionalCosts(additionalCosts);
        setSquareMeters(squareMeters);

        addOffer(chatId);
    }

    public void addOffer(Long chatId) {
        LinkedList offers;

        if (seenOffers.get(chatId) != null)
            offers = seenOffers.get(chatId);
        else {
            offers = new LinkedList<>();
            seenOffers.put(chatId, offers);
        }

        if (offers.size() > maxOffers)
            offers.pollFirst();

        offers.addLast(url);
    }

    public boolean isInteresting(BotSetup setup) {
        Integer minimalPrice = setup.getPriceFrom();
        Integer maximalPrice = setup.getPriceTo();

        boolean interested = true;

        if (minimalPrice != null)
            if (this.price < minimalPrice) interested = false;

        if (maximalPrice != null)
            if (this.price > maximalPrice) interested = false;

        return interested;
    }

    public static boolean checkIfSeen(String url, Long chatId) {
        if (seenOffers.get(chatId) == null) return false;

        if (seenOffers.get(chatId).contains(url)) return true;

        return false;
    }

    public static boolean isBlacklistedUrl(String url) {
        // Blacklist useless, promoted offers
        if (url.contains("otodom.pl")) return true;

        return false;
    }

    private double stringToDouble(String price) {
        double value = 0;

        try {
            value = Double.parseDouble(price.replaceAll("[^\\d,.]", "").replaceAll(",", "."));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return value;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.price = (int) stringToDouble(price);
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(String squareMeters) {
        this.squareMeters = stringToDouble(squareMeters);
    }

    public int getAdditionalCosts() {
        return additionalCosts;
    }

    public void setAdditionalCosts(String additionalCosts) {
        this.additionalCosts = (int) stringToDouble(additionalCosts);
    }

}
