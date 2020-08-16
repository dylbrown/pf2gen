package model.creatures;

import model.data_managers.sources.SourcesLoader;
import model.enums.Trait;

import java.util.ArrayList;
import java.util.List;

public class CustomCreature {
    public final List<Trait> traits = new ArrayList<>();
    private CreatureFamily family;
    public void set(CreatureFamily family) {
        this.family = family;
        Trait trait = SourcesLoader.instance().traits().find(family.getName());
        if(trait != null)
            traits.add(trait);
    }
    public void unset(CreatureFamily family) {
        this.family = null;
        Trait trait = SourcesLoader.instance().traits().find(family.getName());
        if(trait != null)
            traits.remove(trait);
    }
}
