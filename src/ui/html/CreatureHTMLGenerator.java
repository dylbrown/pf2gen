package ui.html;

import model.abilities.Ability;
import model.ability_scores.AbilityScore;
import model.attributes.BaseAttribute;
import model.creatures.Attack;
import model.creatures.Creature;
import model.creatures.CreatureItem;
import model.creatures.CreatureSpellList;
import model.enums.Trait;
import model.spells.Spell;
import model.util.StringUtils;

import java.util.stream.Collectors;

public class CreatureHTMLGenerator {
    public static String parse(Creature creature) {
        StringBuilder builder = new StringBuilder();
        builder.append("<p><b>").append(creature.getName()).append(" - Creature ")
                .append(creature.getLevel()).append("</b><br>");
        if(creature.getFamily() != null)
            builder.append("<b>Family</b> ").append(creature.getFamily().getName()).append("<br>");
        builder.append("<b>Traits</b> ").append(
                creature.getTraits().stream()
                        .map(Trait::toString)
                        .collect(Collectors.joining(", "))
        ).append("</br>");
        builder.append("<b>Source</b> pg. ").append(creature.getPage())
                .append("; <b>Perception</b> ");
        String perception = signed(creature.getModifiers().get(BaseAttribute.Perception));
            builder.append(perception).append("<br>");
        builder.append("<b>Languages</b> ").append(
                creature.getLanguages().stream().map(Enum::toString).collect(Collectors.joining(", "))
        ).append("<br>");
        builder.append("<b>Str</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Str))
                .append(", <b>Dex</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Dex))
                .append(", <b>Con</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Con))
                .append(", <b>Int</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Int))
                .append(", <b>Wis</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Wis))
                .append(", <b>Cha</b> ").append(creature.getAbilityModifiers().get(AbilityScore.Cha))
                .append("<br>");
        if(creature.getItems().size() > 0)
            builder.append("<b>Items</b> ")
                    .append(creature.getItems().stream()
                            .map(CreatureItem::getItemName)
                            .collect(Collectors.joining(", "))
                    );
        if(creature.getMiscAbilities().size() > 0) {
            for (Ability ability : creature.getMiscAbilities()) {
                builder.append("<br>").append(AbilityHTMLGenerator.parseSingleLine(ability));
            }
        }
        builder.append("<hr>");
        builder.append("<b>AC</b> ").append(creature.getAC());
        if(!creature.getACMods().isBlank())
            builder.append(" (").append(creature.getACMods()).append(")");
        builder.append("; <b>Fort</b> ").append(signed(creature.getModifiers().get(BaseAttribute.Fortitude)))
                .append("; <b>Ref</b> ").append(signed(creature.getModifiers().get(BaseAttribute.Reflex)))
                .append("; <b>Will</b> ").append(signed(creature.getModifiers().get(BaseAttribute.Will)));
        if(!creature.getSaveMods().isBlank())
            builder.append("; ").append(creature.getSaveMods());
        builder.append("<br><b>HP</b> ").append(creature.getHP());
        if(!creature.getHealthMods().isBlank())
            builder.append(" (").append(creature.getHealthMods()).append(")");
        if(!creature.getImmunities().isBlank())
            builder.append("; <b>Immunities</b> ").append(creature.getImmunities());
        if(!creature.getResistances().isBlank())
            builder.append("; <b>Resistances</b> ").append(creature.getResistances());
        if(!creature.getWeaknesses().isBlank())
            builder.append("; <b>Weaknesses</b> ").append(creature.getWeaknesses());
        if(creature.getDefensiveAbilities().size() > 0) {
            for (Ability ability : creature.getDefensiveAbilities()) {
                builder.append("<br>").append(AbilityHTMLGenerator.parseSingleLine(ability));
            }
        }
        builder.append("<hr>");
        builder.append("<b>Speed</b> ").append(creature.getSpeed());
        for (Attack attack : creature.getAttacks()) {
            builder.append("<br>").append("<b>").append(attack.getAttackType()).append("</b> ")
                    .append(attack.getName()).append(" ").append(signed(attack.getModifier()))
                    .append(" (").append(attack.getTraits().stream()
                                .map(Trait::getName)
                                .collect(Collectors.joining(", ")))
            .append("), <b>Damage</b> ").append(attack.getDamage());
        }
        for (CreatureSpellList list : creature.getSpells()) {
            builder.append("<b>").append(list.getIndex()).append("</b> DC ").append(list.getDC())
                    .append(", attack ").append(signed(list.getAttack())).append("; ");
            for(int i = list.getHighestLevelCanCast(); i >= -1; i--) {
                if(list.getSpellsKnown(i).size() == 0)
                    continue;
                builder.append("<b>").append(StringUtils.spellLevelOrdinal(i));
                if(i <= 0)
                    builder.append(" (").append(StringUtils.spellLevelOrdinal(list.getHeightenedLevel()))
                        .append(")");
                builder.append("</b> ");
                boolean first = true;
                for (Spell spell : list.getSpellsKnown(i)) {
                    if(!first) builder.append(", ");
                    else first = false;
                    builder.append(spell.getName());
                    String specialInfo = list.getSpecialInfo(spell, i);
                    if(!specialInfo.isBlank())
                        builder.append(" ").append(specialInfo);
                }
                builder.append("; ");
            }
        }
        if(creature.getOffensiveAbilities().size() > 0) {
            for (Ability ability : creature.getOffensiveAbilities()) {
                builder.append("<br>").append(AbilityHTMLGenerator.parseSingleLine(ability));
            }
        }
        return builder.toString();
    }

    private static String signed(int modifier) {
        return (modifier >= 0) ? "+"+modifier : String.valueOf(modifier);
    }
}
