package envoy;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.types.ArrayOf;
import org.apache.kafka.common.protocol.types.BoundField;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Schema;
import org.apache.kafka.common.protocol.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import envoy.types.Array;
import envoy.types.FieldSpec;
import envoy.types.Primitive;
import envoy.types.RequestClass;
import envoy.types.Structure;
import envoy.types.TypeSpecification;

public class Generator {

    private static final Logger LOG = LoggerFactory.getLogger(Generator.class);

    public static void main(final String[] args)
            throws Exception {

        if (args.length < 1) {
            throw new IllegalArgumentException("usage: gradle run ENVOY_DIRECTORY [RENDER_TO_STRING]");
        }

        final Path path = Paths.get(args[0]);
        final boolean renderToString = args.length >= 2 ? Boolean.parseBoolean(args[1]) : false;

        final File sourceDirectory = directory(path, "source/extensions/filters/network/kafka/generated");
        final File testDirectory = directory(path, "test/extensions/filters/network/kafka/generated");

        LOG.info("Will generate source files in [{}]", sourceDirectory);
        LOG.info("Will generate test files in [{}]", testDirectory);

        // serialization composites
        {
            LOG.info("Generating serialization composites");
            final List<SerializationComposite> composites = IntStream.rangeClosed(0, 9).mapToObj(
                    SerializationComposite::new).collect(Collectors.toList());

            generateSerializationCompositeHeader(composites, sourceDirectory);
            generateSerializationCompositeTest(composites, testDirectory);
        }

        // requests
        {
            LOG.info("Generating request classes");
            final List<RequestClass> requests = computeRequests(renderToString);

            generateRequestHeaders(requests, sourceDirectory);
            generateRequestHeadersTest(requests, testDirectory);

            generateRequestParserResolver(requests, sourceDirectory);
        }
    }

    private static File directory(final Path path, final String child) {
        final File directory = new File(path.toFile(), child);
        if (!directory.exists()) {
            final boolean created = directory.mkdirs();
            if (!created) {
                throw new IllegalArgumentException(String.format("Directory %s could not be created", directory));
            }
        }
        return directory;
    }

    private static void generateSerializationCompositeHeader(final List<SerializationComposite> composites,
                                                             final File directory)
            throws Exception {

        final TreeMap<String, Object> arg = new TreeMap<>();
        arg.put("composites", composites);
        final String rendered = Templating.render(arg, "serialization_composite_h.ftlh");
        writeToFile(rendered, directory, "serialization_composite.h");
    }

    private static void generateSerializationCompositeTest(final List<SerializationComposite> composites,
                                                           final File directory)
            throws Exception {

        final TreeMap<String, Object> arg = new TreeMap<>();
        arg.put("composites", composites);
        final String rendered = Templating.render(arg, "serialization_composite_test_cc.ftlh");
        writeToFile(rendered, directory, "serialization_composite_test.cc");
    }

    private static void generateRequestHeaders(final List<RequestClass> requests, final File directory)
            throws Exception {

        final StringBuilder inner = new StringBuilder();

        for (final RequestClass requestClass : requests) {

            final List<Structure> declarations = requestClass.computeDeclarationChain();

            // define structures used by request
            // the request class is at the end of chain, and will be defined separately
            for (final Structure dependency : declarations.subList(0, declarations.size() - 1)) {
                final String rendered = Templating.render(dependency, "requests_h_child_structure.ftlh");
                inner.append(rendered);
                inner.append("\n");
            }

            // define request itself
            final String rendered = Templating.render(requestClass, "requests_h_request_class.ftlh");
            inner.append(rendered);
            inner.append("\n");
        }

        final String rendered = inner.toString();

        final StringBuilder outer = new StringBuilder();

        // not very pretty
        outer.append("// DO NOT EDIT - THIS FILE WAS GENERATED\n");
        outer.append("// clang-format off\n");
        outer.append("#pragma once\n");
        outer.append("#include \"extensions/filters/network/kafka/kafka_request.h\"\n\n");
        outer.append("namespace Envoy {\n");
        outer.append("namespace Extensions {\n");
        outer.append("namespace NetworkFilters {\n");
        outer.append("namespace Kafka {\n\n");
        outer.append(rendered);
        outer.append("}}}}\n");
        outer.append("// clang-format on\n");

        writeToFile(outer.toString(), directory, "requests.h");
    }

    private static void generateRequestHeadersTest(final List<RequestClass> requests, final File directory)
            throws Exception {

        final TreeMap<String, Object> arg = new TreeMap<>();
        arg.put("requests", requests);
        final String rendered = Templating.render(arg, "requests_test_cc.ftlh");
        writeToFile(rendered, directory, "requests_test.cc");
    }

    public static List<RequestClass> computeRequests(final boolean renderToString)
            throws Exception {

        final List<RequestClass> result = new LinkedList<>();

        for (final ApiKeys key : getApiKeys()) {
            final Schema[] schemas = key.requestSchemas;
            for (int i = 0; i < schemas.length; i++) {
                final Schema schema = schemas[i];
                if (schema != null) {
                    final RequestClass requestClass = parseRequest(key, i, schemas[i], renderToString);
                    result.add(requestClass);
                }
            }
        }

        return result;
    }

    private static List<ApiKeys> getApiKeys() {
        final List<ApiKeys> result = new ArrayList<>();
        result.add(ApiKeys.OFFSET_COMMIT);
        return result;
    }

    private static RequestClass parseRequest(final ApiKeys key,
                                             final int apiVersion,
                                             final Schema schema,
                                             final boolean renderToString) {

        final String name = String.format("%sRequestV%d", key.name, apiVersion);
        final RequestClass result = new RequestClass(name, key.id, apiVersion, renderToString);

        for (final BoundField field : schema.fields()) {
            final Field fieldDef = field.def;
            final TypeSpecification type = parseType(name, fieldDef.name, fieldDef.type, renderToString);
            final FieldSpec parsedField = new FieldSpec(fieldDef.name, type);
            result.addField(parsedField);
        }
        return result;
    }

    private static TypeSpecification parseType(final String owner,
                                               final String name,
                                               final Type type,
                                               final boolean renderToString) {

        if (type instanceof ArrayOf) {
            final Type innerType = ((ArrayOf) type).type();
            final TypeSpecification parsed = parseType(owner, name, innerType, renderToString);
            return new Array(parsed);
        }
        else if (type instanceof Schema) {
            final Schema schema = (Schema) type;
            return parseSchema(owner, name, schema, renderToString);
        }
        else {
            return new Primitive(type.toString());
        }
    }

    private static Structure parseSchema(final String owner,
                                         final String name,
                                         final Schema schema,
                                         final boolean renderToString) {

        final Structure result = new Structure(owner, name, renderToString);

        for (final BoundField field : schema.fields()) {
            final Field fieldDef = field.def;
            final TypeSpecification type = parseType(owner, fieldDef.name, fieldDef.type, renderToString);
            final FieldSpec parsedField = new FieldSpec(fieldDef.name, type);
            result.addField(parsedField);
        }
        return result;
    }

    private static void generateRequestParserResolver(final List<RequestClass> requests, final File directory)
            throws Exception {

        final TreeMap<String, Object> arg = new TreeMap<>();
        arg.put("requests", requests);
        final String rendered = Templating.render(arg, "kafka_request_resolver_cc.ftlh");
        writeToFile(rendered, directory, "kafka_request_resolver.cc");
    }

    // === MISC ========================================================================================================

    private static void writeToFile(final String data, final File directory, final String name)
            throws Exception {

        final File file = new File(directory, name);
        LOG.info("Writing [{}]", file);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(data);
            writer.flush();
        }
    }

}
