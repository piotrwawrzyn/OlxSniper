public class OlxOffer extends Offer {

    public OlxOffer(String title, String price, String location, String additionalCosts, String squareMeters, String url, Long chatId) {
        super(title, price, location, additionalCosts, squareMeters, url, chatId);
    }

    public String toString() {
        return String.format("<b>%s</b>\n[%d zł. + %d zł.] [%s] [%.0f m²]\n\n<a href=\"%s\">ZOBACZ OFERTĘ</a>", this.getTitle().toUpperCase(), this.getPrice(), this.getAdditionalCosts(), this.getLocation(), this.getSquareMeters(), this.getUrl());
    }
}
