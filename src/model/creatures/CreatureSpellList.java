package model.creatures;

import model.spells.Spell;
import model.spells.SpellList;
import model.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class CreatureSpellList extends SpellList {
    private Integer dc = null;
    private Integer attack = null;
    private Integer heightenedLevel = null;
    private Map<Pair<Spell, Integer>, String> specialInfo = new HashMap<>();

    public CreatureSpellList(String index) {
        super(index);
    }

    public Integer getDC() {
        return dc;
    }

    public void setDC(int dc) {
        if(this.dc == null)
            this.dc = dc;
    }

    public Integer getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        if(this.attack == null)
            this.attack = attack;
    }

    public boolean addSpell(Spell spell, int level) {
        return addSpellInternal(spell, level, true);
    }

    public boolean addSpell(Spell spell, int level, String specialInfo) {
        if(addSpellInternal(spell, level, true)) {
            this.specialInfo.put(new Pair<>(spell, level), specialInfo);
            return true;
        }
        return false;
    }

    public String getSpecialInfo(Spell spell, int level) {
        return specialInfo.get(new Pair<>(spell, level));
    }

    public void setHeightenedLevel(int heightenedLevel) {
        this.heightenedLevel = heightenedLevel;
    }

    public int getHeightenedLevel() {
        return heightenedLevel;
    }
}
