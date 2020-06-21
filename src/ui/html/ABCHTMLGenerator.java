package ui.html;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.Ability;
import model.abilities.abilitySlots.AbilitySlot;
import model.abilities.abilitySlots.FilledSlot;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.attributes.Attribute;
import model.attributes.AttributeMod;
import model.enums.Alignment;
import model.enums.Language;
import model.spells.Spell;
import setting.Deity;
import setting.Domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ABCHTMLGenerator {
    private ABCHTMLGenerator() {}

    public static String parseAncestry(Ancestry ancestry) {
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
                String.join(", ", ancestry.getSenses()),
                ancestry.getLanguages().stream().map(Language::name).collect(Collectors.joining(", ")),
                bonusLanguages,
                ancestry.getDesc());
    }

    public static String parseBackground(Background background) {
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
                background.getDesc(),
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

    public static String parsePClass(PClass pClass) {
        List<String> proficiencies = new ArrayList<>();
        for (AbilitySlot slot : pClass.getLevel(1)) {
            if(!(slot instanceof FilledSlot))
                continue;
            Ability currentAbility = slot.getCurrentAbility();
            if(currentAbility.getName().trim().toLowerCase().equals("initial proficiencies")) {
                for (AttributeMod modifier : currentAbility.getModifiers()) {
                    proficiencies.add(modifier.getMod().name() + " in " + modifier.toNiceAttributeString());
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
                pClass.getDesc(),
                String.join(", ", getAbilityMods(pClass.getAbilityMods())),
                pClass.getHP(),
                pClass.getSkillIncrease(),
                String.join("<br>", proficiencies));
    }

    public static String parseDeity(Deity deity) {
        String font = "none";
        if(deity.isHarmFont() && deity.isHealFont()) {
            font = "harm or heal";
        } else if(deity.isHealFont()) {
            font = "heal";
        } else if(deity.isHarmFont()) {
            font = "harm";
        }
        List<String> spells = new ArrayList<>();
        for (Map.Entry<Integer, Spell> entry : deity.getSpells().entrySet()) {
            spells.add(entry.getKey() + ": " + entry.getValue());
        }

        return String.format("<h3>%s (%s) [%s]</h3>" +
                        "<p>%s<p>" +
                        "<b>Edicts</b> %s<br>" +
                        "<b>Anathema</b> %s<br>" +
                        "<b>Areas of Concern</b> %s<br>" +
                        "<b>Follower Alignments</b> %s<br>" +
                        "<b>Divine Font</b> %s<br>" +
                        "<b>Divine Skill</b> %s<br>" +
                        "<b>Favored Weapon</b> %s<br>" +
                        "<b>Domains</b> %s<br>" +
                        "<b>Spells</b> %s<br>",
                deity.getName(),
                deity.getTitle(),
                deity.getDeityAlignment().name(),
                deity.getDescription(),
                deity.getEdicts(),
                deity.getAnathema(),
                deity.getAreasOfConcern(),
                deity.getFollowerAlignments().stream().map(Alignment::name).collect(Collectors.joining(", ")),
                font,
                deity.getDivineSkillChoices().stream().map(Attribute::name).collect(Collectors.joining(" or ")),
                (deity.getFavoredWeapon() != null) ? deity.getFavoredWeapon().getName() : "None",
                deity.getDomains().stream().map(Domain::getName).collect(Collectors.joining(", ")),
                String.join(", ", spells));
    }
}
