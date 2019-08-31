package tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FeatListParser {
    private StringBuilder currentFeat = new StringBuilder();
    private BufferedWriter skill;
    private BufferedWriter general;

    public static void main(String[] args) {
        new FeatListParser();
    }

    private FeatListParser() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("source.txt")))) {
            skill = new BufferedWriter(new FileWriter(new File("skill.txt")));
            general = new BufferedWriter(new FileWriter(new File("general.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
            skill.close();
            general.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int currentPointInList = 0;
    private boolean isSkill = false;
    private void parseLine(String line) throws IOException {
        if(line.contains("@")){
            currentFeat.append("</Description></Ability>");
            currentPointInList = 0;
            if(!isSkill){
                general.write(currentFeat.toString());
            }else
                skill.write(currentFeat.toString());
            isSkill = false;
            currentFeat = new StringBuilder();
            return;
        }
        switch (currentPointInList){
            case 0:
                String[] split = line.split(" FEAT ");
                currentFeat.append("<Ability name=\"").append(camelCase(split[0]))
                        .append("\" level=\"").append(split[1]).append("\">");
                break;
            case 1:
                split = line.split(" ");
                List<String> specialTraits = new ArrayList<>();
                for (String s : split) {
                    if(s.equals("Skill"))
                        isSkill = true;
                    else if(!s.equals("General"))
                        specialTraits.add(s);
                }
                if(specialTraits.size() > 0)
                    currentFeat.append("<Traits>").append(String.join(", ", specialTraits)).append("</Traits>");
                break;
            case 2:
                if(!line.substring(0, 15).equals("Prerequisite(s)")){
                    currentPointInList = 3;
                    parseLine(line);
                    break;
                }
                currentFeat.append("<Requires>").append(camelCase(line.substring(16))).append("</Requires>");
                break;
            case 3:
                currentFeat.append("<Description>").append(line.trim());
                break;
            default:
                currentFeat.append(line.trim());
                break;
        }
        currentPointInList++;
    }

    String camelCase(String str) {
        String[] split = str.split(" ");
        for(int i=0; i<split.length;i++) {
            split[i] = camelCaseWord(split[i]);
        }
        return String.join(" ", split);
    }

    String camelCaseWord(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
