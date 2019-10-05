package model.util;

public class StringUtils {
    private StringUtils(){}

    public static String camelCase(String str) {
        String[] split = str.split(" ");
        for(int i=0; i<split.length;i++) {
            split[i] = camelCaseWord(split[i]);
        }
        return String.join(" ", split);
    }

    public static String camelCaseWord(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
