package model.enums;

import model.util.StringUtils;

public enum Type {
    Initial, Ancestry, Background, Class, Feat, Heritage, General, Skill, Dedication, None, Apex,
    Choice, // These are not feats in the game, they are things like Rogue's Rackets
    ClassFeature, // These are feats that are shared across classes like Weapon Specialization
    Misc, Defensive, Offensive, // These are used for creature abilities
    Item,
    Five,Ten,Fifteen,Twenty;

    public static Type get(int level) {
        switch(level) {
            case 5:
                return Five;
            case 10:
                return Ten;
            case 15:
                return Fifteen;
            case 20:
                return Twenty;
            default: return Initial;
        }
    }

    public String fancyName() {
        switch (this) {
            case Five:
                return "Level 5";
            case Ten:
                return "Level 10";
            case Fifteen:
                return "Level 15";
            case Twenty:
                return "Level 20";
            default:
                return name();
        }
    }

    public static Type robustValueOf(String category) {
        try {
            return valueOf(StringUtils.camelCase(category.trim()));
        } catch (IllegalArgumentException e) {
            if(category.equalsIgnoreCase("classfeature"))
                return ClassFeature;
            return null;
        }
    }
}
