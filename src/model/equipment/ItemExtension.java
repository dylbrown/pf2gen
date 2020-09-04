package model.equipment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ItemExtension {
    private final Item baseItem;

    protected ItemExtension(Item baseItem) {
        this.baseItem = baseItem;
    }
    public Item getBaseItem() {
        return baseItem;
    }

    public static abstract class Builder {
        public abstract ItemExtension build(Item baseItem);
    }

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    public @interface ItemDecorator {

    }
}
