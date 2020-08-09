package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.function.Predicate;

public class NethysCreatureFamilyScraper extends NethysListScraper {

    public static void main(String[] args) {
        new NethysCreatureFamilyScraper(
                "https://2e.aonprd.com/MonsterFamilies.aspx?Type=M",
                "generated/creature_families.pfdyl",
                s->s.equals("bestiary"),
                true
        );
    }

    public NethysCreatureFamilyScraper(String inputURL, String outputPath, Predicate<String> sourceValidator, boolean multithreaded) {
        super(inputURL, outputPath, "ctl00_MainContent_DetailedOutput", href->href.contains("MonsterFamilies.aspx?ID="), sourceValidator, multithreaded);
    }

    @Override
    Pair<String, String> addItem(Document doc) {
        StringBuilder builder = new StringBuilder();
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");
        Pair<String, String> sourcePage = getSourcePage(output);
        String name = output.getElementsByClass("title").first().wholeText();
        builder.append("<Family page=\"").append(sourcePage.second).append("\">\n\t<Name>")
                .append(name).append("</Name>\n\t<Description>");
        Node curr = output.getElementsByTag("br").first().nextSibling();
        while(!(curr instanceof Element && ((Element) curr).tagName().equals("h3"))) {
            builder.append(parseDesc(curr));
            curr = curr.nextSibling();
        }
        Element element = ((Element) curr).nextElementSibling();
        while (!element.tagName().equals("h3"))
            element = element.nextElementSibling();
        curr = element;
        while(curr != null) {
            builder.append(parseDesc(curr));
            curr = curr.nextSibling();
        }
        builder.append("</Description>\n</Family>\n");
        return new Pair<>(builder.toString(), sourcePage.first);
    }
}
