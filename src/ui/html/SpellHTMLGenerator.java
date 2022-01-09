package ui.html;

import model.enums.Trait;
import model.items.weapons.Damage;
import model.items.weapons.Dice;
import model.spells.heightened.HeightenedEvery;
import model.spells.Spell;
import model.util.Pair;
import model.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SpellHTMLGenerator {
    private static final String[] beginnings = new String[]{"deal", "takes", "damage plus"};
    private static final String[] endings = new String[]{".", ";", ","};

    public static String parse(Spell spell, int level) {
        //Cast Time and Requirements
        StringBuilder castTimeRequirements = new StringBuilder();
        if(!spell.getCastTime().equals(""))
            castTimeRequirements.append(spell.getCastTime().trim()).append(" (");
        castTimeRequirements.append(spell.getComponents().stream()
                .map(Enum::toString).collect(Collectors.joining(", ")));
        if(!spell.getCastTime().equals(""))
            castTimeRequirements.append(")");
        if(!spell.getRequirements().equals(""))
            castTimeRequirements.append("; <b>Requirements</b> ").append(spell.getRequirements());
        StringBuilder spellAttributes = new StringBuilder();
        boolean semiColon = false;
        if(!spell.getRange().equals("")) {
            semiColon = true;
            spellAttributes.append("<b>Range</b> ").append(spell.getRange());
        }
        if(!spell.getArea().equals("")) {
            if(semiColon) spellAttributes.append("; ");
            else semiColon = true;
            spellAttributes.append("<b>Area</b> ").append(spell.getArea());
        }
        if(!spell.getTargets().equals("")) {
            if(semiColon) spellAttributes.append("; ");
            else semiColon = true;
            spellAttributes.append("<b>Targets</b> ").append(spell.getTargets());
        }

        //Saving Throws and Duration
        if(semiColon) spellAttributes.append("<br>");
        semiColon = false;
        StringBuilder savesDuration = new StringBuilder();
        if(!spell.getSave().equals("")) {
            semiColon = true;
            savesDuration.append("<b>Saving Throw</b> ").append(spell.getSave());
        }
        if(!spell.getDuration().equals("")) {
            if(semiColon) savesDuration.append("; ");
            savesDuration.append("<b>Duration</b> ").append(spell.getDuration());
        }
        return String.format(
                "<p><b>%s - Spell %s</b><br>" +
                        "<b>Source</b> %s<br>" +
                        "<b>Traits</b> %s<br>" +
                        "<b>Traditions</b> %s</b><br>" +
                        "<b>Cast</b> %s<br>" +
                        "%s" +
                        "%s" +
                        "<hr>" +
                        "%s</p>",
                spell.getName(),
                spell.getLevel(),
                spell.getSource(),
                spell.getTraits().stream()
                        .map(Trait::toString).collect(Collectors.joining(", ")),
                spell.getTraditions().stream()
                        .map(Enum::toString).collect(Collectors.joining(", ")),
                castTimeRequirements,
                spellAttributes,
                savesDuration,
                getDescription(spell, level));
    }

    public static String getDescription(Spell spell, int level) {
        if(spell.getHeightenedData() == null)
            return spell.getDescription();
        StringBuilder description = new StringBuilder();
        if(spell.isCantrip())
            description.append(getCantripDescription(spell, level));
        else
            description.append(spell.getDescription());
        if(spell.getHeightenedData() instanceof HeightenedEvery) {
            if(!spell.isCantrip()) // TODO: Support checking if no damage found
                description.append("<br>");
            description.append("<br><b>(+")
                    .append(((HeightenedEvery) spell.getHeightenedData()).getEvery())
                    .append("):</b> ")
                    .append(((HeightenedEvery) spell.getHeightenedData()).getDescription());
        }else{
            boolean heightenedSoFar = false;
            for(int i = spell.getLevel(); i <= level; i++) {
                if(spell.getHeightenedData().hasAtLevel(i)) {
                    if(!heightenedSoFar) {
                        heightenedSoFar = true;
                        if(!spell.isCantrip())
                            description.append("<br>");
                    }
                    description.append("<br><b>Level ")
                            .append(i).append(":</b> ")
                            .append(spell.getHeightenedData().descriptionAtLevel(i));
                }
            }
        }

        return description.toString();
    }

    private static String getCantripDescription(Spell spell, int level) {
        String desc = spell.getDescription();
        List<Integer> heightenLevel = new ArrayList<>();
        List<String> heightenStrings = new ArrayList<>();
        for(int i = spell.getLevel(); i <= level; i++) {
            if(spell.getHeightenedData().hasAtLevel(i)) {
                heightenLevel.add(i);
                heightenStrings.add(spell.getHeightenedData().descriptionAtLevel(i));
            }
        }
        // This stores the numerical damage of each type
        Map<String, Damage.Builder> damageMap = new HashMap<>();
        // This notes whether we should add the base damage at the end
        Map<String, Boolean> replaceMap = new HashMap<>();
        // This stores string damage such as your spellcasting ability modifier
        Map<String, StringBuilder> stringDamagePlus = new HashMap<>();
        // See Acid Splash (acid damage, splash acid damage, persistent acid damage)
        String baseDamageSuffix = "";
        for (String fullString : heightenStrings) {
            for (String clause : fullString.split("(, ?| ?and ){1,2}")) {
                if(clause.matches("[Tt]he( \\w+)* damage( \\w+)* increases (to|by) .*")) {
                    int endIndex = clause.indexOf("damage");
                    String damageType = clause.substring(4, endIndex);
                    // See Acid Splash (acid damage, splash acid damage, persistent acid damage)
                    if(damageType.equals("initial ")) {
                        int end = desc.indexOf(" damage");
                        int start = desc.lastIndexOf(" ", end-1) + 1;
                        baseDamageSuffix = desc.substring(start, end) + " ";
                        damageType = "";
                    }
                    damageType = damageType + baseDamageSuffix;
                    addDamage(damageType,
                            clause.replaceAll("[Tt]he( \\w+)* damage( \\w+)* increases (to|by) ", ""),
                            damageMap, replaceMap, stringDamagePlus, clause.contains("increases to"));
                }
            }
        }
        // Map to String to insert in range
        Map<Pair<Integer, Integer>, String> finalDamageMap = new TreeMap<>(Comparator.comparing(p -> p.first));
        // Map to damageType from range
        Map<Pair<Integer, Integer>, String> finalDamageTypeReference = new TreeMap<>(Comparator.comparing(p -> p.first));
        for (Map.Entry<String, Damage.Builder> entry : damageMap.entrySet()) {
            String damageType = entry.getKey();
            String searchString = damageType + "damage";
            int i = desc.indexOf(searchString);
            int startIndex = 0;
            int endIndex = -1;
            if(i != -1) {
                int equalTo = desc.indexOf("equal to", i + searchString.length());
                for(String s : endings) {
                    int maybeBetter = desc.indexOf(s, i + searchString.length());
                    if(maybeBetter != -1 && (endIndex == -1 || maybeBetter < endIndex))
                        endIndex = maybeBetter;
                }
                if(equalTo != -1 && equalTo < endIndex) {
                    // Follows format "deal damage equal to ..."
                    startIndex = equalTo + 9;
                    while(desc.substring(i + searchString.length() + 9, endIndex).contains("damage")) {
                        int newIndex = desc.lastIndexOf("plus", endIndex);
                        if (newIndex == -1)
                            break;
                        endIndex = newIndex;
                    }
                } else {
                    // Follows format "deal ... damage"
                    int found = -1;
                    for(String s: beginnings) {
                        int maybeBetter = desc.lastIndexOf(s, i);
                        if(found == -1 || maybeBetter > found) {
                            found = maybeBetter;
                            startIndex = maybeBetter + s.length() + 1;
                        }
                    }
                    endIndex = i - 1;
                }
                // Add base damage if should
                if(!replaceMap.containsKey(damageType))
                    addDamage(damageType, desc.substring(startIndex, endIndex),
                            damageMap, replaceMap, stringDamagePlus, false);
                String damageString;
                // If damage contains numerical damage
                if(damageMap.containsKey(damageType)) {
                    damageString = damageMap.get(damageType).build().toString();
                    if (stringDamagePlus.containsKey(damageType)) {
                        // If damage ALSO has a string
                        StringBuilder stringBuilder = stringDamagePlus.get(damageType);
                        if(!(stringBuilder.charAt(0) == ' '))
                            damageString += " plus " + stringBuilder;
                        else
                            damageString += " plus" + stringBuilder;
                    }
                }else{
                    // If damage is only a string
                    damageString = stringDamagePlus.getOrDefault(damageType, new StringBuilder()).toString();
                }
                finalDamageMap.put(new Pair<>(startIndex, endIndex), damageString);
                finalDamageTypeReference.put(new Pair<>(startIndex, endIndex), damageType);
            }
        }
        StringBuilder description = new StringBuilder();
        int prev = 0;
        for (Map.Entry<Pair<Integer, Integer>, String> entry : finalDamageMap.entrySet()) {
            // Insert the section up to the start, then the replacement damage
            description.append(desc, prev, entry.getKey().first)
                    .append(entry.getValue());
            // Set the start to the end of the range
            prev = entry.getKey().second;
        }
        // Append remainder of the string
        description.append(desc.substring(prev));
        if(finalDamageTypeReference.size() > 0)
            description.append("<br>");
        // Record original damage
        for (Map.Entry<Pair<Integer, Integer>, String> entry : finalDamageTypeReference.entrySet()) {
            description.append("<br><b>Original ")
                    .append(StringUtils.camelCaseWord(entry.getValue()))
                    .append("Damage</b> ")
                    .append(desc, entry.getKey().first, entry.getKey().second);
        }

        return description.toString();
    }

    private static void addDamage(String damageType,
                                  String damageString,
                                  Map<String, Damage.Builder> damageMap,
                                  Map<String, Boolean> replaceMap,
                                  Map<String, StringBuilder> stringDamagePlus,
                                  boolean replace) {
        Damage.Builder damage;
        if(replace) {
            damage = new Damage.Builder();
            damageMap.put(damageType, damage);
            replaceMap.put(damageType, true);
        } else {
            damage = damageMap.computeIfAbsent(damageType, s->new Damage.Builder());
        }
        String dice = null;
        String flat = null;
        if(damageString.contains("+")) {
            String[] damageSplit = damageString.split(" ?\\+ ?", 2);
            dice = damageSplit[0];
            flat = damageSplit[1];
        }else if(damageString.contains("plus")) {
            String[] damageSplit = damageString.split(" ?plus ?", 2);
            dice = damageSplit[0];
            flat = damageSplit[1];
        }else if(damageString.matches("\\d+d\\d+.*")) {
            dice = damageString.replaceAll("[.;]", "");
        }else{
            flat = damageString;
        }
        if(dice != null) { // Numerical Damage
            damage.addDice(Dice.valueOf(dice));
        }
        if(flat != null) { // Text Damage
            try{
                int i = Integer.parseInt(flat.replaceAll("[.;]", ""));
                damage.addAmount(i);
            }catch (NumberFormatException ignored) {
                if(replace) {
                    stringDamagePlus.put(damageType, new StringBuilder(flat));
                }else{
                    stringDamagePlus.computeIfAbsent(damageType, s->new StringBuilder())
                            .append(" ")
                            .append(flat);
                }
            }
        }
    }
}
