package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class NethysDomainScraper extends NethysListScraper {

    public static void main(String[] args) {
        new NethysDomainScraper("https://2e.aonprd.com/Domains.aspx", "generated/domains.pfdyl");
    }

    private NethysDomainScraper(String inputURL, String outputPath) {
        super(inputURL, outputPath, "ctl00_MainContent_DomainElement", href -> href.contains("ID") && href.contains("Domains"));
    }

    @Override
    Pair<String, String> addItem(Document doc) {
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");
        String domainName = output.getElementsByTag("h1")
                .first().text().replaceAll(" Domain", "").trim();
        String sourceAndPage = output.getElementsMatchingText("\\ASource\\z")
                .first().nextElementSibling().text().replaceAll(";", "");
        String source = sourceAndPage.replaceAll(" pg.*", "");
        String pageNo = sourceAndPage.replaceAll(".*pg\\. ", "");
        String domainSpell = output.getElementsMatchingText("\\ADomain Spell\\z")
                .first().nextElementSibling().text().replaceAll(";", "").trim();
        String advDomainSpell = output.getElementsMatchingText("\\AAdvanced Domain Spell\\z")
                .first().nextElementSibling().text().replaceAll(";", "").trim();
        Node descNode = output.getElementsByTag("br").last().nextSibling();

        String desc = (descNode instanceof TextNode) ? ((TextNode) descNode).getWholeText().trim() : "";
        String results = String.format("<Domain page=\"%s\">\n" +
                        "\t<Name>%s</Name>\n" +
                        "\t<DomainSpell>%s</DomainSpell>\n" +
                        "\t<AdvancedDomainSpell>%s</AdvancedDomainSpell>\n" +
                        "\t<Description>%s</Description>\n" +
                        "</Domain>\n",
                pageNo, domainName, domainSpell, advDomainSpell, desc);

        return new Pair<>(results, source);
    }
}
