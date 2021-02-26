package ui.todo;

public abstract class AbstractTodoItem implements TodoItem  {
    private final String name;
    private final Priority priority;
    private final Runnable navigateTo;

    public AbstractTodoItem(String name, Priority priority, Runnable navigateTo) {
        this.name = name;
        this.priority = priority;
        this.navigateTo = navigateTo;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    public void navigateTo() {
        navigateTo.run();
    }

    @Override
    public String toString() {
        if(name.matches("[aeiouAEIOU].*"))
            return "Select an " + name;
        return "Select a " + name;
    }
}
