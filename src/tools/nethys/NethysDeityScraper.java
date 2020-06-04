package tools.nethys;

import model.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class NethysDeityScraper extends NethysListScraper {
    public static void main(String[] args) {
        new NethysDeityScraper("https://2e.aonprd.com/Deities.aspx", "generated/deities.pfdyl");
    }

    private NethysDeityScraper(String inputURL, String outputPath) {
        super(inputURL, outputPath, "ctl00_MainContent_DeityElement", href -> href.contains("ID") && href.contains("Deities"));
    }

    @Override
    Pair<String, String> addItem(Document doc) {
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");
        String deity = output.getElementsByTag("h1")
                .first().text().replaceAll("( \\(.*| ?\\[.*)", "");
        String title = output.getElementsByTag("h1")
                .first().text().replaceAll("(.*\\(|\\).*| ?\\[.*)", "");
        if(title.equals(deity)) title = "";
        String deityAlignment = output.getElementsByTag("h1")
                .first().text().replaceAll("(.*\\[|].*)", "");
        Element sourceLabel = output.getElementsMatchingText("\\ASource\\z").first();
        String sourceAndPage = sourceLabel.nextElementSibling().text().replaceAll(";", "");
        String source = sourceAndPage.replaceAll(" pg.*", "");
        String pageNo = sourceAndPage.replaceAll(".*pg\\. ", "");
        String desc = "";
        Node temp = sourceLabel.nextSibling();
        while(true) {
            if(temp instanceof TextNode && !((TextNode) temp).getWholeText().trim().equals("")) {
                desc = ((TextNode) temp).getWholeText().trim();
                break;
            }
            temp = temp.nextSibling();
            if(temp == null) break;
        }
        String edicts = getAfter(output, "Edicts");
        String anathema = getAfter(output, "Anathema");
        String areasOfConcern = getAfter(output, "Areas of Concern");
        String followerAlignments = getAfter(output, "Follower Alignments");
        String divineFont = getRestOfLine(output, "Divine Font");
        String divineSkill = getRestOfLine(output, "Divine Skill");
        String favoredWeapon = getRestOfLine(output, "Favored Weapon");
        String domains = getRestOfLine(output, "Domains");
        String clericSpellsRaw = getRestOfLine(output, "Cleric Spells").replaceAll("\\([^)]*\\)", "");
        StringBuilder clericSpells = new StringBuilder("\n");
        for (String s : clericSpellsRaw.split(", ?")) {
            String[] numberName = s.split("\\w\\w: ?");
            clericSpells.append("\t\t<Spell level=\"")
                    .append(numberName[0]).append("\" name=\"")
                    .append(numberName[1]).append("\" />\n");
        }
        clericSpells.append("\t");
        String results = String.format("<Deity page=\"%s\">\n" +
                        getEntry("Name") +
                        getEntry("Title") +
                        getEntry("DeityAlignment") +
                        getEntry("Edicts") +
                        getEntry("Anathema") +
                        getEntry("AreasOfConcern") +
                        getEntry("FollowerAlignments") +
                        getEntry("DivineFont") +
                        getEntry("DivineSkill") +
                        getEntry("FavoredWeapon") +
                        getEntry("Domains") +
                        getEntry("Spells") +
                        getEntry("Description") +
                        "</Deity>\n",
                pageNo, deity, title, deityAlignment, edicts,
                anathema, areasOfConcern, followerAlignments,
                divineFont, divineSkill, favoredWeapon,
                domains, clericSpells.toString(), desc).replaceAll("\t<[^>]*><[^>]*>\n", "");

        return new Pair<>(results, source);
    }
}
