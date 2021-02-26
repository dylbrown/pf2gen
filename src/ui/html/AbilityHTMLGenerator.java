package ui.html;

import model.abilities.*;
import model.ability_scores.AbilityScore;
import model.attributes.CustomAttribute;
import model.enums.Trait;
import model.util.Pair;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbilityHTMLGenerator {
    public static String parse(Ability ability) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h4 style='display:inline;'>").append(ability.getName());
        ActivityExtension activityExt = ability.getExtension(ActivityExtension.class);
        if(activityExt != null)
            text.append(" ").append(activityExt.getCost().getIcon());
        text.append("<span style='float:right'>Level ").append(ability.getLevel()).append("</span></h4><br>");
        text.append("<b>Traits</b> ")
                .append(ability.getTraits().stream().map(Trait::toString).collect(Collectors.joining(", ")))
                .append("<br>");

        Requirement<CustomAttribute> requiredAttrs = ability.getRequiredAttrs();
        List<Pair<AbilityScore, Integer>> requiredScores = ability.getRequiredScores();
        Requirement<String> requiredWeapons = ability.getRequiredWeapons();
        boolean strings = !ability.getPrereqStrings().isEmpty() || !ability.getPrerequisites().isEmpty();
        boolean attrs = requiredAttrs instanceof SingleRequirement || requiredAttrs instanceof RequirementList;
        boolean scores = requiredScores.size() > 0;
        boolean weapons = requiredWeapons instanceof SingleRequirement || requiredWeapons instanceof RequirementList;
        if(strings || attrs || scores || weapons) {
            text.append("<b>Prerequisites</b> ")
                .append(Stream.concat(ability.getPrereqStrings().stream(),
                        ability.getPrerequisites().stream())
                        .collect(Collectors.joining(", ")));
            if(strings && attrs)
                text.append(", ");
            print(text, requiredAttrs, " in ");
            if((strings || attrs) && scores)
                text.append(", ");
            text.append(requiredScores.stream()
                    .map(p->p.first.toString() + " " + p.second.toString())
                    .collect(Collectors.joining(", ")));
            if((strings || attrs || scores) && weapons)
                text.append(", ");
            print(text, requiredWeapons, " with a ");
            text.append("<br>");
        }
        if(activityExt != null) {
            if(!activityExt.getTrigger().isBlank())
                text.append("<b>Trigger</b> ").append(activityExt.getTrigger()).append("<br>");
        }
        if(!ability.getRequirements().isBlank()) {
            text.append("<b>Requirements</b> ").append(ability.getRequirements()).append("<br>");
        }
        text.append("<hr>").append(ability.getDescription());
        return text.toString();
    }

    private static <T> void print(StringBuilder text, Requirement<T> req, String infix) {
        if(req instanceof RequirementList) {
            String separator = (((RequirementList<T>) req).isAll()) ?
                    " and " : " or ";
            List<Requirement<T>> list = ((RequirementList<T>) req).getRequirements();
            for(int i = 0; i < list.size(); i++) {
                print(text, list.get(i), infix);
                if(i != list.size() - 1)
                    text.append(separator);
            }
        }
        if(req instanceof SingleRequirement) {
            SingleRequirement<T> r = (SingleRequirement<T>) req;
            text.append(r.getProficiency().name()).append(infix).append(r.get().toString());
        }
    }

    public static String parseSingleLine(Ability ability) {
        StringBuilder text = new StringBuilder();
        text.append("<p><b>").append(ability.getName());
        ActivityExtension activityExt = ability.getExtension(ActivityExtension.class);
        if(activityExt != null)
            text.append(" ").append(activityExt.getCost().getIcon());
        text.append("</b> ");
        if(!ability.getPrereqStrings().isEmpty() || !ability.getPrerequisites().isEmpty()) {
            text.append("<b>Prerequisites</b> ")
                    .append(Stream.concat(ability.getPrereqStrings().stream(),
                            ability.getPrerequisites().stream())
                            .collect(Collectors.joining(", ")));
        }
        if(activityExt != null) {
            if(!activityExt.getTrigger().isBlank())
                text.append("<b>Trigger</b> ").append(activityExt.getTrigger());
        }
        if(!ability.getRequirements().isBlank()) {
            text.append("<b>Requirements</b> ").append(ability.getRequirements());
        }
        text.append(ability.getDescription());
        return text.toString();
    }
}
