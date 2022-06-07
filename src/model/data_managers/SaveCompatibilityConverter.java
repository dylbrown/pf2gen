package model.data_managers;

import model.util.Pair;

import java.util.Arrays;
import java.util.List;

public class SaveCompatibilityConverter {
    private SaveCompatibilityConverter(){}

    public static void updateTo(Pair<List<String>, Integer> lines, int version) {
        switch (version) {
            case 1:
                lines.first.addAll(0, Arrays.asList(
                        "PF2Gen Save - v0",
                        "Sources",
                        " - Core Rulebook"
                ));
                break;
            case 2:
                boolean inventory = false;
                for (int i = 0; i < lines.first.size(); i++) {
                    String line = lines.first.get(i);
                    if(!inventory) {
                        if(line.startsWith("Inventory")) {
                            inventory = true;
                        }
                        continue;
                    }
                    if(line.startsWith(" @ ")) {
                        lines.first.add(i + 1, "    Runes");
                        i++;
                    }else if(line.startsWith("   - ")) {
                        lines.first.set(i, "     - " + line.substring(5));
                    }else if(!line.startsWith(" - "))
                        break;
                }

                break;
            case 3:
                int spellsKnown = lines.first.indexOf("Spells Known");
                lines.first.add(spellsKnown, "Formulas");
                break;
            case 4:
                int formulas = lines.first.indexOf("Formulas");
                lines.first.set(formulas, "Formulas Bought");
                spellsKnown = lines.first.indexOf("Spells Known");
                lines.first.add(spellsKnown, "Formulas Granted");
                break;
            case 5:
                for (int i = 0; i < lines.first.size(); i++) {
                    String line = lines.first.get(i);
                    if(line.startsWith("name =")) {
                        break;
                    }
                    if(line.contains("=")) {
                        lines.first.set(i, "Variant Rules");
                        break;
                    }
                }
                break;
        }
        lines.first.set(0,"PF2Gen Save - v" + version);
    }
}
