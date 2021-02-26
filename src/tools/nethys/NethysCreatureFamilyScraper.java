package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.function.Predicate;

public class NethysCreatureFamilyScraper extends NethysListScraper {

    public static void main(String[] args) {
        new NethysCreatureFamilyScraper(
                "https://2e.aonprd.com/MonsterFamilies.aspx?Type=N",
                "generated/npc_families.pfdyl",
                s->s.equals("gamemastery_guide"),
                true
        );
    }

    public NethysCreatureFamilyScraper(String inputURL, String outputPath, Predicate<String> sourceValidator, boolean multithreaded) {
        super(inputURL, outputPath, "ctl00_MainContent_DetailedOutput", href->href.contains("MonsterFamilies.aspx?ID="), sourceValidator, multithreaded);
    }

    @Override
    Entry addItem(Document doc) {
        StringBuilder builder = new StringBuilder();
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");
        Pair<String, String> sourcePage = getSourcePage(output);
        String name = output.getElementsByClass("title").first().wholeText();
        if(name.equalsIgnoreCase("basilisk"))
            System.out.println("WEE");
        builder.append("<Family page=\"").append(sourcePage.second).append("\">\n\t<Name>")
                .append(name).append("</Name>\n\t<Description>");
        Node curr = output.getElementsByTag("br").first().nextSibling();
        while(curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h3"))) {
            builder.append(parseDesc(curr));
            curr = curr.nextSibling();
        }
        Element element = (curr != null) ? ((Element) curr).nextElementSibling() : null;
        while (element != null && !element.tagName().equals("h3"))
            element = element.nextElementSibling();
        curr = element;
        while(curr != null) {
            builder.append(parseDesc(curr));
            curr = curr.nextSibling();
        }
        builder.append("</Description>\n</Family>\n");
        return new Entry(name, builder.toString(), sourcePage.first);
    }
}
