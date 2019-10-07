package org.jboss.as.quickstarts.ejb.remote.tracing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.ejb.protocol.remote.tracing.SpanCodec;
import org.jboss.ejb.protocol.remote.tracing.SpanFormat;

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
        // We only need the extracting side since ejb server will not propagate any spans remotely, only
        // accept them
        bldr.registerExtractor(SpanFormat.EJB, new SpanCodec());
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