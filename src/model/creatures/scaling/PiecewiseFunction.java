package model.creatures.scaling;

import java.util.Arrays;
import java.util.List;

public class PiecewiseFunction implements ZRMapping {
    private final List<ZRMapping> pieces;

    public PiecewiseFunction(ZRMapping... pieces) {
        this.pieces = Arrays.asList(pieces);
    }

    @Override
    public double map(int input) {
        for (ZRMapping piece : pieces) {
            if(piece.isInDomain(input))
                return piece.map(input);
        }
        throw new NotInDomainException();
    }

    @Override
    public boolean isInDomain(int input) {
        return pieces.stream().anyMatch(zrMapping -> zrMapping.isInDomain(input));
    }
}
