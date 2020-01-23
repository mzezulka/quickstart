package org.jboss.as.quickstarts.ejb.remote.stateful;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.XAConnectionFactory;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jms2.TracingConnection;
import io.opentracing.contrib.jms2.TracingMessageProducer;
import io.opentracing.contrib.jms2.TracingSession;
import io.opentracing.util.GlobalTracer;

@Stateful
@Remote(RemoteCounter.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class EnhancedCounterBean implements RemoteCounter {

    private int count = 0;

    @Resource(lookup = "java:/jboss/DummyXaConnectionFactory")
    private XAConnectionFactory factory;

    @Resource(lookup = "java:/jms/queue/DummyQueue")
    private Queue queue;

    
    @PersistenceContext
    private EntityManager em;

    @Override
    public void increment() {
        Span s = GlobalTracer.get().buildSpan("EnhancedCounterBean/increment").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            dbSave(new DummyEntity(this.count++));
        } finally {
            s.finish();
        }
    }

    @Override
    public void decrement() {
        Span s = GlobalTracer.get().buildSpan("EnhancedCounterBean/decrement").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            dbSave(new DummyEntity(this.count--));
        } finally {
            s.finish();
        }
    }

    @Override
    public int getCount() {
        Span s = GlobalTracer.get().buildSpan("EnhancedCounterBean/getCount").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            jmsSend("getCount() invoked with the counter state: " + this.count);
            return this.count;
        } finally {
            s.finish();
        }
    }

    protected void jmsSend(final String message) {
        Tracer t = GlobalTracer.get();
        try (TracingConnection connection = new TracingConnection(factory.createXAConnection(), t);
                TracingSession session = new TracingSession(connection.createSession(), GlobalTracer.get());
                TracingMessageProducer producer = new TracingMessageProducer(session.createProducer(queue), t)) {
            connection.start();
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(message);
            producer.send(textMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void dbSave(DummyEntity quickstartEntity) {
        if (quickstartEntity.isTransient()) {
            em.persist(quickstartEntity);
        } else {
            em.merge(quickstartEntity);
        }
    }
}
