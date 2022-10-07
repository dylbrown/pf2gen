package model.abc;

import model.abilities.Ability;
import model.data_managers.sources.Source;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static model.player.PC.MAX_LEVEL;

public class ACTest {

    private Ancestry ancestry;
    private Ability ability;

    @BeforeEach
    void makeAncestry() {
        Ancestry.Builder aBuilder = new Ancestry.Builder(null);
        aBuilder.setName("Test");
        Ability.Builder abilityBuilder = new Ability.Builder((Source) null);
        abilityBuilder.setLevel(1);
        abilityBuilder.setName("Testy");
        ability = abilityBuilder.build();
        aBuilder.addFeat(ability);
        ancestry = aBuilder.build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 15, MAX_LEVEL})
    void getValidFeatLists(int level) {
        List<Ability> feats = ancestry.getFeats(level);
        Assertions.assertEquals(feats.size(), 1);
        Assertions.assertEquals(feats.get(0), ability);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 23})
    void getInvalidFeatList(int level) {
        Assertions.assertThrows(RuntimeException.class,
                ()-> ancestry.getFeats(level));
    }
}
