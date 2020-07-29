package model.enums;

public enum Type {
    Initial, Ancestry, Background, Class, Feat, Heritage, General, Skill, Dedication, None, Apex,
    Choice, // These are not feats in the game, they are things like Rogue's Rackets
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
}
