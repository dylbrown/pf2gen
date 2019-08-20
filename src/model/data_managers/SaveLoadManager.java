package model.data_managers;

import model.abc.Ancestry;
import model.abc.Background;
import model.abc.PClass;
import model.abilities.abilitySlots.Choice;
import model.abilities.abilitySlots.ChoiceList;
import model.abilities.abilitySlots.FeatSlot;
import model.ability_scores.AbilityModChoice;
import model.enums.Attribute;
import model.equipment.Equipment;
import model.xml_parsers.AncestriesLoader;
import model.xml_parsers.BackgroundsLoader;
import model.xml_parsers.PClassesLoader;

import java.io.*;
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
                choice -> choice.getChoice()!=null).map(
                (choice -> choice.toString()+";"+choice.getChoice().toString())).collect(
                Collectors.toList()));
        map.put("skillChoices", character.attributes().getSkillChoices());
        HashMap<String, Integer> items = new HashMap<>();
        character.inventory().getItems().values().forEach((item)-> items.put(item.getName(), item.getCount()));
        map.put("inventory", items);


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
        Map<String, Object> map;
        if (file != null) {
            character.reset();
            long start = System.currentTimeMillis();
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
                map = (Map<String, Object>) in.readObject();
                in.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                map = new HashMap<>();
            }
            //Name
            character.setName((String) map.get("name"));
            //Ancestry
            String anc = (String) map.get("ancestry");
            for (Ancestry ancestry : AncestriesLoader.instance().parse()) {
                if(ancestry.getName().equals(anc)){
                    character.setAncestry(ancestry);
                    break;
                }
            }
            //Background
            String bac = (String) map.get("background");
            for (Background background : BackgroundsLoader.instance().parse()) {
                if(background.getName().equals(bac)){
                    character.setBackground(background);
                    break;
                }
            }
            //PClass
            String cla = (String) map.get("class");
            for (PClass pClass : PClassesLoader.instance().parse()) {
                if(pClass.getName().equals(cla)){
                    character.setClass(pClass);
                    break;
                }
            }
            //Level
            Integer lvl = (Integer) map.get("level");
            while(character.getLevel() < lvl)
                character.levelUp();
            //Ability Score Choices
            List<AbilityModChoice> mods = new ArrayList<>((List<AbilityModChoice>) map.get("abilityChoices"));
            for (AbilityModChoice modChoice : character.scores().getAbilityScoreChoices()) {
                Iterator<AbilityModChoice> iterator = mods.iterator();
                while(iterator.hasNext()){
                    AbilityModChoice next = iterator.next();
                    if(next.matches(modChoice)){
                        character.scores().choose(modChoice, next.getTarget());
                        iterator.remove();
                        break;
                    }
                }
            }
            //AbilitySlot Decisions
            List<String> decisionStrings = (List<String>) map.get("decisions");
            Map<String, String> decisionStringMap = new HashMap<>();
            for (String s : decisionStrings) {
                String[] split = s.split(";");
                decisionStringMap.put(split[0], split[1]);
            }
            List<Choice> decisions = character.decisions().getUnmadeDecisions();
            while(decisions.size() > 0){
                int successes = 0;
                for(Choice decision: decisions) {
                    String choice = decisionStringMap.get(decision.toString());
                    if(choice != null){
                        List options;
                        if(decision instanceof ChoiceList)
                            options = ((ChoiceList) decision).getOptions();
                        else if(decision instanceof FeatSlot)
                            options = character.abilities().getOptions((FeatSlot)decision);
                        else options = Collections.emptyList();
                        for (Object option : options) {
                            if(option.toString().equals(choice)){
                                character.choose(decision, option);
                                successes++;
                                break;
                            }
                        }
                    }
                }
                if(successes == 0) break;
                decisions = character.decisions().getUnmadeDecisions();
            }
            character.attributes().resetSkills();
            //Skill Increase Choices
            SortedMap<Integer, Set<Attribute>> skillChoices = (SortedMap<Integer, Set<Attribute>>) map.get("skillChoices");
            for (Set<Attribute> choices : skillChoices.values()) {
                for (Attribute choice : choices) {
                    character.attributes().advanceSkill(choice);
                }
            }

            //Items
            character.inventory().reset();
            HashMap<String, Integer> items = (HashMap<String, Integer>) map.get("inventory");
            for (Equipment equipment : EquipmentManager.getEquipment()) {
                if (items.get(equipment.getName()) != null)
                    character.inventory().buy(equipment, items.get(equipment.getName()));
            }

            System.out.println(System.currentTimeMillis()-start+" ms");
        }
    }
}
