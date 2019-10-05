package tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

public class BackgroundScraper extends SRDScraper {
    private BufferedWriter out;
    private Map<String, StringBuilder> sources = new HashMap<>();

    public static void main(String[] args) {
        new BackgroundScraper();
    }

    private BackgroundScraper() {
        Document doc;
        try  {
            doc = Jsoup.connect("http://pf2.d20pfsrd.com/background").get();
            out = new BufferedWriter(new FileWriter(new File("backgrounds.txt")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        doc.getElementById("archive-data-table").getElementsByTag("tbody").first().getElementsByTag("tr").forEach(element -> {
            try {
                addBackground(element.child(0).child(0).attr("href"), element.child(2).text());
            } catch (ArrayIndexOutOfBoundsException e) {e.printStackTrace(); }
        });
        for (StringBuilder value : sources.values()) {
            try {
                out.write(value.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String format = "<background>\n" +
            "\t\t<Name>%s</Name>\n" +
            "\t\t<Source>%s</Source>\n" +
            "\t\t<Description>%s</Description>\n" +
            "\t\t<Skill>%s, Lore (%s)</Skill>\n" +
            "\t\t<AbilityBonuses>%s or %s, Free</AbilityBonuses>\n" +
            "\t\t<Feat>%s</Feat>\n" +
            "\t</background>\n";

    private void addBackground(String href, String source) {
        Document doc;
        try {
            doc = Jsoup.connect(href).get();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Element article = doc.getElementsByTag("article").first();

        String backgroundName = article.child(0).text();
        System.out.println(backgroundName);
        String body = article.getElementsByClass("article-content").first().text().replaceAll("\\n", "");
        String[] descChoose = body.split("[cC]hoose [tT]wo [Aa]bility [bB]oosts\\. *One must be to ", 2);
        String description = descChoose[0];
        String[] chooseSkill; boolean or = false;
        if(descChoose[1].contains("You’re trained in the ")) {
            chooseSkill = descChoose[1].split(" ?, and one is a free (ability )?boost\\. *You’re trained in the ", 2);
        }else{
            or = true;
            chooseSkill = descChoose[1].split(" ?, and one is a free (ability )?boost\\. *You’re trained in your choice of (either )?the ", 2);
        }
        String[] choices = chooseSkill[0].split(" or ");
        String choice1 = camelCaseWord(choices[0].substring(0,3));
        String choice2 = camelCaseWord(choices[1].substring(0,3));
        String[] skillsFeat = chooseSkill[1].split(" Lore skill.*You gain the ", 2);
        String skill1; String Lore;
        if(!or) {
            skill1 = camelCaseWord(skillsFeat[0].replaceAll(" .*", ""));
        }else{
            skill1 = camelCase(skillsFeat[0].replaceAll(" skill.*", "").replaceAll(" ?, ?", " or "));
        }
        Lore = camelCase(skillsFeat[0].replaceAll("\\w+ skill( and |, as well as the | and the )", "")).replaceFirst("The ", "");
        String feat = camelCase(skillsFeat[1].replaceAll(" skill feat.*", ""));

        sources.computeIfAbsent(source.toLowerCase(), key->new StringBuilder())
                .append(String.format(format, backgroundName, camelCase(source), description, skill1, Lore, choice1, choice2, feat));
    }

    /*

    * */
}
