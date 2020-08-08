package setting;

import model.NamedObject;
import model.spells.Spell;

public class Domain extends NamedObject {
    private final Spell domainSpell, advancedDomainSpell;

    private Domain(Builder builder) {
        super(builder);
        this.domainSpell = builder.domainSpell;
        this.advancedDomainSpell = builder.advancedDomainSpell;
    }

    public Spell getDomainSpell() {
        return domainSpell;
    }

    public Spell getAdvancedDomainSpell() {
        return advancedDomainSpell;
    }

    public static class Builder extends NamedObject.Builder {
        private Spell domainSpell = null;
        private Spell advancedDomainSpell = null;

        public void setDomainSpell(Spell domainSpell) {
            this.domainSpell = domainSpell;
        }

        public void setAdvancedDomainSpell(Spell advancedDomainSpell) {
            this.advancedDomainSpell = advancedDomainSpell;
        }

        public Domain build() {
            return new Domain(this);
        }
    }
}
