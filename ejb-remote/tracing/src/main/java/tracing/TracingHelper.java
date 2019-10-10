package tracing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.Configuration.SenderConfiguration;
import io.jaegertracing.internal.JaegerTracer.Builder;
import io.opentracing.Tracer;

public class TracingHelper {

    private static final String TRACER_CONFIG_LOCATION = "tracer_config.properties";

    static Tracer getJaegerTracer(Properties config) {
        SamplerConfiguration samplerConfig = new SamplerConfiguration().withType("const").withParam(1);
        SenderConfiguration senderConfig = new SenderConfiguration()
                .withAgentHost(config.getProperty("jaeger.reporter_host"))
                .withAgentPort(Integer.decode(config.getProperty("jaeger.reporter_port")));
        ReporterConfiguration reporterConfig = new ReporterConfiguration().withLogSpans(true).withFlushInterval(1000)
                .withMaxQueueSize(10000).withSender(senderConfig);
        Builder bldr = new Configuration("EJB-REMOTE-SERVER")
                .withSampler(samplerConfig)
                .withReporter(reporterConfig)
                .getTracerBuilder();
        return bldr.build();
    }

    static Properties loadConfig() {
        try (InputStream fs = TracingStartup.class.getClassLoader().getResourceAsStream(TRACER_CONFIG_LOCATION)) {
            Properties config = new Properties();
            config.load(fs);
            return config;
        } catch (IOException ex) {
            // unrecoverable exception
            throw new RuntimeException(ex);
        }
    }
}