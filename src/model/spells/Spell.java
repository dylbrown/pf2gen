package model.spells;

import model.NamedObject;
import model.enums.Trait;
import model.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Spell extends NamedObject implements Comparable<Spell> {
	private final String castTime, requirements, range, area, targets, duration, save;
	private final int level;
	private final List<Trait> traits;
	private final List<Tradition> traditions;
	private final List<SpellComponent> components;
	private final boolean isCantrip;
	private final HeightenedData heightenedData;

	private Spell(Spell.Builder builder) {
		super(builder);
		this.level = builder.level;
		this.traits = builder.traits;
		this.traditions = builder.traditions;
		this.components = builder.cast;
		this.castTime = builder.castTime;
		this.requirements = builder.requirements;
		this.range = builder.range;
		this.area = builder.area;
		this.targets = builder.targets;
		this.duration = builder.duration;
		this.save = builder.save;
		this.isCantrip = builder.isCantrip;
		if(builder.heightenedEvery != null) {
			builder.heightenedEvery.setSpell(this);
			heightenedData = builder.heightenedEvery.build();
		}else if(builder.heightenedLevels != null) {
			builder.heightenedLevels.setSpell(this);
			heightenedData = builder.heightenedLevels.build();
		} else heightenedData = null;
	}

	public String getCastTime() {
		return castTime;
	}

	public String getRequirements() {
		return requirements;
	}

	public String getRange() {
		return range;
	}

	public String getArea() {
		return area;
	}

	public String getTargets() {
		return targets;
	}

	public String getDuration() {
		return duration;
	}

	public String getSave() {
		return save;
	}

	public HeightenedData getHeightenedData() {
		return heightenedData;
	}

	public int getLevel() {
		return level;
	}

	public List<Trait> getTraits() {
		return traits;
	}

	public List<Tradition> getTraditions() {
		return traditions;
	}

	public List<SpellComponent> getComponents() {
		return components;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Spell o) {
		return getName().compareTo(o.getName());
	}

	public boolean isCantrip() {
		return isCantrip;
	}

	public int getLevelOrCantrip() {
		return isCantrip ? 0 : getLevel();
	}

	public static class Builder extends NamedObject.Builder {
		private int level;
		private List<Trait> traits = Collections.emptyList();
		private List<Tradition> traditions = Collections.emptyList();
		private final List<SpellComponent> cast = new ArrayList<>();
		private boolean isCantrip = false;
		private String castTime = "";
		private String requirements = "";
		private String range = "";
		private String area = "";
		private String targets = "";
		private String duration = "";
		private String save = "";
		private HeightenedEvery.Builder heightenedEvery;
		private HeightenedLevels.Builder heightenedLevels;

		public void setLevel(String level) {
			this.level = Integer.parseInt(level);
		}

		public void addTrait(Trait trait) {
			if(traits.isEmpty())
				traits = new ArrayList<>();
			traits.add(trait);
		}

		public void setTraditions(String traditions) {
			if(this.traditions.size() == 0) this.traditions = new ArrayList<>();
			for (String tradition : traditions.split(", ?")) {
				try {
					this.traditions.add(Tradition.valueOf(StringUtils.camelCaseWord(tradition)));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void setCantrip(boolean cantrip) {
			isCantrip = cantrip;
		}

		public void setCast(String casts) {
			int index = casts.indexOf("(");
			if(index != -1) {
				int endIndex = casts.indexOf(")");
				setCastTime(casts.substring(0, index));
				casts = casts.substring(index + 1, endIndex);
			}
			for (String cast : casts.split(", ?")) {
				try {
					this.cast.add(SpellComponent.valueOf(StringUtils.camelCaseWord(cast).trim()));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void setCastTime(String castTime) {
			this.castTime = castTime.trim();
		}

		public void setRequirements(String requirements) {
			this.requirements = requirements;
		}

		public void setRange(String range) {
			this.range = range;
		}

		public void setArea(String area) {
			this.area = area;
		}

		public void setTargets(String targets) {
			this.targets = targets;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

		public void setSave(String save) {
			this.save = StringUtils.camelCase(save);
		}

		public void addHeightenedLevel(int level, String description) {
			if(heightenedLevels == null) heightenedLevels = new HeightenedLevels.Builder();
			heightenedLevels.add(level, description);
		}

		public void setHeightenedEvery(int every, String description) {
			heightenedEvery = new HeightenedEvery.Builder();
			heightenedEvery.setEvery(every);
			heightenedEvery.setDescription(description);
		}

		public Spell build() {
			if(level == 0) isCantrip = true;
			if(heightenedLevels != null && heightenedEvery != null)
				throw new RuntimeException("Cannot have both kinds of heightened.");
			return new Spell(this);
		}
	}
}
