package tools;

import model.enums.Action;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static model.util.StringUtils.camelCase;

class FeatListParser extends SourceParser {
    private StringBuilder currentFeat = new StringBuilder();
    private BufferedWriter skill;
    private BufferedWriter general;

    public static void main(String[] args) {
        new FeatListParser();
    }

    private FeatListParser() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("generated/featListSource.txt")))) {
            skill = new BufferedWriter(new FileWriter(new File("generated/skill.txt")));
            general = new BufferedWriter(new FileWriter(new File("generated/general.txt")));
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
    private Action cost = null;
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
            cost = null;
            return;
        }
        switch (currentPointInList){
            case 0:
                String[] split = line.split(" FEAT ");
                if(line.contains("[ONE-ACTION]")){
                    cost = Action.One;
                }else if(line.contains("[TWO-ACTIONS]")){
                    cost = Action.Two;
                }else if(line.contains("[THREE-ACTIONS]")){
                    cost = Action.Three;
                }else if(line.contains("[REACTION]")){
                    cost = Action.Reaction;
                }else if(line.contains("[FREE-ACTION]")){
                    cost = Action.Free;
                }
                currentFeat.append("<Ability name=\"").append(camelCase(split[0].replaceAll("\\[[^\\[\\]]*]", "")))
                        .append("\" level=\"").append(split[1]).append("\"");
                if(cost != null)
                    currentFeat.append(" cost=\"").append(cost.toString()).append("\">");
                else currentFeat.append(">");
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
}
