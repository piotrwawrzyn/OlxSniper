import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.List;

public class OlxScanningBot extends ScanningBot {
    private static final String baseUrl = "https://www.olx.pl/nieruchomosci/mieszkania/";

    public OlxScanningBot(String BOT_TOKEN_SECRET, String BOT_USERNAME, String CHAT_ID) {
        super(BOT_TOKEN_SECRET, BOT_USERNAME, CHAT_ID);
    }

    @Override
    protected void scan(ScanningLoop scanningRunnable) {
        // Never scan for offers if the bot isn't fully set up
        if (!setup.isSetUp()) return;

        HtmlPage page = null;

        try {
            page = webClient.getPage(this.getScanningUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get all offers
        List<HtmlElement> items = page.getByXPath("//table[@id='offers_table']/tbody/tr/td[contains(@class, 'offer') and not(contains(@class, 'promoted'))]/div[@class='offer-wrapper']/table/tbody");

        if (!items.isEmpty()) {
            for (HtmlElement item : items) {
                HtmlElement hrefElement = item.getFirstByXPath(".//tr[1]//td[1]/a");
                String url = hrefElement.getAttribute("href");

                if (Offer.checkIfSeen(url) == false && Offer.isBlacklistedUrl(url) == false) {
                    // Scrap information from offers-list site
                    HtmlElement titleElement = item.getFirstByXPath(".//tr[1]//td[2]/div/h3");
                    HtmlElement priceElement = item.getFirstByXPath(".//tr[1]//td[3]/div/p");
                    HtmlElement locationElement = item.getFirstByXPath(".//tr[2]//td[1]/div/p/small/span");

                    String title = titleElement.asText();
                    String price = priceElement.asText();
                    String location = locationElement.asText();

                    // Scrap information from a specific offer's page
                    try {
                        page = webClient.getPage(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    HtmlElement squareMetersElement = page.getFirstByXPath("//*[contains(text(),'Powierzchnia')]/following-sibling::td[contains(@class, 'value')]");
                    HtmlElement additionalCostsElement = page.getFirstByXPath("//*[contains(text(),'Czynsz (dodatkowo)')]/following-sibling::td[contains(@class, 'value')]");

                    String squareMeters = squareMetersElement.asText();
                    String additionalCosts = additionalCostsElement.asText();

                    OlxOffer offer = new OlxOffer(title, price, location, additionalCosts, squareMeters, url);

                    // Check the offer meets the requirements
                    if (offer.isInteresting(setup))
                        sendMessage(offer.toString());
                }
            }

        }
    }

    @Override
    protected String getScanningUrl() {
        StringBuilder urlBuilder = new StringBuilder(baseUrl + setup.getCategory() + "/" + setup.getCity() + "/" + "?search");
        if (setup.getPriceFrom() != null)
            urlBuilder.append("&search%5Bfilter_float_price%3Afrom%5D=" + setup.getPriceFrom());
        if (setup.getPriceTo() != null) urlBuilder.append("&search%5Bfilter_float_price%3Ato%5D=" + setup.getPriceTo());

        return urlBuilder.toString();
    }

    @Override
    protected boolean validateCity(String city) {
        // Ping the page to check if it exists
        String url = baseUrl + city;
        return BotSetup.getUrlStatus(url) == 200;
    }

}
