import java.net.HttpURLConnection;
import java.net.URL;

class BotSetup {
    private final String category = "wynajem"; // Uncustomizable for now
    private String city;
    private Integer priceFrom;
    private Integer priceTo;

    public boolean isSetUp() {
        return city != null;
    }

    public static int getUrlStatus(String url) {
        int code = 404;

        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();
            code = connection.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();

        }

        return code;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getPriceFrom() {
        return this.priceFrom;
    }

    public void setPriceFrom(Integer priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Integer getPriceTo() {
        return this.priceTo;
    }

    public void setPriceTo(Integer priceTo) {
        this.priceTo = priceTo;
    }

    public String getCategory() {
        return this.category;
    }
}

