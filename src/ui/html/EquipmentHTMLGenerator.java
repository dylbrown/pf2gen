package ui.html;

import model.abilities.Ability;
import model.enums.ArmorProficiency;
import model.enums.Trait;
import model.equipment.Equipment;
import model.equipment.armor.Armor;
import model.equipment.armor.Shield;
import model.equipment.weapons.RangedWeapon;
import model.equipment.weapons.Weapon;

import java.util.stream.Collectors;

import static model.util.StringUtils.generateCostString;

public class EquipmentHTMLGenerator {

    public static String parse(Equipment equipment) {
        if (equipment instanceof Weapon)
            return generateWeaponText((Weapon) equipment);
        else if (equipment instanceof Armor)
            return generateArmorText((Armor) equipment);
        return generateItemText(equipment);
    }


    private static String generateItemText(Equipment equipment) {
        if(equipment == null) return null;
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(equipment.getName()).append("<div style=\"padding-right:5px; float:right\">");
        text.append(equipment.getCategory()).append("</div></h3><br><b>Cost</b> ");
        text.append(generateCostString(equipment.getValue()));
        if(equipment.getWeight() > 0)
            text.append("; <b>Bulk</b> ").append(equipment.getPrettyWeight());
        if(equipment.getHands() > 0)
            text.append("; <b>Hands</b> ").append(equipment.getHands());
        if(equipment.getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(equipment.getTraits().stream().map(Trait::toString).collect(Collectors.joining(", ")));
        text.append("<hr>").append(equipment.getDescription());
        for (Ability ability : equipment.getAbilities()) {
            text.append("<hr>").append(AbilityHTMLGenerator.parse(ability));
        }

        return text.toString();
    }

    private static String generateArmorText(Armor armor) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(armor.getName()).append("<div style=\"padding-right:5px; float:right\">");
        if(armor.getProficiency() != ArmorProficiency.Shield)
            text.append(armor.getProficiency()).append(" Armor</div></h3><br><b>Cost</b> ");
        else
            text.append("Shield</div></h3><br><b>Cost</b> ");
        text.append(generateCostString(armor.getValue())).append("; <b>Bulk</b> ");
        text.append(armor.getPrettyWeight()).append("<br><b>AC Bonus</b> ");
        if(armor.getAC() >= 0)
            text.append("+");
        text.append(armor.getAC());
        if(armor instanceof Shield) {
            text.append("; <b>Speed Penalty</b> ");
            if (armor.getSpeedPenalty() < 0)
                text.append(Math.abs(armor.getSpeedPenalty())).append(" ft.");
            else
                text.append("—");
            text.append("<br><b>Hardness</b> ").append(((Shield) armor).getHardness());
            text.append("; <b>HP(BT)</b> ").append(((Shield) armor).getHP()).append("(");
            text.append(((Shield) armor).getBT()).append(")");
        }else{
            text.append("; <b>Dex Cap</b> +").append(armor.getMaxDex()).append("<br><b>ACP</b> ");
            if (armor.getACP() < 0)
                text.append(armor.getACP());
            else
                text.append("—");
            text.append("; <b>Speed Penalty</b> ");
            if (armor.getSpeedPenalty() < 0)
                text.append(Math.abs(armor.getSpeedPenalty())).append(" ft.");
            else
                text.append("—");
            text.append("<br><b>Strength</b> ");
            if (armor.getStrength() > 0)
                text.append(armor.getStrength());
            else
                text.append("—");
            text.append("; <b>Group</b> ").append(armor.getGroup().getName());
        }
        if(armor.getArmorTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(armor.getArmorTraits().stream().map(Trait::getName).collect(Collectors.joining(", ")));
        return text.toString();
    }

    private static String generateWeaponText(Weapon weapon) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(weapon.getName()).append("<div style=\"padding-right:5px; float:right\">");
        text.append(weapon.getProficiency().toString()).append(" Weapon</div></h3><br><b>Cost</b> ");
        text.append(generateCostString(weapon.getValue())).append("; <b>Bulk</b> ");
        text.append(weapon.getPrettyWeight()).append("; <b>Hands</b> ").append(weapon.getHands());
        text.append("<br><b>Damage</b> ").append(weapon.getDamage()).append("; <b>Group</b> ").append(weapon.getGroup().getName());
        if(weapon instanceof RangedWeapon){
            text.append("<br><b>Range</b> ").append(((RangedWeapon) weapon).getRange());
            text.append("; <b>Reload</b> ").append(((RangedWeapon) weapon).getReload());
        }
        if(weapon.getWeaponTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(weapon.getWeaponTraits().stream().map(Trait::getName).collect(Collectors.joining(", ")));
        return text.toString();
    }
}
