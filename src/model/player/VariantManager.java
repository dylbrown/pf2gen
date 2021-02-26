package model.player;

import model.abc.PClass;
import model.ability_slots.AbilitySlot;
import model.ability_slots.FeatSlot;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariantManager implements PlayerState {
    private final Map<Variant, Boolean> variants = new HashMap<>();

    @Override
    public void reset(PC.ResetEvent resetEvent) {

    }

    public void setFreeArchetype(boolean newVal) {
        boolean oldVal = variants.getOrDefault(Variant.FreeArchetype, false);
        if(oldVal != newVal) {
            variants.put(Variant.FreeArchetype, newVal);
        }
    }

    public Map<Variant, Boolean> getMap() {
        return Collections.unmodifiableMap(variants);
    }

    public List<AbilitySlot> getLevel(PClass pClass, int level) {
        List<AbilitySlot> abilitySlots = pClass.getLevel(level);
        if(variants.getOrDefault(Variant.FreeArchetype, false)) {
            if(level > 0 && level % 2 == 0) {
                FeatSlot slot = new FeatSlot("Free Archetype Feat", level, Collections.singletonList("archetype"));
                abilitySlots = Stream.of(abilitySlots, Collections.singletonList(slot))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toUnmodifiableList());
            }
        }
        return abilitySlots;
    }

    public enum Variant {
        FreeArchetype
    }
}
