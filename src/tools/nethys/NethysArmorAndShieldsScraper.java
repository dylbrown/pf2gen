package tools.nethys;

import model.util.StringUtils;
import model.util.Triple;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class NethysArmorAndShieldsScraper extends NethysWeaponsArmorScraper {

    private final Map<String, String> categoryMap = new ConcurrentHashMap<>();
    private final int rowNumber;

    public static void main(String[] args) {
        List<Triple<String, String, Integer>> tables = Arrays.asList(
                new Triple<>("https://2e.aonprd.com/Armor.aspx", "Armor.aspx?ID=", 1),
                new Triple<>("https://2e.aonprd.com/Shields.aspx", "Shields.aspx?ID=", -1)
        );
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter("generated/armorAndShields.txt"), 32768);
            out.write("<?xml version = \"1.0\"?>\n" +
                    "<pf2:armorAndShields xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                    "             xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "             xsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/armorAndShields.xsd\">\n");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (Triple<String, String, Integer> table : tables) {
            new NethysArmorAndShieldsScraper(table.first, out, "ctl00_MainContent_TreasureElement", href->href.contains(table.second), s->s.equalsIgnoreCase("core_rulebook"), table.third);
        }
        try {
            out.write("</pf2:armorAndShields>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NethysArmorAndShieldsScraper(String inputURL, Writer output, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator, int rowNumber) {
        super(false);
        this.rowNumber = rowNumber;
        parseList(inputURL, container, hrefValidator, e -> true);
        printOutput(output, sourceValidator);
    }

    @Override
    CategoryEntry addItem(Document doc) {
        Element output = doc.getElementById("main");
        String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
        String source = sourceAndPage.replaceAll(" pg.*", "");

        String name = output.select(".title > a[href*=\"?ID=\"]").first().wholeText();

        String price = getAfter(output, "Price");
        if(price.equalsIgnoreCase("—") || price.equalsIgnoreCase("0"))
            price = "0 sp";
        String acBonus = getAfter(output, "AC Bonus");
        String dexCap = getAfter(output, "Dex Cap");
        String checkPenalty = getAfter(output, "Check Penalty");
        String speedPenalty = getAfter(output, "Speed Penalty").replaceAll(" *ft\\.", "");
        String strength = getAfter(output, "Strength");
        String hardness = getAfter(output, "Hardness");
        String hpBt = getAfter(output, "HP \\(BT\\)");
        String hp = (hpBt.isBlank()) ? "" : hpBt.substring(0, hpBt.indexOf(' '));
        String bt = (hpBt.isBlank()) ? "" : hpBt.substring(hpBt.indexOf("(") + 1, hpBt.indexOf(")"));
        String bulk = getAfter(output, "Bulk");
        if(bulk.equalsIgnoreCase("—"))
            bulk = "0";
        String group = getAfter(output, "Group");
        String traits = getAfter(output, "Traits");
        if(traits.contains("—"))
            traits = "";

        Node afterHr = output.select("#ctl00_MainContent_DetailedOutput hr").first().nextSibling();
        StringBuilder description = new StringBuilder();
        while(afterHr != null) {
            description.append(parseDesc(afterHr));
            afterHr = afterHr.nextSibling();
            if(afterHr instanceof Element && Arrays.asList("hr", "h2")
                    .contains(((Element) afterHr).tagName()))
                break;
        }

        StringBuilder result = new StringBuilder();

        result.append("<Armor>\n\t<Name>")
                .append(name).append("</Name>\n")
                .append("\t<Price>").append(price).append("</Price>\n");
        checkAppend(result, "AC", acBonus);
        checkAppend(result, "MaxDex", dexCap);
        checkAppend(result, "ACP", checkPenalty);
        checkAppend(result, "SpeedPenalty", speedPenalty);
        checkAppend(result, "Strength", strength);
        checkAppend(result, "Hardness", hardness);
        checkAppend(result, "HP", hp);
        checkAppend(result, "BT", bt);
        result.append("\t<Bulk>").append(bulk).append("</Bulk>\n");
        checkAppend(result, "Group", group);
        checkAppend(result, "Traits", traits);
        result.append("\t<Description>").append(description).append("</Description>\n")
                .append("</Armor>\n");

        return new CategoryEntry(name, result.toString(), source, categoryMap.getOrDefault(StringUtils.clean(name), "Shield"));
    }

    private void checkAppend(StringBuilder result, String label, String value) {
        if(!value.isBlank() && !value.equals("—"))
            result.append("\t<").append(label).append(">")
                    .append(value)
                    .append("</").append(label).append(">\n");
    }

    @Override
    protected void setupItem(String href, Element row) throws IOException {
        if(rowNumber != -1) {
            categoryMap.put(StringUtils.clean(row.children().get(0).wholeText()),
                    row.children().get(rowNumber).wholeText());
        }
        super.setupItem(href, row);
    }

    @Override
    protected List<String> getProficiencies() {
        return Arrays.asList("Unarmored", "Light", "Medium", "Heavy", "Shield");
    }
}
