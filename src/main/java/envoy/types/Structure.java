package envoy.types;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.text.WordUtils;

/**
 * Describes complex structures that are used inside requests/responses
 * e.g. TopicData in ProduceRequest
 */
public class Structure
    implements TypeSpecification {

    private final String originalName;
    private final String owner;
    private final String typeName;
    private final boolean renderToString;
    private final LinkedList<FieldSpec> fields;

    public Structure(final String owner, final String name, final boolean renderToString) {
        this.owner = owner;
        this.originalName = name;
        // changes to camel case & removes plural case e.g. 'topic_partitions' changes into 'TopicPartition'
        final String tmp = owner + WordUtils.capitalizeFully(name, '_').replaceAll("_", "");
        if (tmp.endsWith("s")) {
            this.typeName = tmp.substring(0, tmp.length() - 1);
        }
        else {
            this.typeName = tmp;
        }
        this.renderToString = renderToString;
        this.fields = new LinkedList<>();
    }

    public String getOriginalName() {
        return this.originalName;
    }

    public String getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.typeName;
    }

    public boolean getRenderToString() {
        return this.renderToString;
    }

    public LinkedList<FieldSpec> getFields() {
        return this.fields;
    }

    public void addField(final FieldSpec field) {
        this.fields.add(field);
    }

    @Override
    public List<Structure> computeDeclarationChain() {
        final LinkedList<Structure> result = new LinkedList<>();
        for (final FieldSpec field : this.fields) {
            final TypeSpecification type = field.getType();
            result.addAll(type.computeDeclarationChain());
        }
        result.add(this);
        return result;
    }

    @Override
    public String getCorrespondingDeserializerName() {
        return String.format("%sDeserializer", this.typeName);
    }

    @Override
    public String getExampleValue() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (final FieldSpec field : this.fields) {
            sb.append(field.getType().getExampleValue());
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

}
