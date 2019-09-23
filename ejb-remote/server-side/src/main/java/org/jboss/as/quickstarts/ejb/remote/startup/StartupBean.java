package org.jboss.as.quickstarts.ejb.remote.startup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.Configuration.SenderConfiguration;
import io.opentracing.util.GlobalTracer;

@Singleton
@Startup
public class StartupBean {

    private static final String TRACER_CONFIG_LOCATION = "tracer_config.properties";

    @PostConstruct
    private void startMeUp() {
        Properties config = loadConfig();
        SamplerConfiguration samplerConfig = new SamplerConfiguration().withType("const").withParam(1);
        SenderConfiguration senderConfig = new SenderConfiguration()
                .withAgentHost(config.getProperty("jaeger.reporter_host"))
                .withAgentPort(Integer.decode(config.getProperty("jaeger.reporter_port")));
        ReporterConfiguration reporterConfig = new ReporterConfiguration().withLogSpans(true).withFlushInterval(1000)
                .withMaxQueueSize(10000).withSender(senderConfig);
        GlobalTracer.registerIfAbsent(new Configuration("tx-demo-participant").withSampler(samplerConfig)
                .withReporter(reporterConfig).getTracer());
    }

    private Properties loadConfig() {
        try (InputStream fs = StartupBean.class.getClassLoader().getResourceAsStream(TRACER_CONFIG_LOCATION)) {
            Properties config = new Properties();
            config.load(fs);
            return config;
        } catch (IOException ex) {
            // unrecoverable exception
            throw new RuntimeException(ex);
        }
    }
}
