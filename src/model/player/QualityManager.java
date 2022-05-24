package model.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import model.abc.Ancestry;
import model.abilities.Ability;
import model.abilities.AncestryExtension;
import model.abilities.GranterExtension;
import model.enums.Language;
import model.enums.Sense;
import model.enums.Trait;
import model.util.ObjectNotFoundException;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class QualityManager implements PlayerState {
    private final Map<String, StringProperty> qualities = new HashMap<>();
    private final ObservableSet<Trait> traits = FXCollections.observableSet(new TreeSet<>());
    private final Set<Language> languages = new TreeSet<>();
    private final Set<Sense> senses = new TreeSet<>();
    private final ObservableList<Language> bonusLanguages = FXCollections.observableArrayList();
    private final ArbitraryChoice<Language> bonusLanguageChoice;
    private Sense lowLightVision = null, darkvision = null;
    private boolean upgradedLowLight = false;
    private int senseUpgradeCount = 0;

    QualityManager(Consumer<ArbitraryChoice<Language>> addDecision,
                   Consumer<ArbitraryChoice<Language>> removeDecision,
                   Applier<Ability> applier, SourcesManager sources) {
        ArbitraryChoice.Builder<Language> builder = new ArbitraryChoice.Builder<>();
        builder.setName("Bonus Languages");
        builder.setChoices(bonusLanguages);
        builder.setFillFunction(this::addBonusLanguage);
        builder.setEmptyFunction(this::removeBonusLanguage);
        builder.setMaxSelections(0);
        builder.setOptionsClass(Language.class);
        bonusLanguageChoice = builder.build();
        bonusLanguageChoice.maxSelectionsProperty().addListener((o, oldVal, newVal) -> {
            if(oldVal.intValue() > 0 && newVal.intValue() <= 0) removeDecision.accept(bonusLanguageChoice);
            if(oldVal.intValue() <= 0 && newVal.intValue() > 0) addDecision.accept(bonusLanguageChoice);
        });

        try {
            lowLightVision = sources.senses().find("Low-Light Vision");
            darkvision = sources.senses().find("Darkvision");
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }

        applier.onPreApply(ability->{
            AncestryExtension ancestry = ability.getExtension(AncestryExtension.class);
            if(ancestry != null) {
                for (Trait trait : ancestry.getGrantsTraits()) {
                    addTrait(trait);
                }
            }
            GranterExtension granter = ability.getExtension(GranterExtension.class);
            if(granter != null) {
                for (Sense sense : granter.getSenses()) {
                    if(sense == Sense.UPGRADED_VISION) {
                        if(senses.contains(lowLightVision)) {
                            senses.add(darkvision);
                        } else {
                            senses.add(lowLightVision);
                            upgradedLowLight = true;
                        }
                        senseUpgradeCount++;
                    } else {
                        senses.add(sense);
                    }
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
            GranterExtension granter = ability.getExtension(GranterExtension.class);
            if(granter != null) {
                for (Sense sense : granter.getSenses()) {
                    if(sense == Sense.UPGRADED_VISION) {
                        if(senseUpgradeCount == 1 && upgradedLowLight) {
                            senses.remove(lowLightVision);
                            upgradedLowLight = false;
                        } else if((upgradedLowLight && senseUpgradeCount == 2) || senseUpgradeCount == 1) {
                            senses.remove(darkvision);
                        }
                        senseUpgradeCount--;
                    } else {
                        senses.remove(sense);
                    }
                }
            }
        });
    }

    private void addBonusLanguage(Language language) {
        languages.add(language);
    }

    private void removeBonusLanguage(Language language) {
        languages.remove(language);
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
        bonusLanguages.removeIf(language -> ancestry.getBonusLanguages().contains(language));
        bonusLanguages.removeIf(l->!ancestry.getBonusLanguages().contains(l));
        for (Language bonusLanguage : ancestry.getBonusLanguages()) {
            if(!bonusLanguages.contains(bonusLanguage))
                bonusLanguages.add(bonusLanguage);
        }
        int bonusLanguageIncrease = 0;
        for (Language language : oldAncestry.getLanguages()) {
            if(language.getName().equals("Free"))
                bonusLanguageIncrease -= 1;
            else
                languages.remove(language);
        }
        for (Language language : ancestry.getLanguages()) {
            if(language.getName().equals("Free"))
                bonusLanguageIncrease += 1;
            else
                languages.add(language);
        }
        bonusLanguageChoice.increaseChoices(bonusLanguageIncrease);

        oldAncestry.getSenses().forEach(senses::remove);
        senses.addAll(ancestry.getSenses());

        set("languages", languages.stream().map(Language::toString).collect(Collectors.joining(", ")));
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
