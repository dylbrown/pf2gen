package model.attributes;

import java.util.Collections;
import java.util.List;

public class AttributeRequirementList extends AttributeRequirement {
    private final List<AttributeRequirement> requirements;

    public AttributeRequirementList(List<AttributeRequirement> requirements) {
        super();
        this.requirements = requirements;
    }

    @Override
    public List<AttributeRequirement> requirements() {
        return Collections.unmodifiableList(requirements);
    }
}
