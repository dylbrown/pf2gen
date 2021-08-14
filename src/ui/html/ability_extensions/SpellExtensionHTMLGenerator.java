package ui.html.ability_extensions;

import model.abilities.SpellExtension;
import model.ability_scores.AbilityScore;
import model.spells.CasterType;
import model.spells.Spell;
import model.spells.SpellType;
import model.util.StringUtils;

import java.util.List;
import java.util.Map;

public class SpellExtensionHTMLGenerator {
    public static String parse(SpellExtension spellExtension) {
        StringBuilder text = new StringBuilder();
        if(!spellExtension.getSpellListName().isBlank() && (
                (spellExtension.getCasterType() != CasterType.None ||
                        spellExtension.getTradition() != null ||
                        spellExtension.getCastingAbilityScore() != AbilityScore.None))) {
            text.append("<li>");
            if(spellExtension.getCasterType() != CasterType.None)
                text.append(spellExtension.getCasterType()).append(" ");
            if(spellExtension.getTradition() != null)
                text.append(spellExtension.getTradition()).append(" ");
            if(spellExtension.getCastingAbilityScore() != AbilityScore.None)
                text.append(spellExtension.getCastingAbilityScore()).append("-based ");
            text.append(spellExtension.getSpellListName()).append(" Spellcasting</li>");
        }
        for (Map.Entry<Integer, Integer> entry : spellExtension.getSpellSlots().entrySet()) {
            text.append("<li>").append(entry.getValue()).append(" ")
                    .append(StringUtils.spellLevelOrdinal(entry.getKey())).append("-level spell slots</li>");
        }
        for (Map.Entry<Integer, Integer> entry : spellExtension.getExtraSpellsKnown().entrySet()) {
            text.append("<li>").append(entry.getValue()).append(" extra ")
                    .append(StringUtils.spellLevelOrdinal(entry.getKey())).append("-level spells known</li>");
        }
        for (Map.Entry<SpellType, List<Spell>> entry : spellExtension.getBonusSpells().entrySet()) {
            text.append("<li>").append(entry.getKey().toString()).append("(s):<ul>");
            for (Spell spell : entry.getValue()) {
                text.append("<li>").append(spell.getName()).append("</li>");
            }
            text.append("</ul></li>");
        }

        String list = text.toString();
        if(list.isBlank()) return "";
        return "<b>Spellcasting Benefits</b><ul>" + list + "</ul>";
    }
}
