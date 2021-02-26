package tools.nethys;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NethysAncestryScraper extends NethysScraper {

    public static void main(String[] args) {
        new NethysAncestryScraper("https://2e.aonprd.com/Ancestries.aspx?ID=19", "generated/ancestry.txt");

    }

    private NethysAncestryScraper(String inputURL, String outputPath) {
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

            String hitPoints = getHeaderContents(detailedOutput, "Hit Points");
            String size = getHeaderContents(detailedOutput, "Size");
            String speed = getHeaderContents(detailedOutput, "Speed").split(" ")[0];
            List<String> boosts = getHeaderList(detailedOutput, "Ability Boosts");
            List<String> penalties = getHeaderList(detailedOutput, "Ability Flaw(s)");
            String languagesString = getHeaderContents(detailedOutput, "Languages");
            List<String> languages = Arrays.asList(languagesString.replaceAll("Additional languages.*", "").split(" *\n *"));
            String bonusLanguages = languagesString.replaceAll("((.|\n)*Choose from |, and any other.*)", "");

            List<String> senses = new ArrayList<>();
            if(!detailedOutput.getElementsMatchingOwnText("Darkvision").isEmpty())
                senses.add("Darkvision");
            if(!detailedOutput.getElementsMatchingOwnText("Darkvision").isEmpty())
                senses.add("Low-light Vision");

            out.write("<?xml version = \"1.0\"?>\n" +
                    "<pf2:ancestry xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                    "           xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "           xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/abc/ancestry.xsd\">\n" +
                    "    <Name>"+name.trim()+"</Name>\n" +
                    "    <HP>"+hitPoints.trim()+"</HP>\n" +
                    "    <Size>"+size.trim()+"</Size>\n" +
                    "    <Speed>"+speed.trim()+"</Speed>\n" +
                    "    <AbilityBonuses>"+String.join(", ", boosts)+"</AbilityBonuses>\n" +
                    "    <AbilityPenalties>"+String.join(", ", penalties)+"</AbilityPenalties>\n" +
                    "    <Languages>"+String.join(", ", languages)+"</Languages>\n" +
                    "    <BonusLanguages>"+bonusLanguages+"</BonusLanguages>\n" +
                    "    <Senses>"+String.join(", ", senses)+"</Senses>\n" +
                    "    <Description>"+description+"</Description>\n");
            out.write("\t<Feats>\n");
            String href = doc.getElementById("ctl00_MainContent_SubNavigation").getElementsByAttributeValueStarting("href", "Feats").attr("href");
            new NethysFeatListScraper("https://2e.aonprd.com/" + href, out, source->true);
            out.write("\t</Feats>\n</pf2:ancestry>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
