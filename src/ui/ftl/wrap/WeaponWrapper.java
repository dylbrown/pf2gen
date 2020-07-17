package ui.ftl.wrap;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import model.equipment.weapons.Weapon;
import ui.Main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WeaponWrapper implements TemplateHashModel {

    private final Weapon weapon;
    private final ObjectWrapper wrapper;
    private static final Map<String, Function<Weapon, Object>> map = new HashMap<>();

    static {
        map.put("attack", (weapon)->Main.character.combat().getAttackMod(weapon));
        map.put("damage", (weapon)->Main.character.combat().getDamage(weapon));
        map.put("traits", Weapon::getWeaponTraits);
    }

    WeaponWrapper(Weapon weapon, ObjectWrapper wrapper) {
        this.weapon = weapon;
        this.wrapper = wrapper;
    }

    @Override
    public TemplateModel get(String s) throws TemplateModelException {
        if(map.get(s.toLowerCase()) == null) {
            for (Method method : weapon.getClass().getMethods()) {
                if (method.getName().toLowerCase().equals("get" + s.toLowerCase())
                        && method.getParameterCount() == 0) {
                    try {
                        return wrapper.wrap(method.invoke(weapon));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return wrapper.wrap(map.get(s.toLowerCase()).apply(weapon));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String toString() {
        return weapon.toString();
    }
}
