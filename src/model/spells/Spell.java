package model.spells;

import model.enums.Trait;
import model.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Spell {
	private String name, castTime, requirements, range, area, targets, duration, save, description;
	private int level;
	private List<Trait> traits;
	private List<Tradition> traditions;
	private List<SpellComponent> cast;

	private Spell(String name, int level, List<Trait> traits,
	              List<Tradition> traditions, List<SpellComponent> cast,
	              String castTime, String requirements, String range,
	              String area, String targets, String duration,
	              String save, String description) {
		this.name = name;
		this.level = level;
		this.traits = traits;
		this.traditions = traditions;
		this.cast = cast;
		this.castTime = castTime;
		this.requirements = requirements;
		this.range = range;
		this.area = area;
		this.targets = targets;
		this.duration = duration;
		this.save = save;
		this.description = description;
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

	public static class Builder {
		private String name;
		private int level;
		private List<Trait> traits = new ArrayList<>();
		private List<Tradition> traditions = new ArrayList<>();
		private List<SpellComponent> cast = new ArrayList<>();
		private String castTime = "";
		private String requirements = "";
		private String range = "";
		private String area = "";
		private String targets = "";
		private String duration = "";
		private String save = "";
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

		public void setTraditions(String traditions) {
			for (String tradition : traditions.split(", ?")) {
				try {
					this.traditions.add(Tradition.valueOf(StringUtils.camelCaseWord(tradition)));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
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
					this.cast.add(SpellComponent.valueOf(StringUtils.camelCaseWord(cast)));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void setCastTime(String castTime) {
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
			return new Spell(name, level, traits,
					traditions, cast, castTime, requirements, range, area,
					targets, duration, save, description);
		}
	}
}
