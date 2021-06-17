package model.player;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import model.abilities.Ability;
import model.abilities.GranterExtension;
import model.ability_scores.AbilityMod;
import model.ability_scores.AbilityModChoice;
import model.ability_scores.AbilityScore;
import model.enums.Type;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static model.ability_scores.AbilityScore.*;

public class AbilityScoreManager implements PlayerState {
    private final Map<AbilityScore, ObservableList<AbilityMod>> abilityScores = new HashMap<>();
    private final Map<Type, List<AbilityMod>> abilityScoresByType = new HashMap<>();
    private final PropertyChangeSupport abilityScoreChange = new PropertyChangeSupport(this);
    private final ObservableList<AbilityModChoice> abilityScoreChoices = FXCollections.observableArrayList();
    private final Supplier<AbilityScore> keyAbility;
    private final Function<String, AbilityScore> castingAbility;

    AbilityScoreManager(Applier<Ability> applier,
                        Supplier<AbilityScore> keyAbility,
                        Function<String, AbilityScore> castingAbility) {
        this.keyAbility = keyAbility;
        this.castingAbility = castingAbility;
        List<AbilityModChoice> choices = Arrays.asList(
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial),
                new AbilityModChoice(Type.Initial)
        );
        abilityScoreChoices.addAll(choices);
        abilityScoresByType.put(Type.Initial, new ArrayList<>(choices));
        applier.onApply(ability -> {
            GranterExtension granter = ability.getExtension(GranterExtension.class);
            if(granter != null) {
                apply(granter.getAbilityMods());
            }
        });
        applier.onRemove(ability -> {
            GranterExtension granter = ability.getExtension(GranterExtension.class);
            if(granter != null) {
                remove(granter.getAbilityMods());
            }
        });
    }

    PropertyChangeSupport getScoreEyeball(@SuppressWarnings("SameParameterValue") AbilityScore score) {
        PropertyChangeSupport eyeball = new PropertyChangeSupport(this) {
        };
        abilityScores.computeIfAbsent(score, (key)->FXCollections.observableArrayList()).addListener((ListChangeListener<? super AbilityMod>) observable1 ->
                eyeball.firePropertyChange("scoreChange", null, null));
        return eyeball;
    }
    public Integer getMod(AbilityScore ability) {
        return getMod(ability, null);
    }


    public int getMod(AbilityScore ability, String data) {
        return getScore(ability, data) / 2  - 5;
    }

    public int getScore(AbilityScore ability) {
        return getScore(ability, null);
    }

    public int getScore(AbilityScore ability, String data) {
        if(ability == null) return getScore(None);
        if(ability.equals(KeyAbility))
            return getScore(keyAbility.get());
        if(ability.equals(CastingAbility))
            return getScore(castingAbility.apply(data));
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
        abilityScoreChange.firePropertyChange(new PropertyChangeEvent(this, "abilityMods", null, null));
    }

    void remove(List<AbilityMod> abilityMods) {
        for(AbilityMod mod: abilityMods) {
            List<AbilityMod> mods = abilityScores.get(mod.getTarget());
            mods.remove(mod);
            abilityScoresByType.get(mod.getType()).remove(mod);
            if(mod instanceof AbilityModChoice)
                abilityScoreChoices.remove(mod);
        }
        abilityScoreChange.firePropertyChange(
                new PropertyChangeEvent(this, "abilityMods", null, null));
    }
    public void addAbilityListener(PropertyChangeListener l) {
        abilityScoreChange.addPropertyChangeListener(l);
    }

    public List<AbilityModChoice> getFreeScores() {
        return Collections.unmodifiableList(abilityScoreChoices);
    }

    public void choose(AbilityModChoice choice, AbilityScore value) {
        AbilityScore old = choice.getTarget();
        if(old == value) return;
        if(choice.pick(value)) {
            abilityScores.computeIfAbsent(old, (key)->FXCollections.observableArrayList()).remove(choice);
            if(value != None && value != Free)
                abilityScores.computeIfAbsent(value, (key)->FXCollections.observableArrayList()).add(choice);
            abilityScoreChange.firePropertyChange(new PropertyChangeEvent(this, "abilityMods", null, null));
        }
    }

    public List<AbilityMod> getAbilityMods(Type type) {
        return Collections.unmodifiableList(abilityScoresByType.computeIfAbsent(type, (key)->new ArrayList<>()));
    }

    private final ObservableList<AbilityModChoice> unmodifiableScoreChoices =
            FXCollections.unmodifiableObservableList(abilityScoreChoices);
    public ObservableList<AbilityModChoice> getAbilityScoreChoices() {
        return unmodifiableScoreChoices;
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        if(!resetEvent.isActive()) return;
        for (AbilityModChoice choice : abilityScoreChoices) {
            AbilityScore old = choice.getTarget();
            choice.reset();
            abilityScores.computeIfAbsent(old, (key)->FXCollections.observableArrayList()).remove(choice);
        }
        abilityScoreChange.firePropertyChange(
                new PropertyChangeEvent(this, "abilityMods", null, null));
    }
}
