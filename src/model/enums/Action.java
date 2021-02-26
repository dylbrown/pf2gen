package model.enums;

import model.util.StringUtils;

public enum Action {
    Free, Reaction, One, Two, Three, Exploration, Downtime;

    public static Action robustValueOf(String s) {
        Action result = null;
        try{
            result = valueOf(StringUtils.camelCaseWord(s.replaceAll("( |^|\\A)[Aa]ction.*", "").trim()));
        }catch (Exception e) {
            switch (s) {
                case "1":
                    result = One;
                    break;
                case "2":
                    result = Two;
                    break;
                case "3":
                    result = Three;
                    break;
            }
        }
        return result;
    }

    public String getNiceString() {
        switch (this) {
            case Free:
                return "Free Action";
            case Reaction:
                return "Reaction";
            case One:
                return "One Action";
            case Two:
                return "Two Actions";
            case Three:
                return "Three Actions";
        }
        return "";
    }

    public String getIcon() {
        switch (this) {
            case Free:
                return "Ⓕ";
            case Reaction:
                return "Ⓡ";
            case One:
                return "①";
            case Two:
                return "②";
            case Three:
                return "③";
        }
        return "";
    }
}
