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
        int i = 0;
        while(i < str.length()-1 && !str.substring(i, i+1).matches("\\w")) i++;
        return str.substring(0, i) + str.substring(i,i+1).toUpperCase() + str.substring(i+1).toLowerCase();
    }
}
