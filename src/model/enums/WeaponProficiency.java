package model.enums;

import model.attributes.Attribute;
import model.attributes.BaseAttribute;

public enum WeaponProficiency {
    Unarmed, Simple, Martial, Advanced;

    public static WeaponProficiency robustValueOf(String s) {
        switch (s.toLowerCase().trim()) {
            case "unarmed": return Unarmed;
            case "simple": return Simple;
            case "martial": return Martial;
            case "advanced": return Advanced;
            default: return null;
        }
    }

    public Attribute toAttribute() {
        switch (this) {
            case Unarmed:
                return BaseAttribute.Unarmed;
            case Simple:
                return BaseAttribute.SimpleWeapons;
            case Martial:
                return BaseAttribute.MartialWeapons;
            case Advanced:
                return BaseAttribute.AdvancedWeapons;
        }
        throw new EnumConstantNotPresentException(BaseAttribute.class, toString());
    }
}
