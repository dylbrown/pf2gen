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
        }
    }
}
