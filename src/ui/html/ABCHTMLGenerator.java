package ui.html;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.GranterExtension;
import model.ability_slots.AbilitySlot;
import model.ability_slots.FilledSlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.attributes.AttributeMod;
import model.enums.Language;
import model.enums.Sense;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ABCHTMLGenerator {
    private ABCHTMLGenerator() {}

    public static String parse(Ancestry ancestry) {
        List<String> bonuses = new ArrayList<>();
        List<String> flaws = new ArrayList<>();
        for (AbilityMod abilityMod : ancestry.getAbilityMods()) {
            if(abilityMod.isPositive()) bonuses.add(abilityMod.getTarget().name());
            else flaws.add(abilityMod.getTarget().name());
        }

        String bonusLanguages;
        if(!ancestry.getBonusLanguages().equals(Arrays.asList(Language.getChooseable())))
            bonusLanguages = ancestry.getBonusLanguages().stream()
                    .map(Language::name).collect(Collectors.joining(", "));
        else
            bonusLanguages = "All Common Languages";

        return String.format("<h3>%s</h3>" +
                        "<b>Hit Points</b> %d; <b>Size</b> %s; <b>Speed</b> %d ft.<br>" +
                        "<b>Ability Boosts</b> %s; <b>Ability Flaws</b> %s<br>" +
                        "<b>Senses</b> %s; <b>Languages</b> %s<br>" +
                        "<b>Bonus Languages</b> %s" +
                        "<p>%s</p>",
                ancestry.getName(),
                ancestry.getHP(),
                ancestry.getSize().toString(),
                ancestry.getSpeed(),
                String.join(", ", bonuses),
                String.join(", ", flaws),
                ancestry.getSenses().stream().map(Sense::getName).collect(Collectors.joining(", ")),
                ancestry.getLanguages().stream().map(Language::name).collect(Collectors.joining(", ")),
                bonusLanguages,
                ancestry.getDescription());
    }

    public static String parse(Background background) {
        List<String> abilityMods = new ArrayList<>();
        for (AbilityMod abilityMod : background.getAbilityMods()) {
            if(abilityMod instanceof AbilityModChoice) {
                if(((AbilityModChoice) abilityMod).getChoices().size() == 6)
                    abilityMods.add("Free");
                abilityMods.add(((AbilityModChoice) abilityMod).getChoices().stream()
                        .map(AbilityScore::name).collect(Collectors.joining(" or ")));
            }else abilityMods.add(abilityMod.getTarget().name());
        }

        return String.format("<h3>%s</h3>" +
                "<p>%s</p>" +
                "<b>Ability Boosts</b> %s<br>" +
                "<b>Skills</b> %s<br>" +
                "<b>Bonus Feat</b> %s",
                background.getName(),
                background.getDescription(),
                String.join(", ", getAbilityMods(background.getAbilityMods())),
                background.getMods().stream().map(AttributeMod::toNiceAttributeString)
                        .collect(Collectors.joining(", ")),
                background.getFreeFeat().getCurrentAbility().toString());
    }

    private static List<String> getAbilityMods(List<AbilityMod> mods) {
        List<String> abilityMods = new ArrayList<>();
        for (AbilityMod abilityMod : mods) {
            if(abilityMod instanceof AbilityModChoice) {
                if(((AbilityModChoice) abilityMod).getChoices().size() == 6)
                    abilityMods.add("Free");
                abilityMods.add(((AbilityModChoice) abilityMod).getChoices().stream()
                        .map(AbilityScore::name).collect(Collectors.joining(" or ")));
            }else abilityMods.add(abilityMod.getTarget().name());
        }
        return abilityMods;
    }

    public static String parse(PClass pClass) {
        List<String> proficiencies = new ArrayList<>();
        for (AbilitySlot slot : pClass.getLevel(1)) {
            if(!(slot instanceof FilledSlot))
                continue;
            Ability currentAbility = slot.getCurrentAbility();
            if(currentAbility.getName().trim().equalsIgnoreCase("initial proficiencies")) {
                GranterExtension granter = currentAbility.getExtension(GranterExtension.class);
                if(granter != null) {
                    for (AttributeMod modifier : granter.getModifiers()) {
                        proficiencies.add(modifier.getMod().name() + " in " + modifier.toNiceAttributeString());
                    }
                }
                break;
            }
        }

        return String.format("<h3>%s</h3>" +
                "<p>%s</p>" +
                        "<b>Key Ability</b> %s; <b>Hit Points</b> %d plus your Con modifier.<br>" +
                        "<b>Skill Increases</b> %d plus your Int Modifier.<br>" +
                        "<b>Initial Proficiencies</b><br>%s",
                pClass.getName(),
                pClass.getDescription(),
                String.join(", ", getAbilityMods(pClass.getAbilityMods())),
                pClass.getHP(),
                pClass.getSkillIncrease(),
                String.join("<br>", proficiencies));
    }
}
