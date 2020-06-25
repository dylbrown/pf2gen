package ui.html;

import model.abilities.Ability;
import model.abilities.Activity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbilityHTMLGenerator {
    public static String parse(Ability ability) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h4 style='display:inline;'>").append(ability.getName());
        if(ability instanceof Activity)
            text.append(" ").append(((Activity) ability).getCost().getIcon());
        text.append("<span style='float:right'>Level ").append(ability.getLevel()).append("</span></h4><br>");
        if(!ability.getPrereqStrings().isEmpty() || !ability.getPrerequisites().isEmpty()) {
            text.append("<b>Prerequisites</b> ")
                .append(Stream.concat(ability.getPrereqStrings().stream(),
                        ability.getPrerequisites().stream())
                        .collect(Collectors.joining(", ")))
                .append("<br>");
        }
        if(ability instanceof Activity) {
            Activity activity = (Activity) ability;
            if(!activity.getTrigger().isBlank())
                text.append("<b>Trigger</b> ").append(activity.getTrigger()).append("<br>");
        }
        if(!ability.getRequirements().isBlank()) {
            text.append("<b>Requirements</b> ").append(ability.getRequirements()).append("<br>");
        }
        text.append("<hr>").append(ability.getDesc());
        return text.toString();
    }
}
