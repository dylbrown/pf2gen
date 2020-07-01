package model.util;

import org.apache.commons.text.WordUtils;

public class StringUtils {
    private StringUtils(){}

    public static String clean(String src) {
        return src.toLowerCase().trim().replaceAll(" ", "_");
    }

    public static String camelCase(String str) {
        return WordUtils.capitalizeFully(str);
    }

    public static String camelCaseWord(String str) {
        return WordUtils.capitalizeFully(str);
    }

    public static String generateCostString(double cost) {
        if(cost == 0) return "";
        if(Math.floor(cost) != cost)
            return (int)(cost * 10) + " cp";
        else if(cost < 100 || Math.floor(cost/10) != cost/10)
            return (int)cost + " sp";
        else
            return (int)(cost / 10) + " gp";
    }

    public static String intialism(String str) {
        StringBuilder initialism = new StringBuilder();
        for (String s : str.split(" ")) {
            initialism.append(Character.toUpperCase(s.charAt(0)));
        }
        return initialism.toString();
    }

    public static String unclean(String filename) {
        return camelCase(filename.replaceAll("_", " "));
    }

    public static boolean containsIgnoreCase(String string, String searchString) {
        if(string.length() < searchString.length()) return false;
        if(searchString.length() == 0) return true;
        for(int i = 0; i <= string.length() - searchString.length(); i++)
            if(string.regionMatches(true, i, searchString, 0, searchString.length()))
                return true;
        return false;
    }

    public static String getInBrackets(String str) {
        return getInBrackets(str, '(', ')');
    }

    public static Pair<String, String> getInAndOutBrackets(String str) {
        int start = str.indexOf('(');
        int end = str.indexOf(')');
        return new Pair<>(str.substring(0, start), str.substring(start + 1, end));
    }

    public static String getInBrackets(String str, char start, char end) {
        int startIndex = str.indexOf(start);
        int endIndex = str.indexOf(end);
        return str.substring(startIndex + 1, endIndex);
    }
}
