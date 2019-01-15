package envoy.types;

import java.util.List;

public interface TypeSpecification {

    /**
     * Name of given type
     */
    String getName();

    /**
     * Name of deserializer that is capable of constructing instances of this type
     */
    String getCorrespondingDeserializerName();

    /**
     * List of complex types used by this type (includes itself)
     */
    List<Structure> computeDeclarationChain();

    /**
     * Example value that can be put into code
     * <p>
     * The declarations ${name} var = ${example_value}; should be valid C++ code
     */
    String getExampleValue();

}
