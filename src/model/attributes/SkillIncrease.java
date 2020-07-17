package model.attributes;

import model.enums.Proficiency;

import java.util.Objects;

public class SkillIncrease extends AttributeMod {
	private final int level;
	public SkillIncrease(Attribute attr, Proficiency mod, int level, String data) {
		super(attr, mod, (data != null && data.equals("")) ? null : data);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		SkillIncrease that = (SkillIncrease) o;
		return level == that.level;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), level);
	}
}
