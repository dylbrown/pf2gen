package setting;

import model.spells.Spell;

public class Domain {
    private final String name;
    private final Spell domainSpell, advancedDomainSpell;
    private final int page;

    private Domain(Builder builder) {
        this.name = builder.name;
        this.domainSpell = builder.domainSpell;
        this.advancedDomainSpell = builder.advancedDomainSpell;
        this.page = builder.page;
    }

    public String getName() {
        return name;
    }

    public Spell getDomainSpell() {
        return domainSpell;
    }

    public Spell getAdvancedDomainSpell() {
        return advancedDomainSpell;
    }

    public int getPage() {
        return page;
    }

    public static class Builder {
        private String name = "";
        private Spell domainSpell = null;
        private Spell advancedDomainSpell = null;
        private int page = -1;

        public void setName(String name) {
            this.name = name;
        }

        public void setDomainSpell(Spell domainSpell) {
            this.domainSpell = domainSpell;
        }

        public void setAdvancedDomainSpell(Spell advancedDomainSpell) {
            this.advancedDomainSpell = advancedDomainSpell;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public Domain build() {
            return new Domain(this);
        }
    }
}
