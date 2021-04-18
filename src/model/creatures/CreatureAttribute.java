package model.creatures;

import model.attributes.Attribute;

public interface CreatureAttribute {
    Attribute getAttribute();
    int getModifier();
    String getInfo();
}
