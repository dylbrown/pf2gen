package model.enums;

import model.attributes.BaseAttribute;

public enum ArmorProficiency {
    Unarmored, Light, Medium, Heavy, Shield;

    public BaseAttribute toAttribute() {
        switch(this) {
            default:
            case Unarmored:
                return BaseAttribute.Unarmored;
            case Light:
                return BaseAttribute.LightArmor;
            case Medium:
                return BaseAttribute.MediumArmor;
            case Heavy:
                return BaseAttribute.HeavyArmor;
            case Shield:
                return BaseAttribute.Shields;
        }
    }
}
