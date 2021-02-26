package model.abilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FormulaExtension extends AbilityExtension {
    private final Map<Integer, Integer> formulasKnown;

    public FormulaExtension(FormulaExtension.Builder builder, Ability baseAbility) {
        super(baseAbility);
        formulasKnown = builder.formulasKnown;
    }

    public Map<Integer, Integer> getFormulasKnown() {
        return Collections.unmodifiableMap(formulasKnown);
    }

    public static class Builder extends AbilityExtension.Builder {
        private Map<Integer, Integer> formulasKnown = Collections.emptyMap();

        public void addFormulasKnown(int level, int count) {
            if(formulasKnown.isEmpty())
                formulasKnown = new HashMap<>();
            formulasKnown.merge(level, count, Integer::sum);
        }

        @Override
        public FormulaExtension build(Ability baseAbility) {
            return new FormulaExtension(this, baseAbility);
        }
    }
}
