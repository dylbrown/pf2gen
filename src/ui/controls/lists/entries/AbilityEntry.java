package ui.controls.lists.entries;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import model.abilities.Ability;
import model.util.StringUtils;

public class AbilityEntry extends ListEntry<Ability> {
    private final ReadOnlyStringWrapper level;
    private final ReadOnlyStringWrapper type;

    public AbilityEntry(Ability ability) {
        super(ability, ability.getName());
        level = new ReadOnlyStringWrapper(Integer.toString(ability.getLevel()));
        type = new ReadOnlyStringWrapper(ability.getType().toString());
    }

    public AbilityEntry(String label) {
        super(label);
        this.level = new ReadOnlyStringWrapper("");
        this.type = new ReadOnlyStringWrapper("");
    }

    @Override
    public ObservableValue<String> get(String propertyName) {
        switch (propertyName){
            case "name": return nameProperty();
            case "level": return level.getReadOnlyProperty();
            case "type": return type.getReadOnlyProperty();
        }
        return null;
    }

    @Override
    public int compareTo(ListEntry<Ability> o) {
        if(this.getContents() == null && o.getContents() != null) return 1;
        if(this.getContents() != null && o.getContents() == null) return -1;
        if(this.getContents() == null && o.getContents() == null) {
            String s1 = this.toString();
            String s2 = o.toString();
            if(s1.startsWith("Level") && s2.startsWith("Level"))
                return Integer.compare(Integer.parseInt(s1.substring(6)), Integer.parseInt(s2.substring(6)));
            else return s1.compareTo(s2);
        }
        return StringUtils.clean(this.toString()).compareTo(StringUtils.clean(o.toString()));
    }
}
