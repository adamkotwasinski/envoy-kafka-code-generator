package envoy;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class Templating {

    private static final Configuration CONFIGURATION = configuration();

    private static Configuration configuration() {
        final Configuration cf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        final TemplateLoader templateLoader = new ClassTemplateLoader(Templating.class, "/");
        cf.setTemplateLoader(templateLoader);
        return cf;
    }

    public static String render(final Object arg, final String templateName)
            throws Exception {

        final Template template = CONFIGURATION.getTemplate("templates/" + templateName);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(baos);
        template.process(arg, writer);

        return baos.toString();
    }

}
