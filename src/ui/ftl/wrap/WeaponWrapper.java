package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import model.equipment.weapons.Weapon;
import ui.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WeaponWrapper extends GenericWrapper<Weapon> {
    private static final Map<String, Function<Weapon, Object>> map = new HashMap<>();

    static {
        map.put("attack", (weapon)->Main.character.combat().getAttackMod(weapon));
        map.put("damage", (weapon)->Main.character.combat().getDamage(weapon));
        map.put("traits", Weapon::getWeaponTraits);
    }

    public WeaponWrapper(Weapon weapon, ObjectWrapper wrapper) {
        super(weapon, wrapper);
    }

    @Override
    boolean hasSpecialCase(String s) {
        return map.containsKey(s);
    }

    @Override
    Object getSpecialCase(String s, Weapon weapon) {
        return map.get(s).apply(weapon);
    }
}
