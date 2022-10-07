package tools.nethys;

import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NethysAncestryScraper extends NethysSourceScraper {

    public static void main(String[] args) {
        new NethysAncestryScraper("https://2e.aonprd.com/Sources.aspx?ID=12", "Character Guide");
    }

    public NethysAncestryScraper(String inputURL, String source) {
        super(inputURL, source, href->href.startsWith("Ancestries"));
    }

    protected NethysListScraper.Entry addItem(Document doc) {
        StringBuilder item = new StringBuilder();

        Element detailedOutput = doc.getElementById("main");
        String name = detailedOutput.selectFirst(".title").wholeText();
        if(name.contains("Versatile"))
            return null;
        String description = "";
        Element descElem = detailedOutput.selectFirst(
                "#ctl00_RadDrawer1_Content_MainContent_DetailedOutput > i");
        if(descElem != null) {
            description = descElem.wholeText();
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
        if(!detailedOutput.getElementsMatchingOwnText("Low-Light Vision").isEmpty())
            senses.add("Low-light Vision");

        item.append("<?xml version = \"1.0\"?>\n" +
                "<pf2:ancestry xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                "\t\t   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "\t\t   xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/abc/ancestry.xsd\">\n" +
                "\t<Name>")
                .append(name.trim()).append("</Name>\n")
                .append("\t<HP>").append(hitPoints.trim()).append("</HP>\n")
                .append("\t<Size>").append(size.trim()).append("</Size>\n")
                .append("\t<Speed>").append(speed.trim()).append("</Speed>\n")
                .append("\t<AbilityBonuses>").append(String.join(", ", boosts)).append("</AbilityBonuses>\n");
        if(!penalties.isEmpty()) {
            item.append("\t<AbilityPenalties>").append(String.join(", ", penalties))
                    .append("</AbilityPenalties>\n");
        }
        item.append("\t<Languages>").append(String.join(", ", languages)).append("</Languages>\n")
                .append("\t<BonusLanguages>").append(bonusLanguages).append("</BonusLanguages>\n").append("    <Senses>").append(String.join(", ", senses)).append("</Senses>\n")
                .append("\t<Description>").append(description).append("</Description>\n");
        item.append("\t<Feats>\n");
        String href = doc.getElementById("ctl00_RadDrawer1_Content_MainContent_SubNavigation").getElementsByAttributeValueStarting("href", "Feats").attr("href");
        new NethysFeatListScraper("https://2e.aonprd.com/" + href, item::append, source->true);
        item.append("\t</Feats>\n</pf2:ancestry>");

        return new Entry(name, item.toString(), source);
    }

    @Override
    protected String nameToFileName(String name) {
        return "generated/" + source + "/ancestries/" + StringUtils.capitalize(name) + ".pfdyl";
    }
}
