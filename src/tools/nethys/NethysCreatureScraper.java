package tools.nethys;

import model.util.Pair;
import model.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.function.Predicate;

public class NethysCreatureScraper extends NethysListScraper {
    private static final List<String> notAbilities = Arrays.asList("Items", "AC", "Fort", "Ref", "Will", "HP", "Immunities", "Resistances", "Weaknesses", "Speed");
    private enum AbilityPart {
        Traits, Trigger, Frequency, Requirements, Description
    }

    public static void main(String[] args) {
        new NethysCreatureScraper(
                "https://2e.aonprd.com/NPCs.aspx?Letter=All",
                "generated/npcs.pfdyl",
                source->source.equals("gamemastery_guide"));
    }

    public NethysCreatureScraper(String inputURL, String outputPath, Predicate<String> sourceValidator) {
        super(inputURL, outputPath, "ctl00_MainContent_TableElement",
                href-> href.contains("NPCs") && href.contains("ID="), sourceValidator, true);
    }

    @Override
    Entry addItem(Document doc) {
        Element output = doc.getElementById("ctl00_MainContent_DetailedOutput");

        Elements titles = output.getElementsByTag("h1");
        Element title = titles.get(1);
        String creature = title.getElementsByTag("a").first().text();
        Pair<String, String> sourcePage = getSourcePage(output);
        String family = null;
        Elements elems = output.getElementsByAttributeValueContaining("href", "MonsterFamilies");
        if(elems.size() > 0)
            family = elems.first().text();
        String level = title.getElementsByTag("span").first().text();
        int i = level.indexOf(' ');
        if(i != -1)
            level = level.substring(i + 1);
        String description = getUntil(titles.get(0).nextSibling(),
                node->node == null || (node instanceof Element && ((Element) node).hasClass("title")));
        List<String> traits = new ArrayList<>();

        Element curr = titles.get(1).nextElementSibling();
        while(!curr.text().equals("Source")) {
            boolean isTrait = false;
            for (String className : curr.classNames()) {
                if(className.startsWith("trait")) {
                    isTrait = true;
                    break;
                }
            }
            if(isTrait) {
                traits.add(curr.wholeText());
            }
            curr = curr.nextElementSibling();
        }

        String perception = getAfter(output, "Perception");
        String languages = getAfter(output, "Languages");
        String skills = getAfter(output, "Skills");
        String scores = getRestOfLineNoTags(output, "Str");
        String items = getAfter(output, "Items");
        String ac = getAfter(output, "AC");
        String saves = getRestOfLineNoTags(output, "Fort");
        String hp = getAfter(output, "HP");
        String immunities = getAfter(output, "Immunities");
        String resistances = getAfter(output, "Resistances");
        String weaknesses = getAfter(output, "Weaknesses");
        String speed = getAfter(output, "Speed");
        StringBuilder miscAbilities = new StringBuilder();
        StringBuilder defensiveAbilities = new StringBuilder();
        StringBuilder offensiveAbilities = new StringBuilder();
        StringBuilder attacks = new StringBuilder();
        StringBuilder spells = new StringBuilder();
        Elements hrs = output.getElementsByTag("hr");
        loadAbilities(output.getElementsContainingText("Cha").first().nextElementSibling(),
                miscAbilities, attacks, spells);
        loadAbilities(hrs.get(0).nextElementSibling(), defensiveAbilities, attacks, spells);
        loadAbilities(hrs.get(1).nextElementSibling(), offensiveAbilities, attacks, spells);
        StringBuilder result = new StringBuilder();
        result.append("<Creature level=\"").append(level).append("\" page=\"").append(sourcePage.second).append("\">\n")
                .append("\t<Name>").append(creature).append("</Name>\n");
        if(family != null) {
            result.append("\t<Family>").append(family).append("</Family>\n");
        }
        result.append("\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
        result.append("\t<Perception>").append(perception).append("</Perception>\n");
        if(!languages.isBlank())
            result.append("\t<Languages>").append(languages).append("</Languages>\n");
        result.append("\t<Skills>").append(skills).append("</Skills>\n");
        result.append("\t<AbilityScores>").append(scores).append("</AbilityScores>\n");
        if(items.length() > 0)
            result.append("\t<Items>").append(items).append("</Items>\n");
        if(miscAbilities.length() > 0)
            result.append("\t<MiscAbilities>\n").append(miscAbilities).append("\t</MiscAbilities>\n");
        result.append("\t<AC>").append(ac).append("</AC>\n");
        result.append("\t<Saves>").append(saves).append("</Saves>\n");
        result.append("\t<HP>").append(hp).append("</HP>\n");
        if(immunities.length() > 0)
            result.append("\t<Immunities>").append(immunities).append("</Immunities>\n");
        if(resistances.length() > 0)
            result.append("\t<Resistances>").append(resistances).append("</Resistances>\n");
        if(weaknesses.length() > 0)
            result.append("\t<Weaknesses>").append(weaknesses).append("</Weaknesses>\n");
        if(defensiveAbilities.length() > 0)
            result.append("\t<DefensiveAbilities>\n").append(defensiveAbilities).append("\t</DefensiveAbilities>\n");
        result.append("\t<Speed>").append(speed).append("</Speed>\n");
        if(attacks.length() > 0)
            result.append("\t<Attacks>\n").append(attacks).append("\t</Attacks>\n");
        result.append(spells);
        if(offensiveAbilities.length() > 0)
            result.append("\t<OffensiveAbilities>\n").append(offensiveAbilities).append("\t</OffensiveAbilities>\n");
        result.append("\t<Description>").append(description).append("</Description>\n");
        result.append("</Creature>\n");
        return new Entry(creature, result.toString(), sourcePage.first);
    }

    private void loadAbilities(Element curr, StringBuilder abilities, StringBuilder attacks, StringBuilder spells) {
        while(curr != null && !curr.tagName().equals("hr")) {
            if(curr.tagName().equals("b") && !notAbilities.contains(curr.text())) {
                curr = loadAbility(curr, abilities, attacks, spells, false);
            }else if(curr.hasClass("hanging-indent")) {
                Element subCurr = curr.child(0);
                while(subCurr != null)
                    subCurr = loadAbility(subCurr, abilities, attacks, spells, true);
            }
            if(curr == null || curr.tagName().equals("hr") || curr.hasClass("title"))
                break;
            curr = curr.nextElementSibling();
        }
    }

    private static final List<String> spellStrings = Arrays.asList(
            "Domain Spells",
            "Innate Spells",
            "Prepared Spells",
            "Spontaneous Spells",
            "Devotion Spells",
            "Order Spells",
            "Primal Spells",
            "Bloodline Spells",
            "Rituals"
    );
    private Element loadAbility(Element curr, StringBuilder abilities,
                                StringBuilder attacks, StringBuilder spells, boolean isSingleAbility) {
        boolean loadedTraits = false;
        for (String spellString : spellStrings) {
            if(curr.text().endsWith(spellString))
                return addSpells(curr, spells);
        }
        if(curr.text().equals("Melee") || curr.text().equals("Ranged"))
            return addAttack(curr, attacks);
        String abilityName = curr.text();
        String cost = null;
        Elements imgs = curr.getElementsByTag("img");
        if(imgs.size() > 0) {
            switch(imgs.get(0).attr("alt")) {
                case "Single Action":
                    cost = "1";
                    break;
                case "Two Actions":
                    cost = "2";
                    break;
                case "Three Actions":
                    cost = "3";
                    break;
                case "Free Action":
                    cost = "Free";
                    break;
                case "Reaction":
                    cost = "Reaction";
                    break;
            }
        }
        Map<AbilityPart, StringBuilder> builders = new TreeMap<>(Comparator.comparingInt(Enum::ordinal));
        builders.put(AbilityPart.Description, new StringBuilder());
        StringBuilder currentBuilder = builders.get(AbilityPart.Description);
        Node currNode = curr.nextSibling();
        AbilityPart currPart = AbilityPart.Description;
        AbilityPart previousPart = AbilityPart.Description;
Loop:   while(currNode != null) {
            if(currNode instanceof Element) {
                curr = (Element) currNode;
                if(curr.tagName().equals("b")) {
                    if(curr.text().endsWith("Spells")) {
                        curr = addSpells(curr, spells);
                        currNode = curr;
                    } else if(curr.text().matches(".*\\d.*")) {
                        currentBuilder.append("&lt;b&gt;").append(curr.text()).append("&lt;/b&gt;");
                    } else switch (curr.text()) {
                        case "Melee":
                        case "Ranged":
                            if(isSingleAbility) {
                                currentBuilder.append("&lt;b&gt;").append(curr.text()).append("&lt;/b&gt;");
                            } else {
                                curr = addAttack(curr, attacks);
                                currNode = curr;
                            }
                            break;
                        case "Requirement":
                        case "Requirements":
                            currentBuilder = builders.computeIfAbsent(AbilityPart.Requirements,
                                    s->new StringBuilder());
                            break;
                        case "Trigger":
                            currentBuilder = builders.computeIfAbsent(AbilityPart.Trigger,
                                    s->new StringBuilder());
                            break;
                        case "Frequency":
                            currentBuilder = builders.computeIfAbsent(AbilityPart.Frequency,
                                    s->new StringBuilder());
                            break;
                        case "Effect":
                            currentBuilder = builders.computeIfAbsent(AbilityPart.Description,
                                    s->new StringBuilder());
                            break;
                        default:
                            if(isSingleAbility) {
                                currentBuilder.append("&lt;b&gt;").append(curr.text()).append("&lt;/b&gt;");
                            } else {
                                curr = curr.previousElementSibling();
                                break Loop;
                            }
                    }
                } else if(curr.tagName().equals("img")) {
                    switch(curr.attr("alt")) {
                        case "Single Action":
                            cost = "1";
                            break;
                        case "Two Actions":
                            cost = "2";
                            break;
                        case "Three Actions":
                            cost = "3";
                            break;
                        case "Free Action":
                            cost = "Free";
                            break;
                        case "Reaction":
                            cost = "Reaction";
                            break;
                    }
                } else if(curr.tagName().equals("hr") || curr.hasClass("title")) {
                    break;
                } else {
                    currentBuilder.append(parseDesc(curr));
                }
            }else if(currNode instanceof TextNode){
                String text = ((TextNode) currNode).text();
                if(text.endsWith("(") && !loadedTraits) {
                    loadedTraits = true;
                    previousPart = currPart;
                    currPart = AbilityPart.Traits;
                    builders.put(AbilityPart.Traits, new StringBuilder());
                    currentBuilder = builders.get(AbilityPart.Traits);
                    text = text.substring(0, text.length() - 1);
                }else if(text.startsWith(")") && currPart == AbilityPart.Traits) {
                    currPart = previousPart;
                    currentBuilder = builders.get(AbilityPart.Description);
                    text = text.substring(1);
                }
                currentBuilder.append(text);
            }
            if(currNode == null)
                break;
            currNode = currNode.nextSibling();
        }
        if(currNode == null)
            curr = null;
        if(cost == null) abilities.append("\t\t<Ability name=\"").append(abilityName).append("\">\n");
        else abilities.append("\t\t<Ability name=\"").append(abilityName).append("\" cost=\"")
                .append(cost).append("\">\n");
        if(builders.get(AbilityPart.Description).length() == 0)
            builders.remove(AbilityPart.Description);
        for (Map.Entry<AbilityPart, StringBuilder> entry : builders.entrySet()) {
            String s = entry.getValue().toString().trim().replaceAll("(&lt;br&gt;\\z|\\A[; ]*)", "");
            if(s.isBlank())
                continue;
            if(entry.getKey() == AbilityPart.Traits)
                s = StringUtils.capitalize(s);
            abilities.append("\t\t\t<").append(entry.getKey()).append(">")
                    .append(s)
                    .append("</").append(entry.getKey()).append(">\n");
        }
        abilities.append("\t\t</Ability>\n");
        return curr;
    }

    private Element addSpells(Element curr, StringBuilder builder) {
        String title = curr.text();
        Node dcAttack = curr.nextSibling();
        if(!(dcAttack instanceof TextNode))
            return curr;
        String text = ((TextNode) dcAttack).text();
        String dc = (text.length() < 6) ? null : text.substring(4, 6);
        int attackStart = text.indexOf("attack");
        String attack = (attackStart == -1) ? null : text.substring(attackStart+7, text.length()-2);
        curr = curr.nextElementSibling();
        String currLevel = null;
        Map<String, Integer> slotsMap = new TreeMap<>(Comparator.reverseOrder());
        Map<String, List<String>> spells = new TreeMap<>(Comparator.reverseOrder());
        while(curr != null) {
            if(curr.tagName().equals("b")) {
                if(curr.text().matches("\\A\\d+.*")) {
                    currLevel = curr.text().replaceAll("[^\\d]", "");
                    spells.put(currLevel, new ArrayList<>());
                }else if(curr.text().startsWith("Cantrips")) {
                    currLevel = handleSpecialSpellCase(curr, spells, "0");
                }else if(curr.text().startsWith("Constant")) {
                    currLevel = handleSpecialSpellCase(curr, spells, "Constant");
                }else if(curr.text().startsWith("(") && "0".equals(currLevel) || "Constant".equals(currLevel)) {
                    String oldCurr = currLevel;
                    currLevel = currLevel + " " + curr.text();
                    spells.put(currLevel, spells.get(oldCurr));
                    spells.remove(oldCurr);
                }else break;
            } else if(curr.hasClass("hanging-indent")) {
                break;
            } else {
                String s = curr.wholeText();
                Node node = curr.nextSibling();
                if(node instanceof TextNode) {
                    boolean append = false;
                    String s1 = ((TextNode) node).text();
                    if(s1.matches(".*\\([^)]*\\).*"))
                        append = true;
                    int slots = s1.indexOf("slots");
                    if(append && slots != -1) {
                        int start = s1.indexOf('(');
                        slotsMap.put(currLevel, Integer.parseInt(s1.substring(start + 1, slots - 1)));
                        append = false;
                    }
                    if(append)
                        s += s1.replaceAll("[;,]? *\\z", "");
                }
                if(!s.isBlank())
                    spells.get(currLevel).add(s);
            }
            curr = curr.nextElementSibling();
        }
        builder.append("\t<Spells>\n")
                .append("\t\t<Title>").append(title).append("</Title>\n");
        if(dc != null)
            builder.append("\t\t<DC>").append(dc).append("</DC>\n");
        if(attack != null)
            builder.append("\t\t<Attack>").append(attack).append("</Attack>\n");
        for (Map.Entry<String, List<String>> entry : spells.entrySet()) {
            builder.append("\t\t<Spells level=\"").append(entry.getKey());
            if(slotsMap.containsKey(entry.getKey())) {
                builder.append("\" slots=\"").append(slotsMap.get(entry.getKey()));
            }
            builder.append("\">").append(String.join(", ", entry.getValue())).append("</Spells>\n");
        }
        builder.append("\t</Spells>\n");

        if(curr == null)
            return null;
        return curr.previousElementSibling();
    }

    private String handleSpecialSpellCase(Element curr, Map<String, List<String>> spells, String specialCase) {
        int i = curr.text().indexOf('(');
        String currLevel;
        if(i == -1) {
            currLevel = specialCase;
        } else {
            int end = curr.text().indexOf(')');
            currLevel = specialCase + " " + curr.text().substring(i, end+1);
        }
        spells.put(currLevel, new ArrayList<>());
        return currLevel;
    }

    private Element addAttack(Element curr, StringBuilder attacks) {
        String type = curr.text();
        Node nameAttack = curr.nextElementSibling().nextElementSibling().nextSibling();
        if(!(nameAttack instanceof TextNode))
            return curr;
        String text = ((TextNode) nameAttack).text();
        while(text.isBlank()) {
            nameAttack = nameAttack.nextSibling();
            if(nameAttack instanceof TextNode)
                text = ((TextNode) nameAttack).text();
            if(nameAttack instanceof Element)
                text = ((Element) nameAttack).text();
        }
        if(!text.contains("+")) {
            Node next = nameAttack.nextSibling();
            if(next instanceof TextNode)
            text = text + ((TextNode) next).text();
        }
        int bonusSign = text.indexOf('+');
        int minusIndex = text.indexOf(" -");
        if(bonusSign == -1 || (minusIndex != -1 && minusIndex < bonusSign))
            bonusSign = minusIndex + 1;
        String name = text.substring(0, bonusSign-1);
        int endBonus = text.indexOf(' ', bonusSign);
        if(endBonus-bonusSign == 1)
            endBonus = text.indexOf(' ', endBonus + 1);
        String attackModifier = text.substring(bonusSign, endBonus).replace(" ", "");
        List<String> traits = new ArrayList<>();
        curr = curr.nextElementSibling();
        while(!curr.tagName().equals("b") && !curr.tagName().equals("a"))
            curr = curr.nextElementSibling();
        while(!curr.tagName().equals("b")) {
            if(!curr.attr("href").equals("Rules.aspx?ID=322"))
                traits.add(StringUtils.camelCaseWord(curr.text()));
            curr = curr.nextElementSibling();
        }
        String damage = getRestOfLineFromNodeNoTags(curr.nextSibling());
        do{
            curr = curr.nextElementSibling();
        }while (curr != null && curr.tagName().equals("b"));
        attacks.append("\t\t<Attack type=\"").append(type.trim()).append("\">\n")
                .append("\t\t\t<Name>").append(StringUtils.capitalize(name.trim())).append("</Name>\n")
                .append("\t\t\t<AttackModifier>").append(attackModifier.trim()).append("</AttackModifier>\n");
        if(traits.size() > 0)
            attacks.append("\t\t\t<Traits>").append(String.join(", ", traits)).append("</Traits>\n");
        attacks.append("\t\t\t<Damage>").append(damage.trim()).append("</Damage>\n")
                .append("\t\t</Attack>\n");
        return curr;
    }
}
