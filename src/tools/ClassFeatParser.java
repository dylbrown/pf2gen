package tools;

import model.enums.Action;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.util.StringUtils.camelCase;

class ClassFeatParser extends SourceParser {
    private StringBuilder currentFeat = new StringBuilder();
    private BufferedWriter classFeats;

    public static void main(String[] args) {
        new ClassFeatParser();
    }

    private ClassFeatParser() {
        try (BufferedReader br = new BufferedReader(new FileReader(new File("generated/inputClassFeats.txt")))) {
            classFeats = new BufferedWriter(new FileWriter(new File("generated/classFeats.txt")));
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
            classFeats.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int currentPointInList = 0;
    private Action cost = null;
    private boolean descStarted = false;
    private void parseLine(String line) throws IOException {
        if(line.contains("@")){
            if(currentFeat.length() > 0)
                currentFeat.append("</Description></Ability>");
            currentPointInList = 0;
            classFeats.write(currentFeat.toString());
            currentFeat = new StringBuilder();
            cost = null;
            descStarted = false;
            return;
        }
        switch (currentPointInList){
            case 0:
                String[] split = line.split(" FEAT ");
                if(split.length == 1) return;
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
                currentFeat.append("<Ability name=\"").append(camelCase(split[0].replaceAll("\\[.*]", "")))
                        .append("\" level=\"").append(split[1]).append("\"");
                if(cost != null)
                    currentFeat.append(" cost=\"").append(cost.toString()).append("\">");
                else currentFeat.append(">");
                break;
            case 1:
                split = line.split(" ");
                List<String> specialTraits = new ArrayList<>(Arrays.asList(split));
                if(specialTraits.size() > 0)
                    currentFeat.append("<Traits>").append(String.join(", ", specialTraits)).append("</Traits>");
                break;
            default:
                if(line.trim().equals("")) return;
                if(line.startsWith("Trigger")){
                    currentFeat.append("<Trigger>").append(line.replaceAll("Trigger", "").trim())
                            .append("</Trigger>");
                }else if(line.startsWith("Requirements") || line.startsWith("Requirement(s)")){
                    currentFeat.append("<Requirements>").append(line.replaceAll("Requirement\\(?s\\)?", "").trim())
                            .append("</Requirements>");
                }else if(line.startsWith("Frequency")){
                    currentFeat.append("<Frequency>").append(line.replaceAll("Frequency", "").trim())
                            .append("</Frequency>");
                }else if(line.trim().startsWith("Prerequisite(s)")){
                    List<String> prereqs = new ArrayList<>();
                    List<String> requirements = new ArrayList<>();
                    for (String s : line.replaceAll("Prerequisite\\(s\\)", "").trim().split(",")) {
                        if(s.contains(" in "))
                            requirements.add(camelCase(s));
                        else
                            prereqs.add(camelCase(s));
                    }
                    if(prereqs.size()>0)
                        currentFeat.append("<Prerequisites>").append(String.join(", ", prereqs))
                                .append("</Prerequisites>");
                    if(requirements.size()>0)
                        currentFeat.append("<Requires>").append(String.join(", ", requirements)).append("</Requires>");
                }else{
                    if(!descStarted){
                        descStarted = true;
                        currentFeat.append("<Description>");
                    }
                    currentFeat.append(line.trim());
                }
                break;
        }
        currentPointInList++;
    }
}
