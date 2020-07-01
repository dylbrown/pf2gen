package model.player;

import model.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomGetter {

    private final PC pc;

    public CustomGetter(PC pc) {
        this.pc = pc;
    }

    public Object get(String pathString) {
        String[] path = StringUtils.getInBrackets(pathString, '{', '}').split("\\.");
        Object curr = pc;
        for (String s : path) {
            for (Method method : curr.getClass().getMethods()) {
                if(method.getParameterCount() != 0 || !method.canAccess(curr)) continue;
                String name = method.getName().toLowerCase();
                if(name.toLowerCase().equals(s.toLowerCase()) || name.toLowerCase().equals("get" + s.toLowerCase())) {
                    try {
                        curr = method.invoke(curr);
                        break;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                }
            }
            if(curr == null) return null;
        }
        return curr;
    }
}
