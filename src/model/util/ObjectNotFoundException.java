package model.util;

public class ObjectNotFoundException extends Exception {
    public ObjectNotFoundException(String type, String name) {
        super("Could not find a " + type + " named " + name);
    }

    public ObjectNotFoundException(String name, String category, String type) {
        super("Could not find a " + type + " named " + name + " in category " + category);
    }
}
