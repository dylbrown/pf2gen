package tools.nethys;

import model.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class NethysClassScraper extends NethysScraper {

    public static void main(String[] args) {
        new NethysClassScraper("https://2e.aonprd.com/Classes.aspx?ID=14", "generated/class.txt");

    }

    private NethysClassScraper(String inputURL, String outputPath) {
        Document doc;
        BufferedWriter out;
        try  {
            doc = Jsoup.connect(inputURL).get();
            out = new BufferedWriter(new FileWriter(outputPath));

            Element detailedOutput = doc.getElementById("ctl00_MainContent_DetailedOutput");
            String name = detailedOutput.selectFirst(".title").wholeText();
            String description = "";
            for (Element child : detailedOutput.children()) {
                if(child.tagName().equals("i")) {
                    description = child.wholeText();
                    break;
                }
            }
            StringBuilder abilityChoices = new StringBuilder();
            Arrays.stream(detailedOutput.getElementsContainingOwnText("Key Ability: ").first().wholeText().replaceAll("Key Ability: ", "").split(" OR ")).forEach(s->abilityChoices.append("    <AbilityChoices>").append(StringUtils.camelCaseWord(s.substring(0, 3))).append("</AbilityChoices>\n"));

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

            out.write("<?xml version = \"1.0\"?>\n" +
                    "<pf2:class xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                    "           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "           xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/abc/class.xsd\">\n" +
                    "    <Name>" + name + "</Name>\n" +
                    "    <HP>" + hitPoints + "</HP>\n" +
                    abilityChoices +
                    "    <SkillIncreases>" + skillIncreases + "</SkillIncreases>\n" +
                    "    <Description>" + description + "</Description>\n");
            new NethysClassTableScraper(inputURL, out, 1);

            out.write("\t<Feats>\n");
            String href = doc.getElementById("ctl00_MainContent_SubNavigation").getElementsByAttributeValueStarting("href", "Feats").attr("href");

            new NethysFeatListScraper("https://2e.aonprd.com/" + href, s-> {
                try {
                    out.write(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, source->true);
            out.write("\t</Feats>\n</pf2:class>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
