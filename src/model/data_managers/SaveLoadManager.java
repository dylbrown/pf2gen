package model.data_managers;

import javafx.collections.ObservableList;
import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Attribute;
import model.enums.Slot;
import model.enums.Type;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.SearchItem;
import model.util.Pair;
import model.util.Triple;
import model.xml_parsers.AncestriesLoader;
import model.xml_parsers.BackgroundsLoader;
import model.xml_parsers.PClassesLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static ui.Main.character;

public class SaveLoadManager {
    public static void save(File file){
        Map<String, Object> map = new HashMap<>();
        map.put("name", character.getName());
        map.put("ancestry", character.getAncestry().getName());
        map.put("background", character.getBackground().getName());
        map.put("class", character.getPClass().getName());
        map.put("level", character.getLevel());
        map.put("abilityChoices", character.scores().getAbilityScoreChoices());
        map.put("decisions", character.decisions().getDecisions().stream().filter(
                choice -> choice.getSelections().size()>0).map(
                (choice)->{
                    StringBuilder builder = new StringBuilder();
                    builder.append(choice.toString());
                    for (Object selection : choice.getSelections()) {
                        builder.append(selection.toString());
                    }
                    return builder.toString();
                }).collect(
                Collectors.toList()));
        map.put("skillChoices", character.attributes().getSkillChoices());
        HashMap<String, Integer> items = new HashMap<>();
        character.inventory().getItems().values().forEach((item)-> items.put(item.getName(), item.getCount()));
        map.put("inventory", items);

            writeOutLine(out, "Inventory");
            for (ItemCount item : character.inventory().getItems().values()) {
                writeOutLine(out, " - "+item.getCount()+" "+item.stats().getName());
            }
            writeOutLine(out, "Equipped");
            //Print items in specific slots
            for (Map.Entry<Slot, ItemCount> entry : character.inventory().getEquipped().entrySet()) {
                writeOutLine(out, " - "+entry.getKey()+": "+entry.getValue().getCount()+" "+entry.getValue().stats().getName());
            }

        if (file != null) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(map);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void load(File file) {
        if (file != null) {
            List<String> lineList;
            try {
                lineList = Files.readAllLines(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Pair<List<String>, Integer> lines= new Pair<>(lineList, 0);
            character.reset();
            long start = System.currentTimeMillis();
            //Name
            character.setName(nextLineEq(lines));
            character.setPlayer(nextLineEq(lines));

            //Ancestry
            String anc = nextLineEq(lines);
            for (Ancestry ancestry : AncestriesLoader.instance().parse()) {
                if(ancestry.getName().equals(anc)){
                    character.setAncestry(ancestry);
                    break;
                }
            }

            //Background
            String bac = nextLineEq(lines);
            for (Background background : BackgroundsLoader.instance().parse()) {
                if(background.getName().equals(bac)){
                    character.setBackground(background);
                    break;
                }
            }

            //PClass
            String cla = nextLineEq(lines);
            for (PClass pClass : PClassesLoader.instance().parse()) {
                if(pClass.getName().equals(cla)){
                    character.setClass(pClass);
                    break;
                }
            }

            //Level
            Integer lvl = Integer.valueOf(nextLineEq(lines));
            while(character.getLevel() < lvl)
                character.levelUp();

            //Ability Score Choices
            //Parse data into more usable format
            Set<Triple<Type, AbilityScore, List<AbilityScore>>> mods = Arrays.stream(nextLineEq(lines).split(", "))
                    .map(SaveLoadManager::makeTriple).collect(Collectors.toSet());

            //iterate through all current choices and set them if they have a match.
            for (AbilityModChoice modChoice : character.scores().getAbilityScoreChoices()) {
                Iterator<Triple<Type, AbilityScore, List<AbilityScore>>> iterator = mods.iterator();
                while(iterator.hasNext()){
                    Triple<Type, AbilityScore, List<AbilityScore>> next = iterator.next();
                    if(modChoice.matches(next.first, next.third)){
                        character.scores().choose(modChoice, next.second);
                        iterator.remove();
                        break;
                    }
                }
            }

            //Skill Increase Choices
            character.attributes().resetSkills();
            lines.second++; // Skip Section Header
            while(true) {
                String s = nextLine(lines);
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                String[] split = s.split(" ?: ?");
                for (String attribute : split[1].split(" ?, ?")) {
                    character.attributes().advanceSkill(Attribute.valueOf(attribute));
                }
            }

            //Decisions
            lines.second++; // Skip Section Header
            Map<String, String> decisionStringMap = new HashMap<>();
            while(true) {
                String s = nextLine(lines);
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                String[] split = s.split(" ?: ?");
                decisionStringMap.put(split[0].substring(3), split[1]);
            }
            ObservableList<Choice> decisions = character.decisions().getUnmadeDecisions();
            while(decisions.size() > 0){
                int successes = 0;
                for (Choice decision : decisions) {
                    if(decisionStringMap.get(decision.toString()) == null) continue;
                    List<String> selections = Arrays.asList(decisionStringMap.get(decision.toString()).split(" ?\\^ ?"));
                    List options;
                    if(decision instanceof ChoiceList)
                        options = ((ChoiceList) decision).getOptions();
                    else if(decision instanceof FeatSlot)
                        options = character.abilities().getOptions((FeatSlot)decision);
                    else options = Collections.emptyList();
                    for (Object option : options) {
                        if(selections.contains(option.toString()) && !decision.viewSelections().contains(option)){//TODO: Handle multi-take
                            character.addSelection(decision, option);
                            successes++;
                            if(decision.viewSelections().size() == selections.size())
                                decisionStringMap.put(decision.toString(), null);
                            break;
                        }
                    }
                }
                if(successes == 0) break;
            }

            //Items
            character.inventory().reset();
            lines.second++; // Skip Section Header
            while(true) {
                String s;
                try { s = nextLine(lines); }
                catch(RuntimeException e) { break; }
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                String[] split = s.substring(3).split(" ", 2);
                SortedSet<Equipment> tailSet = EquipmentManager.getEquipment().tailSet(new SearchItem(split[1]));
                if (!tailSet.isEmpty()) {
                    if(tailSet.first().getName().equals(split[1]))
                        character.inventory().buy(tailSet.first(), Integer.valueOf(split[0]));
                }
            }

            //Equipping
            lines.second++; // Skip Section Header
            while(true) {
                String s;
                try { s = nextLine(lines); }
                catch(RuntimeException e) { break; }
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                String[] slotSplit = s.substring(3).split(" ?: ?", 2);
                String[] split = slotSplit[1].split(" ", 2);
                SortedSet<Equipment> tailSet = EquipmentManager.getEquipment().tailSet(new SearchItem(split[1]));
                if (!tailSet.isEmpty()) {
                    if(tailSet.first().getName().equals(split[1]))
                        character.inventory().equip(tailSet.first(),Slot.valueOf(slotSplit[0]),  Integer.valueOf(split[0]));
                }
            }

            System.out.println(System.currentTimeMillis()-start+" ms");
        }
    }

    private static String nextLineEq(Pair<List<String>, Integer> pair) {
        return nextLine(pair).split(" ?= ?", 2)[1];
    }

    private static String nextLine(Pair<List<String>, Integer> pair) {
        List<String> lines = pair.first;
        Integer loadIndex = pair.second;

        if(loadIndex == lines.size()) throw new RuntimeException("Expected more lines");
        String line;
        do{
            line = lines.get(loadIndex++);
        }while(line.equals("") && loadIndex < lines.size());

        pair.second = loadIndex;
        return line;
    }

    private static Triple<Type, AbilityScore, List<AbilityScore>> makeTriple(String s) {
        String[] choice = s.split(":", 3);
        Type type = Type.valueOf(choice[0]);
        AbilityScore target = AbilityScore.valueOf(choice[choice.length-1]);
        List<AbilityScore> choices;
        if(choice.length == 3) {
            choices = Arrays.stream(choice[1].split("/")).map(AbilityScore::valueOf).collect(Collectors.toList());
        }else{
            choices = AbilityScore.scores();
        }
        return new Triple<>(type, target, choices);
    }
}
