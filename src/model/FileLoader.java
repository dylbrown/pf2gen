package model;

import model.abc.Ancestry;
import model.abc.Background;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityModChoice;
import model.abilityScores.AbilityScore;
import model.enums.*;

import java.io.*;
import java.util.*;

public class FileLoader {
    private static final File ancestriesPath = new File("data/ancestries");
    private static final File backgroundsPath = new File("data/backgrounds");
    private static FileLoader instance;
    private List<Ancestry> ancestries;
    private List<Background> backgrounds;
    static{
        instance = new FileLoader();
    }
    public static List<Ancestry> getAncestries() {
        return instance.getAnc();
    }
    public static List<Background> getBackgrounds() {
        return instance.getBack();
    }

    private List<Ancestry> getAnc() {
        if(ancestries == null) {
            ancestries = new ArrayList<>();
            for (File file : Objects.requireNonNull(ancestriesPath.listFiles())) {
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    Map<String, String> data = new HashMap<>();
                    String line;
                    while((line = reader.readLine()) != null) {
                        String[] datum = parseLine(line);
                        data.put(datum[0], datum[1]);
                    }
                    String name = camelCase(data.get("name"));
                    int hp = Integer.parseInt(data.get("hp"));
                    Size size = Size.valueOf(camelCase(data.get("size")));
                    int speed = Integer.parseInt(data.get("speed"));
                    List<AbilityMod> abilityMods = getAbilityMods(data.computeIfAbsent("bonuses", (key) -> ""), data.computeIfAbsent("penalties", (key) -> ""), AbilityType.Ancestry);
                    ancestries.add(new Ancestry(name, hp, size, speed, abilityMods));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ancestries;
    }

    private List<Background> getBack() {
        if(backgrounds == null) {
            backgrounds = new ArrayList<>();
            for (File file : Objects.requireNonNull(backgroundsPath.listFiles())) {
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    Map<String, String> data = new HashMap<>();
                    String line;
                    while((line = reader.readLine()) != null) {
                        String[] datum = parseLine(line);
                        data.put(datum[0], datum[1]);
                    }
                    String name = camelCase(data.get("name"));
                    List<AbilityMod> abilityMods = getAbilityMods(data.computeIfAbsent("bonuses", (key) -> ""), "", AbilityType.Background);
                    if(camelCase(data.get("skill").trim()).substring(0, 4).equals("Lore")) {
                        backgrounds.add(new Background(name, abilityMods, Attribute.Lore, camelCase(data.get("skill").trim()).substring(4).trim().replaceAll("[()]", "")));
                    }else{
                        Attribute skill = Attribute.valueOf(camelCase(data.get("skill").trim()));
                        backgrounds.add(new Background(name, abilityMods, skill));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return backgrounds;
    }

    private List<AbilityMod> getAbilityMods(String bonuses, String penalties, AbilityType type) {
        List<AbilityMod> abilityMods = new ArrayList<>();

        String[] split = bonuses.split(",");
        for(int i=0;i<split.length;i++) {
            split[i] = split[i].trim();
            if(split[i].equals("")) continue;
            String[] eachScore = split[i].split("or");
            if(eachScore.length == 1) {
                AbilityScore abilityScore = AbilityScore.valueOf(camelCase(split[i]));
                if(abilityScore != AbilityScore.Free)
                    abilityMods.add(new AbilityMod(abilityScore, true, type));
                else
                    abilityMods.add(new AbilityModChoice(type));
            }else{
                abilityMods.add(new AbilityModChoice(Arrays.asList(parseChoices(eachScore)), type));
            }

        }

        split = penalties.split(",");
        for(int i=0;i<split.length;i++) {
            if(split[i].trim().equals("")) continue;
            split[i] = camelCase(split[i].trim());
            abilityMods.add(new AbilityMod(AbilityScore.valueOf(split[i]), false, type));
        }

        return abilityMods;
    }

    private AbilityScore[] parseChoices(String[] eachScore) {
        AbilityScore[] scores = new AbilityScore[eachScore.length];
        for(int i=0;i<eachScore.length;i++) {
            scores[i] = AbilityScore.valueOf(camelCase(eachScore[i].trim()));
        }
        return scores;
    }

    private String camelCase(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String[] parseLine(String line) {
        String[] split = line.split(":");
        for(int i=0;i<split.length;i++) {
            split[i] = split[i].toLowerCase().trim();
        }
        return split;
    }
}
