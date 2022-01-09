package tools.nethys;

import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Arrays;
import java.util.function.Predicate;

public class NethysClassScraper extends NethysSourceScraper {

    public static void main(String[] args) {
        new NethysClassScraper(
                "https://2e.aonprd.com/Sources.aspx?ID=96",
                "Secrets of Magic",
                href->href.startsWith("Classes"));

    }

    public NethysClassScraper(String inputURL, String source, Predicate<String> hrefValidator) {
        super(inputURL, source, hrefValidator);
    }

    @Override
    protected Entry addItem(Document doc) {
        StringBuilder out = new StringBuilder();

        Element detailedOutput = doc.getElementById("main");
        String name = detailedOutput.selectFirst(".title").wholeText();
        String description = "";
        for (Element child : detailedOutput.children()) {
            if(child.tagName().equals("i")) {
                description = child.wholeText();
                break;
            }
        }
        StringBuilder abilityChoices = new StringBuilder();
            Arrays.stream(detailedOutput.getElementsContainingOwnText("Key Ability: ")
                .first()
                .wholeText()
                .replaceAll("Key Ability: ", "")
                .split(" OR "))
                .forEach(s->
                        abilityChoices.append("    <AbilityChoices>")
                                .append(StringUtils.camelCaseWord(s.substring(0, 3)))
                                .append("</AbilityChoices>\n")
                );

        String hitPoints = detailedOutput.getElementsContainingOwnText("Hit Points: ").first().wholeText().substring("Hit Points: ".length());
        hitPoints = hitPoints.substring(0, hitPoints.indexOf(" "));

        Element skills = detailedOutput.getElementsMatchingOwnText("Skills").first();
        Node curr = skills.nextSibling();
        String skillIncreases = "";
        while(curr != null && !(curr instanceof Element && ((Element) curr).tagName().equals("h2"))) {
            if(curr instanceof TextNode) {
                if(((TextNode) curr).text().trim().startsWith("Trained in a number of additional skills")) {
                    skillIncreases = ((TextNode) curr).text().replaceAll("[^\\d]", "");
                    break;
                }
            }
            curr = curr.nextSibling();
        }

        out.append("<?xml version = \"1.0\"?>\n" +
                "<pf2:class xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                "           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "           xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/abc/class.xsd\">\n" +
                "    <Name>").append(name).append("</Name>\n")
                .append("    <HP>").append(hitPoints).append("</HP>\n")
                .append(abilityChoices)
                .append("    <SkillIncreases>").append(skillIncreases).append("</SkillIncreases>\n")
                .append("    <Description>").append(description).append("</Description>\n");
        new NethysClassTableScraper(doc, out, 1);

        out.append("\t<Feats>\n");
        String href = doc.getElementById("ctl00_RadDrawer1_Content_MainContent_SubNavigation").getElementsByAttributeValueStarting("href", "Feats").attr("href");

        new NethysFeatListScraper("https://2e.aonprd.com/" + href, out::append, source->true, true);
        out.append("\t</Feats>\n</pf2:class>");
        return new Entry(name, out.toString(), source);
    }
}
