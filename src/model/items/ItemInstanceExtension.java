package model.items;

public class ItemInstanceExtension<T extends ItemExtension> extends ItemExtension {
    protected ItemInstanceExtension(ItemInstance instance, T parentExtension) {
        super(instance);
    }
}
