package model.enums;

public enum Slot {
    //Magic
    Head, Headband, Eyes,
    Shoulders, Neck, Chest, Body,
    Belt, Wrists, Hands, Ring,
    Feet,
    //Weapon
    OneHand, TwoHands,
    PrimaryHand, OffHand,
    Armor,
    //Special
    Slotless, Carried, None;

    public static String getPrettyName(Slot slot) {
        switch (slot) {
            case Head: return "Head";
            case Headband: return "Headband";
            case Eyes: return "Eyes";
            case Shoulders: return "Shoulders";
            case Neck: return "Neck";
            case Chest: return "Chest";
            case Body: return "Body";
            case Belt: return "Belt";
            case Wrists: return "Wrists";
            case Hands: return "Hands";
            case Ring: return "Ring";
            case Feet: return "Feet";
            case OneHand: return "One Hand";
            case TwoHands: return "Two Hands";
            case PrimaryHand: return "Primary Hand";
            case OffHand: return "Off Hand";
            case Armor: return "Armor";
            case Slotless: return "Slotless";
            case Carried: return "Carried";
            default:
            case None: return "None";
        }
    }
}
