package model.equipment.runes.runedItems;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.equipment.Equipment;
import model.equipment.runes.Rune;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Runes<T extends Rune> {
    private final String itemName;
    private final ReadOnlyStringWrapper fullName;
    private final Class<T> clazz;
    private ObservableMap<String, T> runes = FXCollections.observableHashMap();
    private ObservableList<Equipment> runesList = FXCollections.observableArrayList();
    private ReadOnlyIntegerWrapper numProperties = new ReadOnlyIntegerWrapper(0);
    private ReadOnlyIntegerWrapper maxProperties = new ReadOnlyIntegerWrapper(0);

    public Runes(String name, Class<T> clazz) {
        itemName = name;
        fullName = new ReadOnlyStringWrapper(name);
        addListener(c->fullName.set(makeRuneName(itemName)));
        this.clazz = clazz;
    }

    public ObservableList<Equipment> list() {
        return FXCollections.unmodifiableObservableList(runesList);
    }

    public void addListener(MapChangeListener<String, T> listener) {
        runes.addListener(listener);
    }

    public boolean tryToAddRune(Rune genericRune) {
        if(!canAddRune(genericRune)) return false;
        T rune = clazz.cast(genericRune);

        maxProperties.set(maxProperties.get() + rune.getGrantsProperty());
        numProperties.set(numProperties.get() + ((rune.isFundamental()) ? 0 : 1));

        runes.put(rune.getBaseRune(), rune);
        runesList.add(rune);
        return true;
    }

    public boolean tryToRemoveRune(Rune genericRune) {
        if(!canRemoveRune(genericRune)) return false;
        T rune = clazz.cast(genericRune);

        maxProperties.subtract(rune.getGrantsProperty());
        numProperties.set(numProperties.get() - ((rune.isFundamental()) ? 0 : 1));

        runesList.remove(runes.get(rune.getBaseRune()));
        runes.remove(rune.getBaseRune());
        return true;
    }

    public boolean tryToUpgradeRune(Rune genericRune, Rune genericUpgradedRune) {
        if(!canUpgradeRune(genericRune, genericUpgradedRune)) return false;
        T rune = clazz.cast(genericRune);
        T upgradedRune = clazz.cast(genericUpgradedRune);

        maxProperties.set(maxProperties.get() - rune.getGrantsProperty() + upgradedRune.getGrantsProperty());

        runes.put(rune.getBaseRune(), upgradedRune);
        runesList.remove(rune);
        runesList.add(upgradedRune);
        return true;
    }

    private boolean canAddRune(Rune rune) {
        if(!clazz.isInstance(rune)) return false;
        if(hasRune(rune)) return false;
        if(rune.isFundamental()) return true;
        else return numProperties.get() < maxProperties.get();
    }

    private boolean canRemoveRune(Rune rune) {
        if(!clazz.isInstance(rune)) return false;
        if(!hasExactRune(rune)) return false;
        int numProperty = maxProperties.get() - numProperties.get() - rune.getGrantsProperty() + ((rune.isFundamental()) ? 0 : 1);
        return numProperty >= 0;
    }

    private boolean canUpgradeRune(Rune rune, Rune upgradedRune) {
        if(!clazz.isInstance(rune) || !clazz.isInstance(upgradedRune)) return false;
        if(!hasExactRune(rune)) return false;
        if(!hasRune(upgradedRune)) return false;
        else return maxProperties.get()
                - numProperties.get()
                - rune.getGrantsProperty()
                + upgradedRune.getGrantsProperty()
                >= 0;
    }

    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(runes.values());
    }

    String makeRuneName(String weapon) {
        Stream<String> stringStream = runes.values().stream()
                .sorted(Comparator.comparingInt(Rune::getGrantsProperty).reversed()
                        .thenComparing((o1, o2) -> Boolean.compare(o1.isFundamental(), o2.isFundamental())))
                .map(r -> {
            String name = r.getName();
            if (name.contains("(")) {
                String brackets = name.replaceAll("(.*\\(|\\).*)", "");
                String main = name.replaceAll(" *\\(.*", "");
                if (main.matches(".*Potency.*")) return brackets;
                else return brackets + " " + main;
            }
            return name;
        });
        return Stream.concat(stringStream, Stream.of(weapon)).collect(Collectors.joining(" "));
    }

    public ReadOnlyStringProperty getFullName() {
        return fullName.getReadOnlyProperty();
    }

    public double getValue() {
        return runes.values().stream().map(Equipment::getValue).reduce(0.0, Double::sum);
    }

    private boolean hasRune(Rune rune) {
        return runes.get(rune.getBaseRune()) != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasExactRune(Rune rune) {
        T t = runes.get(rune.getBaseRune());
        return t != null && t.equals(rune);
    }

    public int getNumProperties() {
        return numProperties.get();
    }

    public int getMaxProperties() {
        return maxProperties.get();
    }
}