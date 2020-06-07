package setting;

import model.attributes.Attribute;
import model.enums.Alignment;
import model.spells.Spell;

import java.util.*;

public class Deity {
    public static final Deity NO_DEITY;

    static {
        Builder builder = new Builder();
        builder.setName("No Deity");
        builder.setTitle("No Title");
        builder.setDescription("You don't follow any deity in particular.");
        NO_DEITY = builder.build();
    }
    private final String name, title, description, edicts, anathema, areasOfConcern;
    private final Alignment deityAlignment;
    private final List<Alignment> followerAlignments;
    private final boolean harmFont, healFont;
    private final List<Attribute> divineSkillChoices;
    // TODO: Favored Weapon
    private final List<Domain> domains;
    private final Map<Integer, Spell> spells;
    private final int page;

    private Deity(Builder builder) {
        this.name = builder.name;
        this.title = builder.title;
        this.description = builder.description;
        this.edicts = builder.edicts;
        this.anathema = builder.anathema;
        this.areasOfConcern = builder.areasOfConcern;
        this.deityAlignment = builder.deityAlignment;
        this.followerAlignments = builder.followerAlignments;
        this.harmFont = builder.harmFont;
        this.healFont = builder.healFont;
        this.divineSkillChoices = builder.divineSkillChoices;
        this.domains = builder.domains;
        this.spells = builder.spells;
        this.page = builder.page;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() { return getName(); }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEdicts() {
        return edicts;
    }

    public String getAnathema() {
        return anathema;
    }

    public String getAreasOfConcern() {
        return areasOfConcern;
    }

    public Alignment getDeityAlignment() {
        return deityAlignment;
    }

    public List<Alignment> getFollowerAlignments() {
        return Collections.unmodifiableList(followerAlignments);
    }

    public boolean isHarmFont() {
        return harmFont;
    }

    public boolean isHealFont() {
        return healFont;
    }

    public List<Attribute> getDivineSkillChoices() {
        return divineSkillChoices;
    }

    public List<Domain> getDomains() {
        return Collections.unmodifiableList(domains);
    }

    public Map<Integer, Spell> getSpells() {
        return Collections.unmodifiableMap(spells);
    }

    public int getPage() {
        return page;
    }

    public static class Builder {
        private String name = "", title = "", edicts = "", anathema = "", areasOfConcern = "", description = "";
        private Alignment deityAlignment = Alignment.N;
        private List<Alignment> followerAlignments = Collections.emptyList();
        private boolean harmFont = false, healFont = false;
        private List<Attribute> divineSkillChoices = Collections.emptyList();
        // TODO: Favored Weapon
        private List<Domain> domains = Collections.emptyList();
        private Map<Integer, Spell> spells = Collections.emptyMap();
        private int page = -1;

        public void setName(String name) {
            this.name = name;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String desc) {this.description = desc; }

        public void setEdicts(String edicts) {
            this.edicts = edicts;
        }

        public void setAnathema(String anathema) {
            this.anathema = anathema;
        }

        public void setAreasOfConcern(String areasOfConcern) {
            this.areasOfConcern = areasOfConcern;
        }

        public void setDeityAlignment(Alignment deityAlignment) {
            this.deityAlignment = deityAlignment;
        }

        public void addFollowerAlignments(Alignment... alignments) {
            if(followerAlignments.size() == 0) followerAlignments = new ArrayList<>();
            followerAlignments.addAll(Arrays.asList(alignments));
        }

        public void setHarmFont(boolean harmFont) {
            this.harmFont = harmFont;
        }

        public void setHealFont(boolean healFont) {
            this.healFont = healFont;
        }

        public void setDivineSkills(List<Attribute> divineSkills) {
            divineSkillChoices = divineSkills;
        }

        public void addDomains(Domain... domains) {
            if(this.domains.size() == 0) this.domains = new ArrayList<>();
            this.domains.addAll(Arrays.asList(domains));
        }

        public void addSpell(int level, Spell spell) {
            if(spells.size() == 0) spells = new TreeMap<>();
            spells.put(level, spell);
        }

        public void setPage(int page) {
            this.page = page;
        }

        public Deity build() {
            return new Deity(this);
        }
    }
}
