package model.items.runes.runedItems;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import model.items.Item;
import model.items.runes.Rune;
import model.util.Pair;
import model.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Runes<T extends Rune> {

    public static Runes<?> getRunes(Item item) {
        Runes<?> runes = null;
        RunedArmor runedArmor = item.getExtension(RunedArmor.class);
        if(runedArmor != null) runes = runedArmor.getRunes();
        RunedWeapon runedWeapon = item.getExtension(RunedWeapon.class);
        if(runedWeapon != null) runes = runedWeapon.getRunes();
        return runes;
    }

    public static boolean isRuned(Item item) {
        RunedArmor runedArmor = item.getExtension(RunedArmor.class);
        if(runedArmor != null) return true;
        RunedWeapon runedWeapon = item.getExtension(RunedWeapon.class);
        return runedWeapon != null;
    }

    private final String itemName;
    private final ReadOnlyObjectWrapper<String> fullName;
    private final Class<T> clazz;
    private final ObservableMap<String, T> runes = FXCollections.observableHashMap();
    private final ObservableList<Item> runesList = FXCollections.observableArrayList();
    private final ReadOnlyIntegerWrapper numProperties = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper maxProperties = new ReadOnlyIntegerWrapper(0);

    public Runes(String name, Class<T> clazz) {
        itemName = name;
        fullName = new ReadOnlyObjectWrapper<>(name);
        addListener(c->fullName.set(makeRuneName(itemName)));
        this.clazz = clazz;
    }

    public ObservableList<Item> list() {
        return FXCollections.unmodifiableObservableList(runesList);
    }

    public void addListener(MapChangeListener<String, T> listener) {
        runes.addListener(listener);
    }

    public void tryToAddRune(Item item) {
        if(item != null)
            tryToAddRune(item.getExtension(clazz));
    }

    public boolean tryToAddRune(Rune genericRune) {
        if(!canAddRune(genericRune)) return false;
        T rune = clazz.cast(genericRune);

        maxProperties.set(maxProperties.get() + rune.getGrantsProperty());
        numProperties.set(numProperties.get() + ((rune.isFundamental()) ? 0 : 1));

        runes.put(rune.getBaseRune(), rune);
        runesList.add(rune.getItem());
        return true;
    }

    public boolean tryToRemoveRune(Rune genericRune) {
        if(!canRemoveRune(genericRune)) return false;
        T rune = clazz.cast(genericRune);

        maxProperties.subtract(rune.getGrantsProperty());
        numProperties.set(numProperties.get() - ((rune.isFundamental()) ? 0 : 1));

        runesList.remove(rune.getItem());
        runes.remove(rune.getBaseRune());
        return true;
    }

    public boolean tryToUpgradeRune(Rune genericRune, Rune genericUpgradedRune) {
        if(!canUpgradeRune(genericRune, genericUpgradedRune)) return false;
        T rune = clazz.cast(genericRune);
        T upgradedRune = clazz.cast(genericUpgradedRune);

        maxProperties.set(maxProperties.get() - rune.getGrantsProperty() + upgradedRune.getGrantsProperty());

        runes.put(rune.getBaseRune(), upgradedRune);
        runesList.remove(rune.getItem());
        runesList.add(upgradedRune.getItem());
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
            String name = r.getItem().getName();
            if (name.contains("(")) {
                Pair<String, String> namePair = StringUtils.getInAndOutBrackets(name);
                if (namePair.first.matches(".*Potency.*")) return namePair.second;
                else return namePair.second + " " + namePair.first;
            }
            return name;
        });
        return Stream.concat(stringStream, Stream.of(weapon)).collect(Collectors.joining(" "));
    }

    public ReadOnlyObjectProperty<String> getFullName() {
        return fullName.getReadOnlyProperty();
    }

    public double getValue() {
        return runesList.stream().map(Item::getValue).reduce(0.0, Double::sum);
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