package model.player;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import model.attributes.AttributeMod;
import model.attributes.AttributeModSingleChoice;
import model.attributes.BaseAttribute;
import model.enums.Proficiency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeManagerTest {

	private AttributeManager attributes;

	@BeforeEach
	void setUp() {
		ReadOnlyObjectWrapper<Integer> level = new ReadOnlyObjectWrapper<>(7);
		attributes = new AttributeManager(null, level, new DecisionManager(),
				new Applier<>(), new Applier<>());
	}

	@Test
	void updateSkillCount() {
		attributes.updateSkillCount(7);
		assertEquals(7, attributes.getSkillIncreases().get(1));
	}

	@Test
	void applyThatIncreases() {
		ObservableValue<Proficiency> value = attributes.getProficiency(BaseAttribute.Athletics);
		assertEquals(Proficiency.Untrained, value.getValue());

		attributes.apply(new AttributeMod(BaseAttribute.Athletics, Proficiency.Trained));
		assertEquals(Proficiency.Trained, value.getValue());

		attributes.apply(new AttributeMod(BaseAttribute.Athletics, Proficiency.Expert));
		assertEquals(Proficiency.Expert, value.getValue());
	}

	@Test
	void applyThatDoesNotIncrease() {
		ObservableValue<Proficiency> value = attributes.getProficiency(BaseAttribute.Athletics);
		assertEquals(Proficiency.Untrained, value.getValue());

		attributes.apply(new AttributeMod(BaseAttribute.Athletics, Proficiency.Expert));
		assertEquals(Proficiency.Expert, value.getValue());

		attributes.apply(new AttributeMod(BaseAttribute.Athletics, Proficiency.Trained));
		assertEquals(Proficiency.Expert, value.getValue());
	}

	@Test
	void applyChoiceIncreaseLater() {
		ObservableValue<Proficiency> value = attributes.getProficiency(BaseAttribute.Athletics);
		assertEquals(Proficiency.Untrained, value.getValue());

		AttributeModSingleChoice choice = new AttributeModSingleChoice(
				Arrays.asList(BaseAttribute.Athletics, BaseAttribute.Acrobatics), Proficiency.Trained);
		attributes.apply(choice);
		assertEquals(Proficiency.Untrained, value.getValue());

		choice.fill(BaseAttribute.Athletics);
		assertEquals(Proficiency.Trained, value.getValue());
	}

	@Test
	void applyChoiceAutomatically() {
		ObservableValue<Proficiency> value = attributes.getProficiency(BaseAttribute.Athletics);
		assertEquals(Proficiency.Untrained, value.getValue());

		AttributeModSingleChoice choice = new AttributeModSingleChoice(
				Collections.singletonList(BaseAttribute.Athletics), Proficiency.Trained);
		attributes.apply(choice);
		assertEquals(Proficiency.Trained, value.getValue());
	}

	@Test
	void remove() {
	}

	@Test
	void advanceSkill() {
	}

	@Test
	void canAdvanceSkill() {
	}

	@Test
	void regressSkill() {
	}

	@Test
	void canRegressSkill() {
	}

	@Test
	void addSkillIncrease() {
		assertEquals(0, attributes.getSkillIncreases().getOrDefault(16, 0));
		attributes.addSkillIncreases(1, 16);
		assertEquals(1, attributes.getSkillIncreases().get(16));
		attributes.addSkillIncreases(2, 16);
		assertEquals(3, attributes.getSkillIncreases().get(16));
	}

	@Test
	void removeSkillIncrease() {
		attributes.addSkillIncreases(3, 16);
		assertEquals(3, attributes.getSkillIncreases().get(16));
		attributes.removeSkillIncreases(2, 16);
		assertEquals(1, attributes.getSkillIncreases().get(16));
		attributes.removeSkillIncreases(1, 16);
		assertEquals(0, attributes.getSkillIncreases().getOrDefault(16, 0));
	}

	@Test
	void applyWeaponGroup() {
	}

	@Test
	void removeWeaponGroup() {
	}

	@Test
	void resetSkills() {

	}

	@Test
	void getSkillIncreasesRemaining() {
	}
}