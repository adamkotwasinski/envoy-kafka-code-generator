package envoy.types;

import java.util.List;

/**
 * Represents Kafka nullable array
 */
public class Array
    implements TypeSpecification {

    private final TypeSpecification type;

    public Array(final TypeSpecification type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return String.format("NullableArray<%s>", this.type.getName());
    }

    @Override
    public String getCorrespondingDeserializerName() {
        return String.format("ArrayDeserializer<%s, %s>", this.type.getName(),
                this.type.getCorrespondingDeserializerName());
    }

    @Override
    public List<Structure> computeDeclarationChain() {
        return this.type.computeDeclarationChain();
    }

    @Override
    public String getExampleValue() {
        return String.format("{{ %s }}", this.type.getExampleValue());
    }

    public TypeSpecification getType() {
        return this.type;
    }

}
