package model.equipment.weapons;

import java.util.*;

public class Damage {
    public static final Damage ZERO = new Damage.Builder().addDice(Dice.get(0,0)).build();
    private List<Dice> dice;
    private int amount;
    private DamageType damageType;
    private boolean isPersistent;

    public Damage(Builder builder) {
        this.dice = new ArrayList<>(builder.dice.values());
        this.amount = builder.amount;
        this.damageType = builder.damageType;
        this.isPersistent = builder.isPersistent;
    }

    Damage() {}

    public List<Dice> getDice() {
        return Collections.unmodifiableList(dice);
    }

    public int getAmount() {
        return amount;
    }

    DamageType getDamageType() {
        return damageType;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public List<Damage> asList() {
        return Collections.singletonList(this);
    }

    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        dice.stream().filter(d -> d.getCount() > 0 && d.getSize() > 0)
                    .forEach(d->parts.add(d.toString()));
        if(amount != 0) parts.add(String.valueOf(amount));
        if(damageType == null) return String.join(" + ", parts);
        return String.join(" + ", parts) + " " + ((isPersistent) ? "persistent " : "") + damageType.toString().toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Damage damage = (Damage) o;
        return amount == damage.amount &&
                isPersistent == damage.isPersistent &&
                Objects.equals(dice, damage.dice) &&
                damageType == damage.damageType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dice, amount, damageType, isPersistent);
    }

    public Damage increaseSize(Dice dice) {
        List<Dice> newDice = new ArrayList<>(this.dice);
        newDice.replaceAll(d->{
            if(d.getSize() == dice.getSize())
                return Dice.increase(d);
            return d;
        });
        return new Builder(this).setDice(newDice).build();
    }

    public Damage increaseSize(Dice dice, DamageType damageType) {
        return increaseSize(dice);
    }

    public Damage add(int damageMod) {
        return new Damage.Builder(this).addAmount(damageMod).build();
    }

    public Damage add(int damageMod, DamageType damageType) {
        if(damageType != this.damageType)
            return new MultiDamage(this, Collections.emptyList()).add(damageMod, damageType);
        return add(damageMod);
    }

    public static class Builder {
        private Map<Integer, Dice> dice = Collections.emptyMap();
        private int amount = 0;
        private DamageType damageType = null;
        private boolean isPersistent = false;

        public Builder() {}

        public Builder(Damage damage) {
            this.addDice(damage.getDice());
            this.damageType = damage.damageType;
            this.isPersistent = damage.isPersistent;
        }

        Damage.Builder addDice(List<Dice> newDice) {
            if(dice.size() == 0) dice = new HashMap<>();
            for (Dice die : newDice) {
                if(die == null) continue;
                int size = die.getSize();
                Dice oldDie = this.dice.get(size);
                this.dice.put(size,
                        Dice.get(die.getCount() +
                        ((oldDie != null) ? oldDie.getCount() : 0)
                        , size));
            }

            return this;
        }

        public Damage.Builder addDice(Dice... newDice) {
            addDice(Arrays.asList(newDice));
            return this;
        }

        public Damage.Builder addAmount(int amount) {
            this.amount += amount;
            return this;
        }

        public Damage.Builder setDamageType(DamageType damageType) {
            this.damageType = damageType;
            return this;
        }

        public Damage.Builder setPersistent(boolean persistent) {
            isPersistent = persistent;
            return this;
        }

        public Damage build() {
            return new Damage(this);
        }

        public Builder setDice(List<Dice> newDice) {
            dice.clear();
            return addDice(newDice);
        }
    }
}
