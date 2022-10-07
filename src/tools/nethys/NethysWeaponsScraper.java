package tools.nethys;

import model.enums.WeaponProficiency;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NethysWeaponsScraper extends NethysSourceScraper {

    public static void main(String[] args) {
        new NethysWeaponsScraper("https://2e.aonprd.com/Sources.aspx?ID=98", "Guns and Gears");
    }

    public NethysWeaponsScraper(String inputURL, String source) {
        super(inputURL, source, href -> href.startsWith("Weapons"));
    }

    protected NethysListScraper.Entry addItem(Document doc) {

        Element output = doc.getElementById("main");
        String sourceAndPage = output.getElementsMatchingText("\\ASource\\z").first().nextElementSibling().text();
        String source = sourceAndPage.replaceAll(" pg.*", "");

        String category = getAfter(output, "Category");

        if(category.equals("Ammunition")) {
            return null;
        }

        String name = output.select(".title > a[href^=\"Weapons.aspx\"]").first().wholeText();

        String price = getAfter(output, "Price");
        if (price.equalsIgnoreCase("—") || price.equalsIgnoreCase("0"))
            price = "0 sp";
        String damage = getAfter(output, "Damage");
        String bulk = getAfter(output, "Bulk");
        if (bulk.equalsIgnoreCase("—"))
            bulk = "0";
        String hands = getAfter(output, "Hands");
        String group = getAfter(output, "Group");
        String traits = getAfter(output, "Traits");
        String range = null;
        String reload = null;

        if (!output.getElementsMatchingText("Range").isEmpty())
            range = getAfter(output, "Range");

        if (!output.getElementsMatchingText("Reload").isEmpty())
            reload = getAfter(output, "Reload");

        Node afterHr = output.select("#ctl00_RadDrawer1_Content_MainContent_DetailedOutput hr").first().nextSibling();
        StringBuilder description = new StringBuilder();
        while (afterHr != null) {
            description.append(parseDesc(afterHr));
            afterHr = afterHr.nextSibling();
            if (afterHr instanceof Element && Arrays.asList("hr", "h2")
                    .contains(((Element) afterHr).tagName()))
                break;
        }

        StringBuilder result = new StringBuilder();

        result.append("<Weapon>\n\t<Name>")
                .append(name).append("</Name>\n")
                .append("\t<Price>").append(price).append("</Price>\n")
                .append("\t<Damage>").append(damage).append("</Damage>\n");

        if (range != null && reload != null)
            result.append("\t<Range>20 ft.</Range>\n" +
                    "\t<Reload>0</Reload>\n");

        result.append("\t<Bulk>").append(bulk).append("</Bulk>\n")
                .append("\t<Hands>").append(hands).append("</Hands>\n")
                .append("\t<Group>").append(group).append("</Group>\n")
                .append("\t<Traits>").append(traits).append("</Traits>\n")
                .append("\t<Description>").append(description).append("</Description>\n")
                .append("</Weapon>\n");
        String rangedOrMelee = (range != null ? "Ranged" : "Melee");
        int categoryIndex = WeaponProficiency.robustValueOf(category).ordinal() + 1;
        return new Entry( rangedOrMelee + "_" + categoryIndex + "_" + category, result.toString(), source);
    }

    @Override
    protected String nameToFileName(String name) {
        return "generated/" + source + "/equipment/" + StringUtils.capitalize(name) + ".pfdyl";
    }

    @Override
    protected void write() {
        objects.keySet().forEach(name -> {
            File file = new File(nameToFileName(name));
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                System.out.println("Failed to create dir " + file);
            }
        });
        BufferedWriter out;
        boolean lastMelee = false;
        boolean lastRanged = false;
        WeaponProficiency lastProf = null;
        try {
            out = new BufferedWriter(new FileWriter(nameToFileName("Weapons")), 32768);
            out.write("<?xml version = \"1.0\"?>\n" +
                    "<pf2:weapons xmlns:pf2=\"https://dylbrown.github.io\"\n" +
                    "\t\t\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "\t\t\txsi:schemaLocation=\"https://dylbrown.github.io ../../../schemata/weapon.xsd\">\n");
            for (Map.Entry<String, List<Entry>> entry : objects.entrySet()) {
                String[] split = entry.getKey().split("_\\d_");
                boolean isMelee = split[0].equals("Melee");
                WeaponProficiency proficiency = WeaponProficiency.robustValueOf(split[1]);
                if ((lastProf != proficiency || (lastMelee && !isMelee)) && lastProf != null) {
                    out.write("</" + lastProf + ">\n");
                }
                if (isMelee && !lastMelee) {
                    out.write("<Melee>\n");
                    lastMelee = true;
                } else if (!isMelee && !lastRanged) {
                    if(lastMelee) {
                        out.write("</Melee>\n");
                    }
                    out.write("<Ranged>\n");
                }
                if (lastProf != proficiency || (lastMelee && !isMelee)) {
                    out.write("<" + proficiency + ">\n");
                    lastProf = proficiency;
                }
                lastMelee = isMelee;
                lastRanged = !isMelee;
                try {
                    for (Entry singleObject : entry.getValue()) {
                        out.write(singleObject.entry);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (lastProf != null) {
                out.write("</" + lastProf + ">\n");
            }
            if(lastMelee) {
                out.write("</Melee>\n");
            } else {
                out.write("</Ranged>\n");
            }

            out.write("\n</pf2:weapons>");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
