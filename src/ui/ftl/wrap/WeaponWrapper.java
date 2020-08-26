package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import model.equipment.weapons.Weapon;
import model.player.PC;

import java.util.Arrays;

public class WeaponWrapper extends GenericWrapper<Weapon> {
    private final PC character;

    public WeaponWrapper(Weapon weapon, ObjectWrapper wrapper, PC character) {
        super(weapon, wrapper);
        this.character = character;
    }

    @Override
    boolean hasSpecialCase(String s) {
        return Arrays.asList("attack", "damage", "traits").contains(s);
    }

    @Override
    Object getSpecialCase(String s, Weapon weapon) {
        switch (s) {
            case "attack":
                return character.combat().getAttackMod(weapon);
            case "damage":
                return character.combat().getDamage(weapon);
            case "traits":
                return weapon.getWeaponTraits();
            default:
                return null;
        }
    }
}
