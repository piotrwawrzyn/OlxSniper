import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlxScanningBot extends ScanningBot {
    private static final String baseUrl = "https://www.olx.pl/nieruchomosci/mieszkania/";

    public OlxScanningBot(String BOT_TOKEN_SECRET, String BOT_USERNAME) {
        super(BOT_TOKEN_SECRET, BOT_USERNAME);
    }

    @Override
    public void removeChat(Long chatId) {
        super.removeChat(chatId);
        isAfterFirstScan.put(chatId, null);
    }

    @Override
    protected void scan(Long chatId, WebClient webClient) {
        BotSetup setup = setups.get(chatId);

        // Never scan for offers if the bot isn't fully set up
        if (!setup.isSetUp()) return;

        HtmlPage page = null;

        try {
            page = webClient.getPage(this.getScanningUrl(setup));
        } catch (Exception e) {
            e.printStackTrace();

            // Don't continue if there is an error while loading the page
            return;
        }

        // Get all offers
        List<HtmlElement> items = page.getByXPath("//table[@id='offers_table']/tbody/tr/td[contains(@class, 'offer') and not(contains(@class, 'promoted'))]/div[@class='offer-wrapper']/table/tbody");

        if (!items.isEmpty()) {
            for (HtmlElement item : items) {
                HtmlElement hrefElement = item.getFirstByXPath(".//tr[1]//td[1]/a");
                String url = hrefElement.getAttribute("href");

                if (Offer.checkIfSeen(url, chatId) == false && Offer.isBlacklistedUrl(url) == false) {
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

                    String squareMeters = "0";
                    String additionalCosts = "0";

                    if (squareMetersElement != null)
                        squareMeters = squareMetersElement.asText();

                    if (additionalCostsElement != null)
                        additionalCosts = additionalCostsElement.asText();

                    OlxOffer offer = new OlxOffer(title, price, location, additionalCosts, squareMeters, url, chatId);

                    // Check the offer meets the requirements
                    if (offer.isInteresting(setup)) {
                        if (isAfterFirstScan.get(chatId) != null) sendMessage(offer.toString(), chatId);
                    }
                }
            }
            isAfterFirstScan.put(chatId, true);
        }
    }

    @Override
    protected String getScanningUrl(BotSetup setup) {
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
