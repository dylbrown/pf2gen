package ui.html.ability_extensions;

import model.abilities.GranterExtension;
import model.ability_scores.AbilityMod;
import model.attributes.AttributeMod;
import model.enums.Sense;

public class GranterExtensionHTMLGenerator {
    public static String parse(GranterExtension granterExtension) {
        StringBuilder text = new StringBuilder();
        for (AttributeMod mod : granterExtension.getModifiers()) {
            text.append("<li>").append(mod.getMod()).append(" in ").append(mod.getAttr()).append("</li>");
        }
        for (AbilityMod mod : granterExtension.getAbilityMods()) {
            text.append("<li> A ").append(mod.getType())
                    .append(mod.isPositive() ? " boost to " : "penalty to ")
                    .append(mod.getTarget()).append("</li>");
        }
        if(granterExtension.getSkillIncreases() > 0) {
            text.append("<li>").append(granterExtension.getSkillIncreases())
                    .append(" skill increases");
        }
        for (Sense sense : granterExtension.getSenses()) {
            text.append("<li>").append(sense).append("</li>");
        }

        String list = text.toString();
        if(list.isBlank()) return "";
        return "<b>Mechanical Benefits</b><ul>" + list + "</ul>";
    }
}
