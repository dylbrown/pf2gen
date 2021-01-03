package ui.todo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Priority implements Comparable<Priority> {
    private final List<Integer> priority;

    /**
     * Create a priority
     * @param priority A string in the format "\\d+(\\.\\d)*" (e.g. 13.1.2)
     */
    public Priority(Integer... priority) {
        this.priority = new ArrayList<>(Arrays.asList(priority));
    }

    public void append(int subPriority) {
        priority.add(subPriority);
    }

    @Override
    public int compareTo(Priority o) {
        int maxSize = Math.max(priority.size(), o.priority.size());
        int result = 0;
        for(int i = 0; i < maxSize; i++) {
            result = Integer.compare(getOrDefault(priority, i), getOrDefault(o.priority, i));
            if(result != 0)
                return result;
        }
        return result;
    }

    private int getOrDefault(List<Integer> list, int index) {
        return (index < list.size()) ? list.get(index) : 0;
    }
}
