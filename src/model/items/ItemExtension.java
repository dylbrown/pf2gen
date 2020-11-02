package model.items;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ItemExtension {
    private final Item item;

    protected ItemExtension(Item item) {
        this.item = item;
    }
    public Item getItem() {
        return item;
    }

    public static abstract class Builder {
        public abstract ItemExtension build(Item baseItem);
    }

    public void applyToCreatedInstance(ItemInstance instance) {

    }

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    public @interface ItemDecorator {

    }

    /**
    * Denotes a decorator which should not run if this is an ItemInstance.
    * Only works for name and description
    * */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface BaseItemOnly {

    }
}
