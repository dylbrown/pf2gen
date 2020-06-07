package model.data_managers;

import model.xml_parsers.DeityLoader;
import setting.Deity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AllDeities {
    private static final SortedMap<String, Deity> allDeitiesTable = new TreeMap<>();
    private static final DeityLoader deitiesLoader = new DeityLoader("data/setting/deities.pfdyl");
    private AllDeities(){}
    static{
        for (Deity domain : deitiesLoader.parse()) {
            allDeitiesTable.put(domain.getName().toLowerCase(), domain);
        }
    }

    public static List<Deity> list() {
        return deitiesLoader.parse();
    }

    public List<Deity> getAllDeities() { return deitiesLoader.parse(); }

    public static Deity find(String name) {
        return allDeitiesTable.get(name.toLowerCase().trim());
    }
}
