package tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

class BackgroundScraper extends SRDScraper {

    public static void main(String[] args) {
        new BackgroundScraper();
    }

    private BackgroundScraper() {
        super("http://pf2.d20pfsrd.com/background", "generated/backgrounds.txt", 2);
    }

    @Override
    String addItem(String href, String source) {
        Document doc;
        try {
            doc = Jsoup.connect(href).get();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        Element article = doc.getElementsByTag("article").first();

        //Get Background Name
        String backgroundName = article.child(0).text();
        System.out.println(backgroundName);

        //Get Area which contains the rest of what we want
        String body = article.getElementsByClass("article-content")
                .first().text().replaceAll("\\n", "");

        //Split the string so that the first element is the description, and the remainder is the second
        String[] descChoose = body.split("[cC]hoose [tT]wo [Aa]bility [bB]oosts\\. *One must be to ", 2);
        String description = descChoose[0];

        //Split the string so that the first element is the ability boosts, and the remainder is the second
        String[] chooseSkill; boolean or = false;
        if(descChoose[1].contains("You're trained in the ") || descChoose[1].contains("You’re trained in the ")) {
            chooseSkill = descChoose[1].split(" ?, and one is a free (ability )?boost\\. *You're trained in the ", 2);
        }else{
            or = true;
            chooseSkill = descChoose[1].split(" ?, and one is a free (ability )?boost\\. *You're trained in your choice of (either )?the ", 2);
        }

        //Split the ability boosts
        String[] choices = chooseSkill[0].split(" or ");
        String choice1 = camelCaseWord(choices[0].substring(0,3));
        String choice2 = camelCaseWord(choices[1].substring(0,3));

        //Split the string so that the first element is the skills, and the remainder is the second
        String[] skillsFeat = chooseSkill[1].split(" Lore skill.*You gain the ", 2);
        String skill1; String Lore;
        if(!or) {
            skill1 = camelCaseWord(skillsFeat[0].replaceAll(" .*", ""));
        }else{
            skill1 = camelCase(skillsFeat[0].replaceAll(" skill.*", "").replaceAll(" ?, ?", " or "));
        }

        //Filter out the Lore
        Lore = camelCase(skillsFeat[0].replaceAll("\\w+ skill( and |, as well as the | and the )", ""))
                .replaceFirst("The ", "");

        // Filter out the feat
        String feat = camelCase(skillsFeat[1].replaceAll(" skill feat.*", ""));

        String format = "<background>\n" +
                "\t\t<Name>%s</Name>\n" +
                "\t\t<Source>%s</Source>\n" +
                "\t\t<Description>%s</Description>\n" +
                "\t\t<Skill>%s, Lore (%s)</Skill>\n" +
                "\t\t<AbilityBonuses>%s or %s, Free</AbilityBonuses>\n" +
                "\t\t<Feat>%s</Feat>\n" +
                "\t</background>\n";
        return String.format(format, backgroundName, camelCase(source), description, skill1, Lore, choice1, choice2, feat);
    }
}
