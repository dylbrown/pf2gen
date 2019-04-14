package model;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.Class;
import model.abilityScores.AbilityMod;
import model.abilityScores.AbilityModChoice;
import model.abilityScores.AbilityScore;
import model.enums.AbilityType;
import model.enums.Attribute;
import model.enums.Size;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileLoader {
    private static final File ancestriesPath = new File("data/ancestries");
    private static final File backgroundsPath = new File("data/backgrounds");
    private static final File classesPath = new File("data/classes");
    private static FileLoader instance;
    private List<Ancestry> ancestries;
    private List<Background> backgrounds;
    private List<Class> classes;
    private DocumentBuilder builder;

    static{
        instance = new FileLoader();
        try {
            instance.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        assert instance.builder != null;
    }
    public static List<Ancestry> getAncestries() {
        return instance.getAnc();
    }
    public static List<Background> getBackgrounds() {
        return instance.getBack();
    }
    public static List<Class> getClasses() {
        return instance.getCls();
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

    private List<Class> getCls() {
        if(classes == null) {
            classes = new ArrayList<>();
            for (File file : Objects.requireNonNull(classesPath.listFiles())) {
                Document doc = null;
                try {
                    doc = builder.parse(file);
                } catch (IOException | SAXException e) {
                    e.printStackTrace();
                }
                assert doc != null;
                NodeList classProperties = doc.getElementsByTagName("class").item(0).getChildNodes();

                String name = ""; int hp = 0; AbilityMod abilityMod = null; Map<Integer, List<AbilitySlot>> table = new HashMap<>();

                for(int i=0; i<classProperties.getLength(); i++) {
                    if(classProperties.item(i).getNodeType() != Node.ELEMENT_NODE)
                        continue;
                    Element curr = (Element) classProperties.item(i);
                    switch(curr.getTagName()){
                        case "Name":
                            name = curr.getTextContent().trim();
                            break;
                        case "HP":
                            hp = Integer.parseInt(curr.getTextContent().trim());
                            break;
                        case "KeyAbilityChoices":
                            if(curr.getElementsByTagName("AbilityScore").getLength() == 1) {
                                abilityMod = new AbilityMod(AbilityScore.valueOf(camelCase(curr.getElementsByTagName("AbilityScore").item(0).getTextContent())), true, AbilityType.Class);
                            }else{
                                List<AbilityScore> scores = new ArrayList<>();
                                for(int j=0; j<curr.getElementsByTagName("AbilityScore").getLength(); j++) {
                                    scores.add(AbilityScore.valueOf(camelCase(curr.getElementsByTagName("AbilityScore").item(j).getTextContent())));
                                }
                                abilityMod = new AbilityModChoice(scores, AbilityType.Class);
                            }
                            break;
                        case "FeatureList":
                            int level = Integer.parseInt(curr.getAttribute("level"));
                            break;
                    }
                }

                classes.add(new Class(name, hp, abilityMod, table));


            }
        }
        return classes;
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
