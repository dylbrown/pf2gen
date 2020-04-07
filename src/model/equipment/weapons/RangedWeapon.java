package model.equipment.weapons;

public class RangedWeapon extends Weapon {
    private final int range;
    private final int reload;

    private RangedWeapon(RangedWeapon.Builder builder) {
        super(builder);
        this.range = builder.range;
        this.reload = builder.reload;
    }

    @Override
    public boolean isRanged(){
        return true;
    }

    public int getRange() {
        return range;
    }

    public int getReload() {
        return reload;
    }

    public static class Builder extends Weapon.Builder {
        private int range = 0;
        private int reload = 0;

        public Builder(Weapon.Builder builder) {
            super(builder.build());
            this.setCategory("Ranged Weapon");
        }

        @Override
        public RangedWeapon build() {
            return new RangedWeapon(this);
        }

        public void setRange(int range) {
            this.range = range;
        }

        public void setReload(int reload) {
            this.reload = reload;
        }
    }
}
