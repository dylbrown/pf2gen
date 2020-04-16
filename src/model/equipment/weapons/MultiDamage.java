package model.equipment.weapons;

import model.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiDamage extends Damage {
    private List<Damage> convertedDamage;
    public MultiDamage(Damage first, List<Damage> additional) {
        super();
        Map<Pair<DamageType, Boolean>, Damage.Builder> damages = new HashMap<>();
        Stream.concat(Stream.of(first), additional.stream()).forEach(dt ->
            damages.computeIfAbsent(new Pair<>(dt.getDamageType(), dt.isPersistent()), ndt->new Damage.Builder().setDamageType(ndt.first).setPersistent(ndt.second))
                    .addDice(dt.getDice())
                    .addAmount(dt.getAmount())
        );
        convertedDamage = damages.values().stream()
                .map(Builder::build)
                .collect(Collectors.toList());

        // Put first damage type first
        for (int i = 0; i < convertedDamage.size(); i++) {
            if(convertedDamage.get(i).getDamageType().equals(first.getDamageType())) {
                if(i == 0) break;
                Damage oldFirst = convertedDamage.get(0);
                Damage newFirst = convertedDamage.get(i);
                convertedDamage.set(0, newFirst);
                convertedDamage.set(i, oldFirst);
                break;
            }
        }

    }

    @Override
    public String toString() {
        return convertedDamage.stream()
                .map(Damage::toString)
                .collect(Collectors.joining(" + "));
    }
}
