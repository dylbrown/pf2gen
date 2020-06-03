package model.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.abc.Ancestry;
import model.enums.Language;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QualityManager {
    private final Map<String, StringProperty> qualities = new HashMap<>();
    private final Set<Language> languages = new TreeSet<>();
    private final Set<String> senses = new TreeSet<>();
    private final ObservableList<String> bonusLanguages = FXCollections.observableArrayList();
    private final ArbitraryChoice bonusLanguageChoice = new ArbitraryChoice("Bonus Languages",
            bonusLanguages, this::addBonusLanguage,  this::removeBonusLanguage, 0, false);

    QualityManager(Consumer<ArbitraryChoice> addDecision, Consumer<ArbitraryChoice> removeDecision) {
        bonusLanguageChoice.numSelectionsProperty().addListener((o, oldVal, newVal) -> {
            if(oldVal.intValue() > 0 && newVal.intValue() <= 0) removeDecision.accept(bonusLanguageChoice);
            if(oldVal.intValue() <= 0 && newVal.intValue() > 0) addDecision.accept(bonusLanguageChoice);
        });
    }

    public ArbitraryChoice getBonusLanguageChoice() {
        return bonusLanguageChoice;
    }

    private void addBonusLanguage(String language) {
        languages.add(Language.valueOf(language));
    }

    private void removeBonusLanguage(String language) {
        languages.remove(Language.valueOf(language));
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
        bonusLanguageChoice.getSelections()
                .removeIf(l->!ancestry.getBonusLanguages().contains(Language.valueOf(l)));
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
        set("senses", String.join(", ", senses));
    }

    private int previousInt = 0;
    public void updateInt(Integer mod) {
        bonusLanguageChoice.increaseChoices(mod - previousInt);
        previousInt = mod;
    }

    public Set<Language> getLanguages() {
        return Collections.unmodifiableSet(languages);
    }

    public Set<String> getSenses() {
        return Collections.unmodifiableSet(senses);
    }
}
