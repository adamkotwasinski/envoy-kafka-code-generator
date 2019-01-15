package envoy.types;

/**
 * Contains extra Request-specific data
 */
public class RequestClass
    extends Structure {

    private final int apiKey;
    private final int apiVersion;

    public RequestClass(final String name, final int apiKey, final int apiVersion, final boolean renderToString) {
        super(name, "", renderToString);
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
    }

    public int getApiKey() {
        return this.apiKey;
    }

    public int getApiVersion() {
        return this.apiVersion;
    }

}
