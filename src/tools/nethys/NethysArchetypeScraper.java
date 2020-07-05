package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;

public class NethysArchetypeScraper extends NethysListScraper {
    public static void main(String[] args) {
        new NethysArchetypeScraper("https://2e.aonprd.com/Archetypes.aspx", "generated/archetypes.pfdyl");
    }

    public NethysArchetypeScraper(String inputURL, String outputPath) {
        super(inputURL, outputPath, "ctl00_MainContent_DetailedOutput",
                href->href.contains("Archetypes") && href.contains("ID"));
    }

    @Override
    Pair<String, String> addItem(Document doc) {
        return null;
    }
}
