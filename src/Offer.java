import java.util.LinkedList;

public abstract class Offer {
    private static LinkedList<String> seenOffers = new LinkedList<>();

    // Store a limited number of offers in the offers history
    private static final int maxOffers = 5000;

    // Offer details
    private String title;
    private int price;
    private String location;
    private String url;
    private double squareMeters;
    private int additionalCosts;

    public Offer(String title, String price, String location, String additionalCosts, String squareMeters, String url) {
        setTitle(title);
        setPrice(price);
        setLocation(location);
        setUrl(url);
        setAdditionalCosts(additionalCosts);
        setSquareMeters(squareMeters);

        if (seenOffers.size() > maxOffers)
            seenOffers.pollFirst();

        seenOffers.addLast(url);
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

    public static boolean checkIfSeen(String url) {
        if (seenOffers.contains(url)) return true;

        return false;
    }

    public static boolean isBlacklistedUrl(String url) {
        // Blacklist useless, promoted offers
        if (url.contains("otodom.pl")) return true;

        return false;
    }

    private double stringToDouble(String price) {
        double value = Double.parseDouble(price.replaceAll("[^\\d,.]", "").replaceAll(",", "."));

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
