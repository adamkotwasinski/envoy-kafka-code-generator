package envoy.types;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Primitive
    implements TypeSpecification {

    /**
     * Translates Kafka type into C++/Envoy type
     */
    private static final Map<String, String> KAFKA_TYPE_TO_ENVOY_TYPE;
    static {
        final Map<String, String> result = new TreeMap<>();
        result.put("STRING", "std::string");
        result.put("NULLABLE_STRING", "NullableString");
        result.put("BOOLEAN", "bool");
        result.put("INT8", "int8_t");
        result.put("INT16", "int16_t");
        result.put("INT32", "int32_t");
        result.put("INT64", "int64_t");
        result.put("UINT32", "uint32_t");
        result.put("BYTES", "Bytes");
        result.put("RECORDS", "NullableBytes");
        KAFKA_TYPE_TO_ENVOY_TYPE = Collections.unmodifiableMap(result);
    }

    /**
     * Kafka type to Envoy deserializer
     */
    private static final Map<String, String> KAFKA_TYPE_TO_DESERIALIZER;
    static {
        final Map<String, String> result = new TreeMap<>();
        result.put("STRING", "StringDeserializer");
        result.put("NULLABLE_STRING", "NullableStringDeserializer");
        result.put("BOOLEAN", "BooleanDeserializer");
        result.put("INT8", "Int8Deserializer");
        result.put("INT16", "Int16Deserializer");
        result.put("INT32", "Int32Deserializer");
        result.put("INT64", "Int64Deserializer");
        result.put("UINT32", "Uint32Deserializer");
        result.put("BYTES", "BytesDeserializer");
        result.put("RECORDS", "NullableBytesDeserializer");
        KAFKA_TYPE_TO_DESERIALIZER = Collections.unmodifiableMap(result);
    }

    /**
     * Kafka type to example value that can be put into code
     */
    private static final Map<String, String> KAFKA_TYPE_TO_EXAMPLE_VALUE;
    static {
        final Map<String, String> result = new TreeMap<>();
        result.put("STRING", "\"string\"");
        result.put("NULLABLE_STRING", "{\"nullable\"}");
        result.put("BOOLEAN", "false");
        result.put("INT8", "8");
        result.put("INT16", "16");
        result.put("INT32", "32");
        result.put("INT64", "64");
        result.put("UINT32", "u32");
        result.put("BYTES", "{0, 1, 2, 3}");
        result.put("RECORDS", "{{10, 20, 30}}");
        KAFKA_TYPE_TO_EXAMPLE_VALUE = Collections.unmodifiableMap(result);
    }

    private final String original;
    private final String name;
    private final String deserializerName;

    public Primitive(final String name) {
        this.original = name;
        this.name = compute(name, KAFKA_TYPE_TO_ENVOY_TYPE);
        this.deserializerName = compute(name, KAFKA_TYPE_TO_DESERIALIZER);
    }

    private static String compute(final String arg, final Map<String, String> map) {
        final String result = map.get(arg);
        if (null == result) {
            throw new IllegalArgumentException(arg);
        }
        return result;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCorrespondingDeserializerName() {
        return this.deserializerName;
    }

    @Override
    public List<Structure> computeDeclarationChain() {
        return Collections.emptyList();
    }

    @Override
    public String getExampleValue() {
        return compute(this.original, KAFKA_TYPE_TO_EXAMPLE_VALUE);
    }

}
