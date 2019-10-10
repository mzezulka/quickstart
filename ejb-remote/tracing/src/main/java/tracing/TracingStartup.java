package tracing;

import java.util.Properties;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.annotation.PostConstruct;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@Singleton
@Startup
public class TracingStartup {
    @PostConstruct
    private void startTracing() {
        Properties config = TracingHelper.loadConfig();
        Tracer tracer = TracingHelper.getJaegerTracer(config);
        GlobalTracer.registerIfAbsent(tracer);
    }
}
