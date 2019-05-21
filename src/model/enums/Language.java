package model.enums;

public enum Language {
    //Common
    Common, Draconic, Dwarven, Elven, Gnomish, Goblin, Halfling, Jotun, Orcish, Sylvan, Undercommon,
    //Uncommon
    Abyssal, Aklo, Aquan, Auran, Celestial, Giant, Gnoll, Ignan, Infernal, Necril, Shadowtongue, Terran,
    //Secret
    Druidic, Free;

    public static Language[] getChooseable() {
        return new Language[]{
                Common, Draconic, Dwarven, Elven, Gnomish, Goblin, Halfling, Jotun, Orcish, Sylvan, Undercommon,
                //Uncommon
                Abyssal, Aklo, Aquan, Auran, Celestial, Giant, Gnoll, Ignan, Infernal, Necril, Shadowtongue, Terran
        };
    }
}
