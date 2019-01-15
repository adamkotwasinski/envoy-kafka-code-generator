package envoy.types;

public class FieldSpec {

    public String name;
    public TypeSpecification type;

    public FieldSpec(final String name, final TypeSpecification type) {
        this.name = name;
        this.type = type;
    }

    public String getDeclaration() {
        return String.format("%s %s", this.type.getName(), this.name);
    }

    public String getName() {
        return this.name;
    }

    public TypeSpecification getType() {
        return this.type;
    }

    public String getCorrespondingDeserializerName() {
        return this.type.getCorrespondingDeserializerName();
    }

    /**
     * Whether given field should be printed in toString methods
     * Currently, arrays of arrays and (nullable)bytes are not printed
     */
    //XXX (adam.kotwasinski) should print lengths of them though
    public boolean printable() {

        if (this.type instanceof Array) {
            final Array array = (Array) this.type;
            if (array.getType() instanceof Array) {
                return false;
            }
            return true;
        }

        if (this.type instanceof Primitive) {
            final Primitive primitive = (Primitive) this.type;

            switch (primitive.getName()) {
            case "Bytes":
            case "NullableBytes":
                return false;
            default:
                return true;
            }
        }

        return true;
    }

}
