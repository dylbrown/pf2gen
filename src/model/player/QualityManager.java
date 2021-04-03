package model.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import model.abc.Ancestry;
import model.abilities.AncestryExtension;
import model.enums.Language;
import model.enums.Sense;
import model.enums.Trait;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QualityManager implements PlayerState {
    private final Map<String, StringProperty> qualities = new HashMap<>();
    private final ObservableSet<Trait> traits = FXCollections.observableSet(new TreeSet<>());
    private final Set<Language> languages = new TreeSet<>();
    private final Set<Sense> senses = new TreeSet<>();
    private final ObservableList<String> bonusLanguages = FXCollections.observableArrayList();
    private final ArbitraryChoice<String> bonusLanguageChoice;

    QualityManager(Consumer<ArbitraryChoice<String>> addDecision, Consumer<ArbitraryChoice<String>> removeDecision, Applier applier) {
        ArbitraryChoice.Builder<String> builder = new ArbitraryChoice.Builder<>();
        builder.setName("Bonus Languages");
        builder.setChoices(bonusLanguages);
        builder.setFillFunction(this::addBonusLanguage);
        builder.setEmptyFunction(this::removeBonusLanguage);
        builder.setMaxSelections(0);
        builder.setOptionsClass(String.class);
        bonusLanguageChoice = builder.build();
        bonusLanguageChoice.maxSelectionsProperty().addListener((o, oldVal, newVal) -> {
            if(oldVal.intValue() > 0 && newVal.intValue() <= 0) removeDecision.accept(bonusLanguageChoice);
            if(oldVal.intValue() <= 0 && newVal.intValue() > 0) addDecision.accept(bonusLanguageChoice);
        });
        applier.onPreApply(ability->{
            AncestryExtension ancestry = ability.getExtension(AncestryExtension.class);
            if(ancestry != null) {
                for (Trait trait : ancestry.getGrantsTraits()) {
                    addTrait(trait);
                }
            }
        });
        applier.onPreRemove(ability->{
            AncestryExtension ancestry = ability.getExtension(AncestryExtension.class);
            if(ancestry != null) {
                for (Trait trait : ancestry.getGrantsTraits()) {
                    removeTrait(trait);
                }
            }
        });
    }

    public ArbitraryChoice<String> getBonusLanguageChoice() {
        return bonusLanguageChoice;
    }

    private void addBonusLanguage(String language) {
        languages.add(Language.valueOf(language));
    }

    private void removeBonusLanguage(String language) {
        languages.remove(Language.valueOf(language));
    }

    protected void addTrait(Trait trait) {
        traits.add(trait);
    }

    protected void removeTrait(Trait trait) {
        traits.remove(trait);
    }

    public String get(String quality) {
        StringProperty property = qualities.get(quality.toLowerCase());
        return (property != null) ? property.get() : "";
    }

    public void set(String quality, String value) {
        getProperty(quality).set(value);
    }

    public StringProperty getProperty(String quality) {
        return qualities.computeIfAbsent(quality.toLowerCase(), s->new SimpleStringProperty(""));
    }

    public Map<String, StringProperty> map() {
        return Collections.unmodifiableMap(qualities);
    }

    public void update(Ancestry ancestry, Ancestry oldAncestry) {
        bonusLanguages.removeIf(language -> ancestry.getBonusLanguages().contains(Language.valueOf(language)));
        bonusLanguages.removeIf(l->!ancestry.getBonusLanguages().contains(Language.valueOf(l)));
        for (Language bonusLanguage : ancestry.getBonusLanguages()) {
            if(!bonusLanguages.contains(bonusLanguage.toString()))
                bonusLanguages.add(bonusLanguage.toString());
        }
        int bonusLanguageIncrease = 0;
        for (Language language : oldAncestry.getLanguages()) {
            if(language.equals(Language.Free))
                bonusLanguageIncrease -= 1;
            else
                languages.remove(language);
        }
        for (Language language : ancestry.getLanguages()) {
            if(language.equals(Language.Free))
                bonusLanguageIncrease += 1;
            else
                languages.add(language);
        }
        bonusLanguageChoice.increaseChoices(bonusLanguageIncrease);

        senses.removeAll(oldAncestry.getSenses());
        senses.addAll(ancestry.getSenses());

        set("languages", languages.stream().map(Enum::toString).collect(Collectors.joining(", ")));
        set("senses", senses.stream().map(Sense::getName).collect(Collectors.joining(", ")));
    }

    private int previousInt = 0;
    public void updateInt(Integer mod) {
        bonusLanguageChoice.increaseChoices(mod - previousInt);
        previousInt = mod;
    }

    public Set<Language> getLanguages() {
        return Collections.unmodifiableSet(languages);
    }

    public Set<Sense> getSenses() {
        return Collections.unmodifiableSet(senses);
    }

    public ObservableSet<Trait> getTraits() {
        return FXCollections.unmodifiableObservableSet(traits);
    }

    @Override
    public void reset(PC.ResetEvent resetEvent) {
        if(!resetEvent.isActive()) return;
        for (StringProperty value : qualities.values()) {
            value.set("");
        }
    }
}
