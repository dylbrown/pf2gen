package ui.controls;

import model.abilities.Ability;
import model.abilities.Activity;

public class AbilityHTMLGenerator {
    public static String generate(Ability ability) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h4 style='display:inline;'>");
        if(ability instanceof Activity)
            text.append(((Activity) ability).getCost().getIcon()).append(" ");
        text.append(ability.getName()).append("</h4><br>");
        if(ability instanceof Activity) {
            Activity activity = (Activity) ability;
            if(!activity.getTrigger().isBlank())
                text.append("<b>Trigger</b> ").append(activity.getTrigger()).append("<br>");
        }
        if(!ability.getRequirements().isBlank()) {
            text.append("<b>Requirements</b> ").append(ability.getRequirements()).append("<br>");
        }
        text.append("<b>Effect:</b> ").append(ability.getDesc());
        return text.toString();
    }
}
