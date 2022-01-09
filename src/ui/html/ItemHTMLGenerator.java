package ui.html;

import model.abilities.Ability;
import model.enums.ArmorProficiency;
import model.enums.Trait;
import model.items.Item;
import model.items.armor.Armor;
import model.items.armor.Shield;
import model.items.weapons.RangedWeapon;
import model.items.weapons.Weapon;

import java.util.stream.Collectors;

import static model.util.StringUtils.generateCostString;

public class ItemHTMLGenerator {

    public static String parse(Item item) {
        if (item.hasExtension(Weapon.class))
            return generateWeaponText(item.getExtension(Weapon.class));
        else if (item.hasExtension(Armor.class))
            return generateArmorText(item.getExtension(Armor.class));
        return generateItemText(item);
    }


    private static String generateItemText(Item item) {
        if(item == null) return null;
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(item.getName()).append("<div style=\"padding-right:5px; float:right\">");
        text.append(item.getCategory()).append("</div></h3><br><b>Source</b> ");
        text.append(item.getSource());
        text.append("<br><b>Cost</b> ");
        text.append(generateCostString(item.getValue()));
        if(item.getWeight() > 0)
            text.append("; <b>Bulk</b> ").append(item.getPrettyWeight());
        if(item.getHands() > 0)
            text.append("; <b>Hands</b> ").append(item.getHands());
        if(item.getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(item.getTraits().stream().map(Trait::toString).collect(Collectors.joining(", ")));
        text.append("<hr>").append(item.getDescription());
        for (Ability ability : item.getAbilities()) {
            text.append("<hr>").append(AbilityHTMLGenerator.parse(ability));
        }

        return text.toString();
    }

    private static String generateArmorText(Armor armor) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(armor.getItem().getName()).append("<div style=\"padding-right:5px; float:right\">");
        if(armor.getProficiency() != ArmorProficiency.Shield)
            text.append(armor.getProficiency()).append(" Armor</div></h3><br><b>Source</b> ");
        else
            text.append("Shield</div></h3><br><b>Source</b> ");
        text.append(armor.getItem().getSource());
        text.append("<br><b>Cost</b> ");
        text.append(generateCostString(armor.getItem().getValue())).append("; <b>Bulk</b> ");
        text.append(armor.getItem().getPrettyWeight()).append("<br><b>AC Bonus</b> ");
        if(armor.getAC() >= 0)
            text.append("+");
        text.append(armor.getAC());
        if(armor.getItem().hasExtension(Shield.class)) {
            Shield shield = armor.getItem().getExtension(Shield.class);
            text.append("; <b>Speed Penalty</b> ");
            if (armor.getSpeedPenalty() < 0)
                text.append(Math.abs(armor.getSpeedPenalty())).append(" ft.");
            else
                text.append("—");
            text.append("<br><b>Hardness</b> ").append(shield.getHardness());
            text.append("; <b>HP(BT)</b> ").append(shield.getHP()).append("(");
            text.append(shield.getBT()).append(")");
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
        if(armor.getItem().getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(armor.getItem().getTraits().stream().map(Trait::getName).collect(Collectors.joining(", ")));
        text.append("<hr>").append(armor.getItem().getDescription());
        for (Ability ability : armor.getItem().getAbilities()) {
            text.append("<hr>").append(AbilityHTMLGenerator.parse(ability));
        }
        return text.toString();
    }

    private static String generateWeaponText(Weapon weapon) {
        StringBuilder text = new StringBuilder();
        text.append("<p><h3 style='display:inline;'>").append(weapon.getItem().getName()).append("<div style=\"padding-right:5px; float:right\">");
        text.append(weapon.getProficiency().toString()).append(" Weapon</div></h3><br><b>Source</b> ");
        text.append(weapon.getItem().getSource());
        text.append("<br><b>Cost</b> ");
        text.append(generateCostString(weapon.getItem().getValue())).append("; <b>Bulk</b> ");
        text.append(weapon.getItem().getPrettyWeight()).append("; <b>Hands</b> ").append(weapon.getItem().getHands());
        text.append("<br><b>Damage</b> ").append(weapon.getDamage()).append("; <b>Group</b> ").append(weapon.getGroup().getName());
        if(weapon.getItem().hasExtension(RangedWeapon.class)){
            RangedWeapon ranged = weapon.getItem().getExtension(RangedWeapon.class);
            text.append("<br><b>Range</b> ").append(ranged.getRange());
            text.append("; <b>Reload</b> ").append(ranged.getReload());
        }
        if(weapon.getItem().getTraits().size() > 0)
            text.append("<br><b>Traits</b> ").append(weapon.getItem().getTraits().stream().map(Trait::getName).collect(Collectors.joining(", ")));
        text.append("<hr>").append(weapon.getItem().getDescription());
        for (Ability ability : weapon.getItem().getAbilities()) {
            text.append("<hr>").append(AbilityHTMLGenerator.parse(ability));
        }
        return text.toString();
    }
}
