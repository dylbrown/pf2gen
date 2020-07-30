package ui.html;

import model.spells.Spell;

import java.util.stream.Collectors;

public class SpellHTMLGenerator {
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
                        "<b>Traits</b> %s<br>" +
                        "<b>Traditions</b> %s</b><br>" +
                        "<b>Cast</b> %s<br>" +
                        "%s" +
                        "%s" +
                        "<hr>" +
                        "%s</p>",
                spell.getName(),
                spell.getLevel(),
                spell.getTraits().stream()
                        .map(Enum::toString).collect(Collectors.joining(", ")),
                spell.getTraditions().stream()
                        .map(Enum::toString).collect(Collectors.joining(", ")),
                castTimeRequirements,
                spellAttributes,
                savesDuration,
                getDescription(spell, level));
    }

    public static String getDescription(Spell spell, int level) {
        StringBuilder description = new StringBuilder();
        description.append(spell.getDescription());
        if(spell.getHeightenedData() == null)
            return description.toString();
        boolean heightenedSoFar = false;
        for(int i = spell.getLevel(); i <= level; i++) {
            if(!heightenedSoFar) {
                heightenedSoFar = true;
                description.append("<br>");
            }
            if(spell.getHeightenedData().hasAtLevel(i)) {
                description.append("<br><b>Level ")
                        .append(i).append(":</b> ")
                        .append(spell.getHeightenedData().descriptionAtLevel(i));
            }
        }

        return description.toString();
    }
}
