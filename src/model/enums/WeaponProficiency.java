package model.enums;

import model.attributes.Attribute;
import model.attributes.BaseAttribute;

public enum WeaponProficiency {
    Unarmed, Simple, Martial, Advanced, Ability;

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
