package model.abilities;

import model.enums.Trait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AncestryExtension extends AbilityExtension {
    private final List<Trait> requiredAncestries;
    private final List<Trait> grantsTraits;
    private final boolean firstLevelOnly;

    private AncestryExtension(AncestryExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        requiredAncestries = builder.requiredAncestries;
        grantsTraits = builder.grantedTraits;
        firstLevelOnly = builder.firstLevelOnly;
    }

    public List<Trait> getRequiredAncestries() {
        return requiredAncestries;
    }

    public List<Trait> getGrantsTraits() {
        return grantsTraits;
    }

    public boolean isFirstLevelOnly() {
        return firstLevelOnly;
    }

    public static class Builder extends AbilityExtension.Builder {
        private List<Trait> requiredAncestries = Collections.emptyList();
        private List<Trait> grantedTraits = Collections.emptyList();
        private boolean firstLevelOnly;
        public Builder() {}

        public Builder(AncestryExtension.Builder other) {
            if(other.requiredAncestries.size() > 0)
                requiredAncestries = new ArrayList<>(other.requiredAncestries);
            if(other.grantedTraits.size() > 0)
                grantedTraits = new ArrayList<>(other.grantedTraits);
            firstLevelOnly = other.firstLevelOnly;
        }

        public void addRequiredAncestry(Trait requiredAncestry) {
            if(requiredAncestries.size() == 0)
                requiredAncestries = new ArrayList<>();
            requiredAncestries.add(requiredAncestry);
        }

        public void addGrantedTrait(Trait ancestry) {
            if(grantedTraits.size() == 0)
                grantedTraits = new ArrayList<>();
            grantedTraits.add(ancestry);
        }

        public void setFirstLevelOnly(boolean firstLevelOnly) {
            this.firstLevelOnly = firstLevelOnly;
        }

        @Override
        AncestryExtension build(Ability baseAbility) {
            return new AncestryExtension(this, baseAbility);
        }
    }
}
