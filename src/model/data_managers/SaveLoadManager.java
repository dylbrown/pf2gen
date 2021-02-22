package model.data_managers;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.ability_slots.Choice;
import model.ability_slots.ChoiceList;
import model.ability_slots.FeatSlot;
import model.attributes.Attribute;
import model.attributes.SkillIncrease;
import model.data_managers.sources.Source;
import model.data_managers.sources.SourcesLoader;
import model.enums.Alignment;
import model.enums.BuySellMode;
import model.enums.Slot;
import model.enums.Type;
import model.items.*;
import model.items.runes.ArmorRune;
import model.items.runes.Rune;
import model.items.runes.WeaponRune;
import model.items.runes.runedItems.Runes;
import model.player.PC;
import model.player.VariantManager;
import model.spells.Spell;
import model.spells.SpellList;
import model.spells.heightened.HeightenedSpell;
import model.util.ObjectNotFoundException;
import model.util.Pair;
import model.util.StringUtils;
import model.util.Triple;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class SaveLoadManager {
    private static final String HEADER = "PF2Gen Save - v";
    private static final int VERSION = 5;

    private SaveLoadManager() {}
    public static void save(PC character, Writer out) throws IOException {
        if (out != null) {
            writeOutLine(out, HEADER+SaveLoadManager.VERSION);
            writeOutLine(out, "Sources");
            for (Source source : character.sources().getSources()) {
                writeOutLine(out, " - "+source.getName());
            }
            writeOutLine(out, "Variant Rules");
            for (Map.Entry<VariantManager.Variant, Boolean> entry : character.variants().getMap().entrySet()) {
                if(entry.getValue())
                    writeOutLine(out, " - "+entry.getKey());
            }
            writeOutLine(out, "name = "+character.qualities().get("name"));
            writeOutLine(out, "player = "+character.qualities().get("player"));
            writeOutLine(out, "alignment = "+character.getAlignment());
            writeOutLine(out, "deity = "+character.getDeity());
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

            writeOutLine(out, "Qualities");
            for (Map.Entry<String, StringProperty> entry : character.qualities().map().entrySet()) {
                writeOutLine(out, entry.getKey() + " = " + entry.getValue().get());
            }

            writeOutLine(out, "Skill Choices");
            for (Map.Entry<Integer, Set<SkillIncrease>> level : character.attributes().getSkillChoices().entrySet()) {
                writeOutLine(out, " - " + level.getKey()+":"+level.getValue().stream().map(s->s.getAttr().toString()).collect(Collectors.joining(", ")));
            }

            //Decisions
            writeOutLine(out, "Decisions");
            for (Choice<?> decision : character.decisions().getDecisions()) {
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
                if(item.stats() instanceof ItemInstance) {
                    ItemInstance stats = (ItemInstance) item.stats();
                    writeOutLine(out, " @ " + item.getCount() + " " + stats.getRawName());
                    Runes<?> runes = Runes.getRunes(item.stats());
                    if(runes != null) {
                        List<Rune> fundamentalRunes = new ArrayList<>();
                        List<Rune> propertyRunes = new ArrayList<>();

                        for (Item o : runes.list()) {
                            Rune rune = o.getExtension(ArmorRune.class);
                            if (rune == null)
                                rune = o.getExtension(WeaponRune.class);
                            if (rune != null) {
                                if (rune.isFundamental())
                                    fundamentalRunes.add(rune);
                                else
                                    propertyRunes.add(rune);
                            }
                        }
                        writeOutLine(out, "    Runes");
                        for (Rune rune : fundamentalRunes)
                            writeOutLine(out, "     - " + rune.getItem().getName());
                        for (Rune rune : propertyRunes)
                            writeOutLine(out, "     - " + rune.getItem().getName());
                    }
                    ItemInstanceChoices choices = stats.getExtension(ItemInstanceChoices.class);
                    if(choices != null) {
                        for (Map.Entry<String, Choice<?>> entry : choices.getChoices().entrySet()) {
                            writeOutLine(out, "    " + entry.getKey());
                            for (Object selection : entry.getValue().getSelections()) {
                                writeOutLine(out, "     - " + selection.toString());
                            }

                        }

                    }
                }else writeOutLine(out, " - "+item.getCount()+" "+item.stats().getName());
            }
            writeOutLine(out, "Equipped");
            //Print items in specific slots
            for (Map.Entry<Slot, ItemCount> entry : character.inventory().getEquipped().entrySet()) {
                writeOutLine(out, " - "+entry.getKey()+": "
                        +entry.getValue().getCount()+" "+entry.getValue().stats().getName());
            }
            for (ItemCount itemCount : character.inventory().getCarried().values()) {
                writeOutLine(out, " - Carried: "+itemCount.getCount()+" "+itemCount.stats().getName());
            }

            //Formulas
            writeOutLine(out, "Formulas Bought");
            for (Item formula : character.inventory().getFormulasBought()) {
                writeOutLine(out, " - "+formula.getRawName());
            }
            writeOutLine(out, "Formulas Granted");
            for (Item formula : character.inventory().getFormulasGranted()) {
                writeOutLine(out, " - "+formula.getRawName());
            }


            //Spells
            writeOutLine(out, "Spells Known");
            for (Map.Entry<String, SpellList> entry : character.spells().getSpellLists().entrySet()) {
                writeOutLine(out, " - " + entry.getKey());
                int i = 0;
                for (ObservableList<Spell> spells : entry.getValue().getSpellsKnown()) {
                    writeOutLine(out, "   - Level "+i);
                    for (Spell spell : spells) {
                        writeOutLine(out, "     - "+ spell.getRawName());
                    }
                    i++;
                }
            }


            out.close();
        }
    }

    private static void writeOutLine(Writer out, String s) throws IOException {
        out.write(s);
        out.write(System.lineSeparator());
    }

    public static void load(PC character, List<String> lineList) {
            Pair<List<String>, Integer> lines= new Pair<>(lineList, 0);
            character.reset();
            long start = System.currentTimeMillis();
            String curr = nextLine(lines);
            int version = 0;
            if(curr.startsWith(HEADER)) {
                version = Integer.parseInt(curr.substring(HEADER.length()));
            }
            if(version > VERSION)
                throw new RuntimeException("Save is from newer version of software!!!");
            while (version < VERSION) {
                version++;
                SaveCompatibilityConverter.updateTo(lines, version);
            }

            //Load Sources
            lines.second++; // Skip Section Header
            curr = nextLine(lines);
            while(curr.startsWith(" - ")) {
                character.sources().add(SourcesLoader.instance().find(curr.substring(3)));
                curr = nextLine(lines);
            }

            //Load Variant Rules
            curr = nextLine(lines);
            while(curr.startsWith(" - ")) {
                switch(VariantManager.Variant.valueOf(curr.substring(3).trim())) {
                    case FreeArchetype:
                        character.variants().setFreeArchetype(true);
                        break;
                    default:
                        throw new RuntimeException("Variant rule not supported in save/load!");
                }
                curr = nextLine(lines);
            }

            // Load simple details
            while(curr.contains("=")) {
                String[] split = curr.split(" ?= ?", 2);
                String afterEq = split[1];
                if(afterEq.length() == 0) afterEq = "";
                switch (split[0].trim()) {
                    case "name": character.qualities().set("name", afterEq); break;
                    case "player": character.qualities().set("player", afterEq); break;
                    case "alignment": character.setAlignment(Alignment.valueOf(afterEq)); break;
                    case "deity":
                        try {
                            character.setDeity(character.sources().deities().find(afterEq));
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "ancestry":
                        if(!StringUtils.clean(afterEq).equals("no_ancestry")) {
                            try {
                                character.setAncestry(character.sources().ancestries().find(afterEq));
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "background":
                        if(!StringUtils.clean(afterEq).equals("no_background")) {
                            try {
                                character.setBackground(character.sources().backgrounds().find(afterEq));
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "class":
                        if(!StringUtils.clean(afterEq).equals("no_class")) {
                            try {
                                character.setPClass(character.sources().classes().find(afterEq));
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
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

            //Qualities
            curr = nextLine(lines);
            while(curr.contains("=")) {
                String[] split = curr.split(" ?= ?", 2);
                String afterEq = split[1];
                if (afterEq.length() == 0) afterEq = "";
                character.qualities().getProperty(split[0].trim()).set(afterEq);
                curr = nextLine(lines);
            }

            //Skill Increase Choices Load Strings
            List<String> skillIncreases = new ArrayList<>();
            while(true) {
                String s = nextLine(lines);
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                skillIncreases.add(s.substring(3));
            }

            //Decisions Load Strings
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

            // Start to Load Decisions
            boolean allDecisionsMade = makeDecisions(character, decisionStringMap);

            //Load Skill Increases and remaining Decisions
            for (String skillIncrease : skillIncreases) {

                String[] split = skillIncrease.split(" ?: ?");
                if(split.length < 2) continue;
                int level = Integer.parseInt(split[0]);
                for (String skill : split[1].split(" ?, ?")) {
                    int openBracket = skill.indexOf("(");
                    String attribute = skill;
                    String data;
                    if(openBracket == -1) data = null;
                    else {
                        attribute = attribute.substring(0, openBracket);
                        int closeBracket = skill.indexOf(")", openBracket);
                        data = attribute.substring(openBracket + 1, closeBracket);
                    }
                    if(!allDecisionsMade && character.attributes().getSkillIncreasesRemaining(level) == 0)
                        allDecisionsMade = makeDecisions(character, decisionStringMap);
                    character.attributes().advanceSkill(Attribute.valueOf(attribute), data);
                }
            }


            //Items
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
                    String cleanedName = StringUtils.clean(split[1]);
                    Item item = null;
                    try {
                        item = character.sources().equipment().find(cleanedName);
                    } catch (ObjectNotFoundException e) {
                        try {
                            item = character.sources().armor().find(cleanedName);
                        } catch (ObjectNotFoundException objectNotFoundException) {
                            try {
                                item = character.sources().weapons().find(cleanedName);
                            } catch (ObjectNotFoundException notFoundException) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(item != null)
                        character.inventory().buy(item, Integer.parseInt(split[0]));
                } else if (s.startsWith(" @ ")) {
                    String itemName = StringUtils.clean(s.substring(3).split(" ", 2)[1]);
                    Item item = null;
                    try {
                        item = character.sources().equipment().find(itemName);
                    } catch (ObjectNotFoundException e) {
                        try {
                            item = character.sources().armor().find(itemName);
                        } catch (ObjectNotFoundException objectNotFoundException) {
                            try {
                                item = character.sources().weapons().find(itemName);
                            } catch (ObjectNotFoundException notFoundException) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (item != null) {
                        ItemInstance instance = new ItemInstance(item);
                        character.inventory().buy(instance, 1);
                        modifyItem(character, instance, lines);
                    }
                } else {
                    lines.second--;
                    break;
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
                for (ItemCount value : character.inventory().getItems().values()) {
                    if(value.stats().getName().equals(split[1])) {
                        character.inventory().equip(value.stats(),Slot.valueOf(slotSplit[0]),  Integer.parseInt(split[0]));
                        break;
                    }
                }
            }

            //Formulas Bought
            lines.second++; //Skip Section Header
            while(true) {
                String s;
                try { s = nextLine(lines); }
                catch(RuntimeException e) { break; }
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                try {
                    Item item = character.sources().equipment().find(s.substring(3));
                    character.inventory().addFormula(new ItemFormula(item), true);
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //Formulas Granted
            lines.second++; //Skip Section Header
            while(true) {
                String s;
                try { s = nextLine(lines); }
                catch(RuntimeException e) { break; }
                if(!s.startsWith(" - ")) {
                    lines.second--;
                    break;
                }
                try {
                    Item item = character.sources().equipment().find(s.substring(3));
                    character.inventory().addFormula(new ItemFormula(item), false);
                } catch (ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
            character.inventory().setMode(BuySellMode.FullPrice);

            //Spells
            lines.second++; // Skip Section Header
            if(lines.second >= lines.first.size())
                return;
            String spellListName = nextLine(lines);
            while (spellListName.startsWith(" - ")) {
                boolean legacy = spellListName.contains("0");
                String spellString = (!legacy) ? "     - " : "   - ";
                spellListName = (!legacy) ? spellListName.substring(3) : character.getPClass().getName();
                SpellList spellList = character.spells().getSpellList(spellListName);
                if(spellList == null) continue;
                for(int i=0; i<=10; i++){
                    lines.second++;
                    while(true) {
                        String s;
                        try { s = nextLine(lines); }
                        catch(RuntimeException e) { break; }
                        if(!s.startsWith(spellString)) {
                            lines.second--;
                            break;
                        }
                        try {
                            Spell spell = character.sources().spells()
                                    .find(s.substring(spellString.length()));
                            if(spell.getLevelOrCantrip() < i)
                                spell = new HeightenedSpell(spell, i);
                            spellList.addSpell(spell);
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(lines.second >= lines.first.size())
                    break;
                spellListName = nextLine(lines);
            }
            character.mods().refreshAlways();
            System.out.println(System.currentTimeMillis()-start+" ms");
    }

    private static boolean makeDecisions(PC character, Map<String, String> decisionStringMap) {
        ObservableList<Choice<?>> decisions = character.decisions().getUnmadeDecisions();
        while(decisions.size() > 0){
            int successes = 0;
            int index = 0;
            while(index < decisions.size()) {
                Choice<?> decision = decisions.get(index);
                if(decisionStringMap.get(decision.toString()) == null) {
                    index++;
                    continue;
                }
                List<String> selections = Arrays.asList(
                        decisionStringMap.get(decision.toString()).split(" ?\\^ ?"));
                List<?> options;
                if(decision instanceof ChoiceList)
                    options = ((ChoiceList<?>) decision).getOptions();
                else if(decision instanceof FeatSlot)
                    options = character.abilities().getOptions((FeatSlot)decision);
                else options = Collections.emptyList();
                for (Object option : options) {
                    if(selections.contains(option.toString())) {
                        int oldSize = decision.getSelections().size();
                        if(!decision.tryAdd(option))
                            throw new ClassCastException("Could not cast "+option+" to "+decision.getOptionsClass());
                        if(decision.getSelections().size() > oldSize)
                            successes++;
                        if(decision.getSelections().size() == selections.size()) {
                            index--;
                            decisionStringMap.remove(decision.toString());
                            break;
                        }
                    }
                }
                index++;
            }
            if(successes == 0) break;
        }
        return decisions.isEmpty();
    }

    private static void modifyItem(PC character, ItemInstance instance, Pair<List<String>, Integer> lines) {
        while(true) {
            String s = nextLine(lines);
            if (s.startsWith("    ")) {
                if (s.equalsIgnoreCase("    Runes")) {
                    while (true) {
                        try {
                            s = nextLine(lines);
                        } catch (RuntimeException e) {
                            return;
                        }
                        if (!s.startsWith("     - ")) {
                            lines.second--;
                            break;
                        }
                        String itemName = s.substring("     - ".length());
                        try {
                            Item rune = character.sources().equipment()
                                    .find(itemName);
                            if (Rune.isRune(rune)) {
                                character.inventory().buy(rune, 1);
                                character.inventory().tryToAddRune(instance, Rune.getRune(rune));
                            }
                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    String choiceName = s.substring(4);
                    ItemInstanceChoices choices = instance.getExtension(ItemInstanceChoices.class);
                    Choice<?> choice = choices.getChoices().get(choiceName);
                    while (true) {
                        try {
                            s = nextLine(lines);
                        } catch (RuntimeException e) {
                            return;
                        }
                        if (!s.startsWith("     - ")) {
                            lines.second--;
                            break;
                        }
                        String decision = s.substring("     - ".length());
                        if(choice.getOptionsClass() == Spell.class) {
                            try {
                                choice.tryAdd(character.sources().spells().find(decision));
                            } catch (ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } else{
                lines.second--;
                return;
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
