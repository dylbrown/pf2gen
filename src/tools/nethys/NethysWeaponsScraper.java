package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NethysWeaponsScraper extends NethysWeaponsArmorScraper {

    public static void main(String[] args) {
        List<Pair<String, String>> tables = Arrays.asList(
                new Pair<>("ctl00_MainContent_MeleeElement", "Melee"),
                new Pair<>("ctl00_MainContent_RangedElement", "Ranged")
        );
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter("generated/weapons.txt"), 32768);
            out.write("<?xml version = \"1.0\"?>\n" +
                    "<pf2:weapons xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                    "\t\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "\t\t\txsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/weapon.xsd\">");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        for (Pair<String, String> table : tables) {
            try {
                out.write("<" + table.second + ">");
                new NethysWeaponsScraper("https://2e.aonprd.com/Weapons.aspx", s-> {
                    try {
                        out.write(s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, table.first,
                        href->href.contains("Weapons.aspx?ID="),
                        s->s.equalsIgnoreCase("core_rulebook"));
                out.write("</" + table.second + ">");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.write("</pf2:weapons>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public NethysWeaponsScraper(String inputURL, Consumer<String> output, String container, Predicate<String> hrefValidator, Predicate<String> sourceValidator) {
        super(inputURL, output, container, hrefValidator, sourceValidator, true);
    }

    @Override
    CategoryEntry addItem(Document doc) {
        Element output = doc.getElementById("main");
        String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
        String source = sourceAndPage.replaceAll(" pg.*", "");

        String name = output.select(".title > a[href^=\"Weapons.aspx\"]").first().wholeText();

        String price = getAfter(output, "Price");
        if(price.equalsIgnoreCase("—") || price.equalsIgnoreCase("0"))
            price = "0 sp";
        String damage = getAfter(output, "Damage");
        String bulk = getAfter(output, "Bulk");
        if(bulk.equalsIgnoreCase("—"))
            bulk = "0";
        String hands = getAfter(output, "Hands");
        String category = getAfter(output, "Category");
        String group = getAfter(output, "Group");
        String traits = getAfter(output, "Traits");
        String range = null;
        String reload = null;

        if(!output.getElementsMatchingText("Range").isEmpty())
            range = getAfter(output, "Range");

        if(!output.getElementsMatchingText("Reload").isEmpty())
            reload = getAfter(output, "Reload");

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

        result.append("<Weapon>\n\t<Name>")
                .append(name).append("</Name>\n")
                .append("\t<Price>").append(price).append("</Price>\n")
                .append("\t<Damage>").append(damage).append("</Damage>\n");

        if(range != null && reload != null)
            result.append("\t<Range>20 ft.</Range>\n" +
                            "\t<Reload>0</Reload>\n");

        result.append("\t<Bulk>").append(bulk).append("</Bulk>\n")
                .append("\t<Hands>").append(hands).append("</Hands>\n")
                .append("\t<Group>").append(group).append("</Group>\n")
                .append("\t<Traits>").append(traits).append("</Traits>\n")
                .append("\t<Description>").append(description).append("</Description>\n")
                .append("</Weapon>\n");

        return new CategoryEntry(name, result.toString(), source, category);
    }

    @Override
    protected List<String> getProficiencies() {
        return Arrays.asList("Unarmed", "Simple", "Martial", "Advanced");
    }
}
