package setting;

import model.NamedObject;
import model.attributes.Attribute;
import model.enums.Alignment;
import model.equipment.weapons.Weapon;
import model.spells.Spell;

import java.util.*;

public class Deity extends NamedObject {
    public static final Deity NO_DEITY;

    static {
        Builder builder = new Builder();
        builder.setName("No Deity");
        builder.setTitle("No Title");
        builder.setDescription("You don't follow any deity in particular.");
        NO_DEITY = builder.build();
    }
    private final String title, edicts, anathema, areasOfConcern;
    private final Alignment deityAlignment;
    private final List<Alignment> followerAlignments;
    private final boolean harmFont, healFont;
    private final List<Attribute> divineSkillChoices;
    private final Weapon favoredWeapon;
    private final List<Domain> domains;
    private final Map<Integer, Spell> spells;

    private Deity(Builder builder) {
        super(builder);
        this.title = builder.title;
        this.edicts = builder.edicts;
        this.anathema = builder.anathema;
        this.areasOfConcern = builder.areasOfConcern;
        this.deityAlignment = builder.deityAlignment;
        this.followerAlignments = builder.followerAlignments;
        this.harmFont = builder.harmFont;
        this.healFont = builder.healFont;
        this.divineSkillChoices = builder.divineSkillChoices;
        this.favoredWeapon = builder.favoredWeapon;
        this.domains = builder.domains;
        this.spells = builder.spells;
    }

    @Override
    public String toString() { return getName(); }

    public String getTitle() {
        return title;
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

    public Weapon getFavoredWeapon() {
        return favoredWeapon;
    }

    public List<Domain> getDomains() {
        return Collections.unmodifiableList(domains);
    }

    public Map<Integer, Spell> getSpells() {
        return Collections.unmodifiableMap(spells);
    }

    public static class Builder extends NamedObject.Builder {
        private String title = "", edicts = "", anathema = "", areasOfConcern = "";
        private Alignment deityAlignment = Alignment.N;
        private List<Alignment> followerAlignments = Collections.emptyList();
        private boolean harmFont = false, healFont = false;
        private List<Attribute> divineSkillChoices = Collections.emptyList();
        private Weapon favoredWeapon = null;
        private List<Domain> domains = Collections.emptyList();
        private Map<Integer, Spell> spells = Collections.emptyMap();

        public void setTitle(String title) {
            this.title = title;
        }

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

        public void setFavoredWeapon(Weapon favoredWeapon) {
            this.favoredWeapon = favoredWeapon;
        }

        public void addDomains(Domain... domains) {
            if(this.domains.size() == 0) this.domains = new ArrayList<>();
            this.domains.addAll(Arrays.asList(domains));
        }

        public void addSpell(int level, Spell spell) {
            if(spells.size() == 0) spells = new TreeMap<>();
            spells.put(level, spell);
        }

        public Deity build() {
            return new Deity(this);
        }
    }
}
