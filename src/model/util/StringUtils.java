package model.util;

public class StringUtils {
    private StringUtils(){}

    public static String clean(String src) {
        return src.toLowerCase().trim().replaceAll(" ", "_");
    }

    public static String camelCase(String str) {
        if(str.length() == 0) return str;
        String[] split = str.split(" ");
        for(int i=0; i<split.length;i++) {
            split[i] = camelCaseWord(split[i]);
        }
        return String.join(" ", split);
    }

    public static String camelCaseWord(String str) {
        if(str.length() == 0) return str;
        int i = 0;
        while(i < str.length()-1 && !str.substring(i, i+1).matches("\\w")) i++;
        return str.substring(0, i) + str.substring(i,i+1).toUpperCase() + str.substring(i+1).toLowerCase();
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
}
