package model.spells;

public enum SpellType {
	Spell, Cantrip, Focus, FocusCantrip;

	@Override
	public String toString() {
		switch (this) {
			case Spell: return "Spell";
			case Cantrip: return "Cantrip";
			case Focus: return "Focus Spell";
			case FocusCantrip: return "Focus Cantrip";
		}
		return "";
	}
}
