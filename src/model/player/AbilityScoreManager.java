package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Type;

import java.util.*;

import static model.ability_scores.AbilityScore.Free;
import static model.ability_scores.AbilityScore.None;

public class AbilityScoreManager {
    private final Map<AbilityScore, ObservableList<AbilityMod>> abilityScores = new HashMap<>();
    private final Map<Type, List<AbilityMod>> abilityScoresByType = new HashMap<>();
    private final Eyeball abilityScoreChange = new Eyeball();
    private final List<AbilityModChoice> abilityScoreChoices = new ArrayList<>();

    AbilityScoreManager(Applier applier) {
        List<AbilityModChoice> choices = Arrays.asList(
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial)
        );
        abilityScoreChoices.addAll(choices);
        abilityScoresByType.put(Type.Initial, new ArrayList<>(choices));
        applier.onApply(ability -> apply(ability.getAbilityMods()));
        applier.onRemove(ability -> remove(ability.getAbilityMods()));
    }

    Eyeball getScoreEyeball(AbilityScore score) {
        Eyeball eyeball = new Eyeball() {
        };
        abilityScores.computeIfAbsent(score, (key)->FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) observable1 -> eyeball.wink());
        return eyeball;
    }
    public Integer getMod(AbilityScore ability) {
        return getScore(ability) / 2  - 5;
    }

    public int getScore(AbilityScore ability) {
        int score = 10;
        List<Type> stackingCheck = new ArrayList<>();
        for(AbilityMod mod: abilityScores.computeIfAbsent(ability, (key)-> FXCollections.observableArrayList())) {
            if(mod.isPositive() && !stackingCheck.contains(mod.getType())) {
                score += (score < 18) ? 2 : 1;
                stackingCheck.add(mod.getType());
            }else if(!mod.isPositive()){
                score -= 2;
            }
        }
        return score;
    }

    void apply(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            abilityScores.computeIfAbsent(mod.getTarget(), (key)-> FXCollections.observableArrayList()).add(mod);
            abilityScoresByType.computeIfAbsent(mod.getType(), (key)->new ArrayList<>()).add(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.add((AbilityModChoice) mod);
        }
        abilityScoreChange.wink();
    }

    void remove(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            List<AbilityMod> mods = abilityScores.get(mod.getTarget());
            mods.remove(mod);
            abilityScoresByType.get(mod.getType()).remove(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.remove(mod);
        }
        abilityScoreChange.wink();
    }
    public void addAbilityObserver(Observer o) {
        abilityScoreChange.addObserver(o);
    }

    public List<AbilityModChoice> getFreeScores() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }

    public void choose(AbilityModChoice choice, AbilityScore value) {
        AbilityScore old = choice.getTarget();
        if(choice.pick(value)) {
            abilityScores.computeIfAbsent(old, (key)->FXCollections.observableArrayList()).remove(choice);
            if(value != None && value != Free)
                abilityScores.computeIfAbsent(value, (key)->FXCollections.observableArrayList()).add(choice);
            abilityScoreChange.wink();
        }
    }

    public List<AbilityMod> getAbilityMods(Type type) {
        return Collections.unmodifiableList(abilityScoresByType.computeIfAbsent(type, (key)->new ArrayList<>()));
    }

    public List<AbilityModChoice> getAbilityScoreChoices() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }
}
