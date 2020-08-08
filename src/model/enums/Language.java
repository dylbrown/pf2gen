package model.enums;

public enum Language {
    //Common
    Common, Draconic, Dwarven, Elven, Gnomish, Goblin, Halfling, Jotun, Orcish, Sylvan, Undercommon,
    //Uncommon
    Abyssal, Aklo, Alghollthu, Amurrun, Aquan, Arboreal, Auran, Boggard, Caligni, Celestial, Cyclops, Daemonic,
    Ettin, Giant, Gnoll, Ignan, Infernal, Iruxi, Necril, Protean, Requian, Shadowtongue, Sphinx, Tengu, Terran, Utopian,
    //Secret
    Druidic, Envisioning, Free;

    public static Language[] getChooseable() {
        return new Language[]{
                Common, Draconic, Dwarven, Elven, Gnomish, Goblin, Halfling, Jotun, Orcish, Sylvan, Undercommon,
                //Uncommon
                Abyssal, Aklo, Aquan, Auran, Celestial, Giant, Gnoll, Ignan, Infernal, Necril, Shadowtongue, Terran
        };
    }


    public static Language testValueOf(String s) {
        try {
            Language.valueOf(s);
        }catch (IllegalArgumentException e) {
            System.out.println("Language: " + s);
        }
        return Common;
    }
}
