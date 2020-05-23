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
import model.attributes.Attribute;
import model.attributes.SkillIncrease;
import model.enums.Alignment;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.enums.Type;
import model.equipment.Equipment;
import model.equipment.ItemCount;
import model.equipment.SearchItem;
import model.equipment.runes.Rune;
import model.equipment.runes.runedItems.RunedEquipment;
import model.spells.Spell;
import model.util.Pair;
import model.util.Triple;
import model.xml_parsers.abc.AncestriesLoader;
import model.xml_parsers.abc.BackgroundsLoader;
import model.xml_parsers.abc.PClassesLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static ui.Main.character;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SaveLoadManager {
    public static void save(File file){
        PrintWriter out = null;
        if (file != null) {
        try {
            out = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (out != null) {
            writeOutLine(out, "name = "+character.getName());
            writeOutLine(out, "player = "+character.getPlayer());
            if(character.getHeight() != null) writeOutLine(out, "height = "+character.getHeight());
            if(character.getWeight() != null) writeOutLine(out, "weight = "+character.getWeight());
            if(character.getAge() != null) writeOutLine(out, "age = "+character.getAge());
            if(character.getHair() != null) writeOutLine(out, "hair = "+character.getHair());
            if(character.getEyes() != null) writeOutLine(out, "eyes = "+character.getEyes());
            if(character.getGender() != null) writeOutLine(out, "gender = "+character.getGender());
            if(character.getAlignment() != null) writeOutLine(out, "alignment = "+character.getAlignment());
            writeOutLine(out, "ancestry = "+character.getAncestry().getName());
            writeOutLine(out, "background = "+character.getBackground().getName());
            writeOutLine(out, "class = "+character.getPClass().getName());
            writeOutLine(out, "level = "+character.getLevel());
            writeOutLine(out, "abilityChoices = " + character.scores().getAbilityScoreChoices().stream().filter(
                    choice -> choice.getTarget() != AbilityScore.Free).map(
                    choice -> {
                        if(choice.getChoices().size() < 6)
                            return choice.getType() + ":"
                                    + choice.getChoices().stream().map(Enum::toString).collect(Collectors.joining("/"))+
                                    ":" + choice.getTarget().toString();
                        else
                            return choice.getType() + ":" + choice.getTarget().toString();
                    }
                ).collect(Collectors.joining(", ")));

            writeOutLine(out, "Skill Choices");
            for (Map.Entry<Integer, Set<SkillIncrease>> level : character.attributes().getSkillChoices().entrySet()) {
                writeOutLine(out, " - " + level.getKey()+":"+level.getValue().stream().map(s->s.getAttr().toString()).collect(Collectors.joining(", ")));
            }

            //Decisions
            writeOutLine(out, "Decisions");
            for (Choice decision : character.decisions().getDecisions()) {
                if(decision.getSelections().size() == 0) continue;
                StringBuilder builder = new StringBuilder();
                builder.append(decision.toString()).append(" : ");
                boolean first = true;
                for (Object selection : decision.getSelections()) {
                    if(first) first = false;
                    else builder.append(" ^ ");
                    builder.append(selection.toString());
                }
                writeOutLine(out, " - " + builder.toString());
            }
            writeOutLine(out, "money = " + character.inventory().getMoney());
            writeOutLine(out, "Inventory");
            for (ItemCount item : character.inventory().getItems().values()) {
                if(item.stats() instanceof RunedEquipment) {
                    RunedEquipment stats = (RunedEquipment) item.stats();
                    writeOutLine(out, " @ "+item.getCount()+" "+stats.getBaseItem().getName());
                    List<Rune> fundamentalRunes = new ArrayList<>();
                    List<Rune> propertyRunes = new ArrayList<>();
                    for (Object o : stats.getRunes().list()) {
                        if(o instanceof Rune) {
                            Rune rune = (Rune) o;
                            if(rune.isFundamental())
                                fundamentalRunes.add(rune);
                            else
                                propertyRunes.add(rune);
                        }
                    }
                    for (Rune rune : fundamentalRunes)
                        writeOutLine(out, "   - " + rune.getName());
                    for (Rune rune : propertyRunes)
                        writeOutLine(out, "   - " + rune.getName());
                }else writeOutLine(out, " - "+item.getCount()+" "+item.stats().getName());
            }
            writeOutLine(out, "Equipped");
            //Print items in specific slots
            for (Map.Entry<Slot, ItemCount> entry : character.inventory().getEquipped().entrySet()) {
                writeOutLine(out, " - "+entry.getKey()+": "+entry.getValue().getCount()+" "+entry.getValue().stats().getName());
            }

            //Spells
            writeOutLine(out, "Spells Known");
            int i = 0;
            for (ObservableList<Spell> spells : character.spells().getSpellsKnown()) {
                writeOutLine(out, " - Level "+i);
                for (Spell spell : spells) {
                    writeOutLine(out, "   - "+ spell.getName());
                }
                i++;
            }


            out.close();
        }
        }
    }

    private static void writeOutLine(PrintWriter out, String s) {
        out.write(s);
        out.println();
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
            String curr = nextLine(lines);
            while(curr.contains("=")) {
                String[] split = curr.split(" ?= ?", 2);
                String afterEq = split[1];
                if(afterEq.length() == 0) afterEq = "";
                switch (split[0].trim()) {
                    case "name": character.setName(afterEq); break;
                    case "player": character.setPlayer(afterEq); break;
                    case "height": character.setHeight(afterEq); break;
                    case "weight": character.setWeight(afterEq); break;
                    case "age": character.setAge(afterEq); break;
                    case "eyes": character.setEyes(afterEq); break;
                    case "hair": character.setHair(afterEq); break;
                    case "gender": character.setGender(afterEq); break;
                    case "alignment": character.setAlignment(Alignment.valueOf(afterEq)); break;
                    case "ancestry":
                        for (Ancestry ancestry : AncestriesLoader.instance().parse()) {
                            if (ancestry.getName().equals(afterEq)) {
                                character.setAncestry(ancestry);
                                break;
                            }
                        } break;
                    case "background":
                        for (Background background : BackgroundsLoader.instance().parse()) {
                            if (background.getName().equals(afterEq)) {
                                character.setBackground(background);
                                break;
                            }
                        } break;
                    case "class":
                        for (PClass pClass : PClassesLoader.instance().parse()) {
                            if (pClass.getName().equals(afterEq)) {
                                character.setClass(pClass);
                                break;
                            }
                        } break;
                    case "level":
                        int lvl = Integer.parseInt(afterEq);
                        while (character.getLevel() < lvl)
                            character.levelUp();
                        break;
                    case "abilityChoices":
                        if (!afterEq.trim().equals("")) {
                            //Parse data into more usable format
                            Set<Triple<Type, AbilityScore, List<AbilityScore>>> mods = Arrays.stream(afterEq.split(", "))
                                    .map(SaveLoadManager::makeTriple).collect(Collectors.toSet());

                            //iterate through all current choices and set them if they have a match.
                            for (AbilityModChoice modChoice : character.scores().getAbilityScoreChoices()) {
                                Iterator<Triple<Type, AbilityScore, List<AbilityScore>>> iterator = mods.iterator();
                                while (iterator.hasNext()) {
                                    Triple<Type, AbilityScore, List<AbilityScore>> next = iterator.next();
                                    if (modChoice.matches(next.first, next.third)) {
                                        character.scores().choose(modChoice, next.second);
                                        iterator.remove();
                                        break;
                                    }
                                }
                            }
                        } break;
                }
                curr = nextLine(lines);
            }
            //Skill Increase Choices
            character.attributes().resetSkills();
            while(true) {
                String s = nextLine(lines);
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                String[] split = s.split(" ?: ?");
                if(split.length < 2) continue;
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
                        if(selections.contains(option.toString())) {
                            int oldSize = decision.getSelections().size();
                            decision.add(option);
                            if(decision.getSelections().size() > oldSize)
                                successes++;
                            if(decision.getSelections().size() == selections.size())
                                decisionStringMap.put(decision.toString(), null);
                            break;
                        }
                    }
                }
                if(successes == 0) break;
            }

            //Items
            character.inventory().reset();
            String money = nextLineEq(lines);
            character.inventory().setMoney(Double.parseDouble(money));
            lines.second++; // Skip Section Header
            character.inventory().setMode(BuySellMode.Cashless);
            while (true) {
                String s;
                try {
                    s = nextLine(lines);
                } catch (RuntimeException e) {
                    break;
                }
                if (s.startsWith(" - ")) {
                    String[] split = s.substring(3).split(" ", 2);
                    SortedSet<Equipment> tailSet = EquipmentManager.getEquipment().tailSet(new SearchItem(split[1]));
                    if (!tailSet.isEmpty()) {
                        if (tailSet.first().getName().equals(split[1]))
                            character.inventory().buy(tailSet.first(), Integer.parseInt(split[0]));
                    }
                } else if (s.startsWith(" @ ")) {
                    String itemName = s.substring(3).split(" ", 2)[1];
                    Equipment item;
                    SortedSet<Equipment> tailSet = EquipmentManager.getEquipment().tailSet(new SearchItem(itemName));
                    if (!tailSet.isEmpty()) {
                        if (tailSet.first().getName().equals(itemName)) {
                            item = tailSet.first();
                            character.inventory().buy(item, 1);
                            upgradeItem(item, lines);
                        }
                    }
                } else {
                    lines.second--;
                    break;
                }
            }
            character.inventory().setMode(BuySellMode.FullPrice);

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
                for (ItemCount value : character.inventory().getItems().values()) {
                    if(value.stats().getName().equals(split[1])) {
                        character.inventory().equip(value.stats(),Slot.valueOf(slotSplit[0]),  Integer.parseInt(split[0]));
                        break;
                    }
                }
            }

            //Spells
            lines.second++; // Skip Section Header
            character.spells().reset();
            for(int i=0; i<=10; i++){
                lines.second++;
                while(true) {
                    String s;
                    try { s = nextLine(lines); }
                    catch(RuntimeException e) { break; }
                    if(!s.startsWith("   - ")) {
                        lines.second--;
                        break;
                    }
                    character.spells().addSpell(AllSpells.find(s.substring(5)));
                }
            }

            System.out.println(System.currentTimeMillis()-start+" ms");
        }
    }

    private static void upgradeItem(Equipment item, Pair<List<String>, Integer> lines) {
        String s;
        while (true) {
            Rune rune;
            try { s = nextLine(lines); }
            catch(RuntimeException e) { return; }
            if(!s.startsWith("   - ")) {
                lines.second--;
                return;
            }
            String itemName = s.substring(5);
            SortedSet<Equipment> tailSet = EquipmentManager.getEquipment().tailSet(new SearchItem(itemName));
            if (!tailSet.isEmpty()) {
                if(tailSet.first().getName().equals(itemName)) {
                    if(tailSet.first() instanceof Rune) {
                        rune = (Rune) tailSet.first();
                        character.inventory().buy(rune, 1);
                        item = character.inventory().tryToAddRune(item, rune).second;
                    }
                }
            }
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
