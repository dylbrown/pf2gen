package model.data_managers;

import model.xml_parsers.DomainsLoader;
import setting.Domain;

import java.util.SortedMap;
import java.util.TreeMap;

public class AllDomains {
    private static final SortedMap<String, Domain> allDomainsTable = new TreeMap<>();
    private static final DomainsLoader domainsLoader = new DomainsLoader("data/setting/domains.pfdyl");
    private AllDomains(){}
    static{
        for (Domain domain : domainsLoader.parse()) {
            allDomainsTable.put(domain.getName().toLowerCase(), domain);
        }
    }

    public static Domain find(String name) {
        return allDomainsTable.get(name.toLowerCase().trim());
    }
}
