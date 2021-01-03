package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.spells.Spell;

public class SpellEntry extends ListEntry<Spell> {
    private final ReadOnlyStringProperty school;


    public SpellEntry(Spell spell) {
        super(spell, getName(spell));
        this.school = new ReadOnlyStringWrapper(spell.getSchool().name()).getReadOnlyProperty();
    }

    private static ReadOnlyStringProperty getName(Spell spell) {
        return new ReadOnlyStringWrapper(spell.getName()).getReadOnlyProperty();
    }

    public SpellEntry(String label) {
        super(label);
        this.school = new ReadOnlyStringWrapper("").getReadOnlyProperty();
    }

    public ObservableValue<String> get(String propertyName) {
        switch (propertyName){
            case "name": return nameProperty();
            case "school": return schoolProperty();
        }
        return null;
    }

    private ReadOnlyStringProperty schoolProperty() {
        return school;
    }

    @Override
    public int compareTo(ListEntry<Spell> o) {
        if(this.getContents() == null && o.getContents() == null) {
            String s1 = this.toString();
            String s2 = o.toString();
            if(s1.matches("\\ALevel \\d{1,2}\\z") && s2.matches("\\ALevel \\d{1,2}\\z")) {
                int i1 = Integer.parseInt(s1.substring("Level ".length()));
                int i2 = Integer.parseInt(s2.substring("Level ".length()));
                return Integer.compare(i1, i2);
            }
        }
        return super.compareTo(o);
    }
}
