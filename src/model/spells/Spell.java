package model.spells;

import model.NamedObject;
import model.enums.Trait;
import model.spells.heightened.HeightenedData;

import java.util.List;

public interface Spell extends NamedObject {
	String getName();
	String getCastTime();
	String getRequirements();
	String getRange();
	String getArea();
	String getTargets();
	String getDuration();
	String getSave();
	MagicalSchool getSchool();
	HeightenedData getHeightenedData();
	int getLevel();
	List<Trait> getTraits();
	List<Tradition> getTraditions();
	List<SpellComponent> getComponents();
	int compareTo(Spell o);
	boolean isCantrip();
	int getLevelOrCantrip();
}
