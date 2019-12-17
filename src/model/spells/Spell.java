package model.spells;

import model.enums.Trait;
import model.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Spell implements Comparable<Spell> {
	private final String name, castTime, requirements, range, area, targets, duration, save, description, page;
	private final int level;
	private final List<Trait> traits;
	private final List<Tradition> traditions;
	private final List<SpellComponent> cast;
	private final boolean isCantrip;

	private Spell(Spell.Builder builder) {
		this.name = builder.name;
		this.level = builder.level;
		this.traits = builder.traits;
		this.traditions = builder.traditions;
		this.cast = builder.cast;
		this.castTime = builder.castTime;
		this.requirements = builder.requirements;
		this.range = builder.range;
		this.area = builder.area;
		this.targets = builder.targets;
		this.duration = builder.duration;
		this.save = builder.save;
		this.description = builder.description;
		this.page = builder.page;
		this.isCantrip = builder.isCantrip;
	}

	public String getName() {
		return name;
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

	public String getDescription() {
		return description;
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

	public List<SpellComponent> getCast() {
		return cast;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Spell o) {
		return name.compareTo(o.name);
	}

	public boolean isCantrip() {
		return isCantrip;
	}

	public static class Builder {
		private String name;
		private int level;
		private List<Trait> traits = new ArrayList<>();
		private List<Tradition> traditions = Collections.emptyList();
		private List<SpellComponent> cast = new ArrayList<>();
		private boolean isCantrip = false;
		private String castTime = "";
		private String requirements = "";
		private String range = "";
		private String area = "";
		private String targets = "";
		private String duration = "";
		private String save = "";
		private String page = "";
		private String description;

		public void setName(String name) {
			this.name = name;
		}

		public void setLevel(String level) {
			this.level = Integer.parseInt(level);
		}

		public void setTraits(String traits) {
			for (String trait : traits.split(", ?")) {
				try {
					this.traits.add(Trait.valueOf(StringUtils.camelCaseWord(trait)));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void setPage(String page) {
			this.page = page;
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
			if(casts.contains("(")) {
				setCast(casts.replaceAll("[^(]*\\(", "")
							.replaceAll("\\).*", ""));
				setCastTime(casts.replaceAll("\\([^)]*\\)", ""));
				return;
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
			this.castTime = castTime;
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

		public void setDescription(String description) {
			this.description = description;
		}

		public void setHeightened(String heightened) {
			this.description += "\n\n" + heightened;
		}

		public Spell build() {
			if(level == 0) isCantrip = true;
			return new Spell(this);
		}
	}
}
