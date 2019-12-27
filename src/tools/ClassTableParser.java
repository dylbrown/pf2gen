package tools;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static model.util.StringUtils.camelCase;
import static model.util.StringUtils.camelCaseWord;

class ClassTableParser extends SourceParser {
    //private BufferedWriter outputFile;
    public static void main(String[] args) {
        new ClassTableParser();
    }
    ClassTableParser() {
//        try {
//            outputFile = new BufferedWriter(new FileWriter(new File("generated/classTable.txt")));
//            BufferedReader br = new BufferedReader(new FileReader(new File("generated/inputTable.txt")));
//            String line;
//            while ((line = br.readLine()) != null) {
//                parseTableLine(line);
//            }
//            outputFile.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private Map<String, StringBuilder> features = new HashMap<>();


    String className = null;
    private boolean isCaster = false;
    String parseTableLine(int level, String abilities) throws IOException {
        if(className == null || abilities.equals("Class Features")){
            return "";
        }
        if(abilities.toLowerCase().contains("spellcasting")) isCaster = true;
        int spellCount = (className.equals("sorcerer")) ? 3 : 2;
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append("<FeatureList level=\"").append(level).append("\">\n");
        for (String feature : abilities.split(", ?")) {
            switch (feature.trim().toLowerCase().replaceAll(className, "class")){
                case "ability boosts":
                    listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"Ability Boosts\">\n" +
                            "\t\t<Ability abilityBoosts=\"4\"/>\n" +
                            "\t</AbilitySlot>\n");
                    break;
                case "ancestry and background":
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"Ancestry Feat\" name=\"Ancestry Feat\"/>\n");
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"Heritage Feat\" name=\"Heritage\"/>\n");
                    break;
                case "ancestry feat":
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"Ancestry Feat\" name=\"Ancestry Feat\"/>\n");
                    break;
                case "class feat":
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"Class Feat\" name=\"")
                            .append(camelCaseWord(className)).append(" Feat\"/>\n");
                    break;
                case "general feat":
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"General Feat\" name=\"General Feat\" />\n");
                    break;
                case "skill feat":
                    listBuilder.append("\t<AbilitySlot state=\"feat\" type=\"Skill Feat\" name=\"Skill Feat\" />\n");
                    break;
                case "skill increase":
                    listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"Skill Increase\">\n" +
                            "\t\t<Ability skillIncrease=\"true\"/>\n" +
                            "\t</AbilitySlot>\n");
                    break;
                case "initial proficiencies":
                    listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"Initial Proficiencies\">\n" +
                            "\t\t<Ability>\n" +
                            "\t\t\t<AttributeMods Proficiency=\"Trained\"> </AttributeMods>\n" +
                            "\t\t\t<AttributeMods Proficiency=\"Expert\"> </AttributeMods>\n" +
                            "\t\t</Ability>\n" +
                            "\t</AbilitySlot>\n");
                    break;
                default:
                    if(feature.trim().matches("\\d(nd|st|rd|th)-level spells")) {
                        String spellLevel = feature.replaceAll("[^\\d]*", "");
                        listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"").append(feature.trim()).append("\">\n")
                                .append("\t\t<Ability>\n")
                                .append("\t\t\t<SpellSlots level=\"").append(spellLevel).append("\" count=\"").append(spellCount).append("\"/>\n")
                                .append("\t\t</Ability>\n")
                                .append("\t</AbilitySlot>\n");
                    }else{
                        listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"")
                                .append(camelCase(feature.trim())).append("\">\n")
                                .append("\t\t<Ability>\n").append("\t\t\t<Description>").append(getDescription(feature)).append("</Description>\n")
                                .append("\t\t</Ability>\n").append("\t\t</AbilitySlot>\n");
                    }
                    break;
            }
        }
        if(isCaster && level % 2 == 0 && level < 19) {
            listBuilder.append("\t<AbilitySlot state=\"filled\" name=\"Spell Slots\">\n")
                    .append("\t\t<Ability>\n")
                    .append("\t\t\t<SpellSlots level=\"").append(level / 2).append("\" count=\"1\"/>\n")
                    .append("\t\t</Ability>\n")
                    .append("\t</AbilitySlot>\n");
        }
        listBuilder.append("</FeatureList>\n");
        return listBuilder.toString();
    }

    String getDescription(String feature) {
        return "";
    }
}
