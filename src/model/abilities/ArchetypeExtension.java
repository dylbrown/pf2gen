package model.abilities;

public class ArchetypeExtension extends AbilityExtension {
    private final String archetype;
    private final boolean isDedication;
    private ArchetypeExtension(Builder builder, Ability baseAbility) {
        super(baseAbility);
        this.archetype = builder.archetype;
        this.isDedication = builder.isDedication;
    }

    public String getArchetype() {
        return archetype;
    }

    public boolean isDedication() {
        return isDedication;
    }

    public static class Builder extends AbilityExtension.Builder {
        private boolean isDedication = false;
        private String archetype = "";

        public void setDedication(boolean dedication) {
            isDedication = dedication;
        }

        public void setArchetype(String archetype) {
            this.archetype = archetype;
        }

        @Override
        ArchetypeExtension build(Ability baseAbility) {
            return new ArchetypeExtension(this, baseAbility);
        }
    }
}
