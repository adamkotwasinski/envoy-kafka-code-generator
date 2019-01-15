package envoy;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Java representation of Envoy CompositeDeserializerWithNDelegates
 */
public class SerializationComposite {

    // how many delegates should be there
    private final int arity;

    public SerializationComposite(final int count) {
        this.arity = count;
    }

    public int getArity() {
        return this.arity;
    }

    // hack: list of [1...arity]
    public List<Integer> getElements() {
        if (this.arity >= 1) {
            return IntStream.rangeClosed(1, this.arity).boxed().collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }

}
