package org.jboss.as.quickstarts.ejb.remote.stateless;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.XAConnectionFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.as.quickstarts.ejb.remote.stateful.DummyEntity;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jms2.TracingConnection;
import io.opentracing.contrib.jms2.TracingMessageProducer;
import io.opentracing.contrib.jms2.TracingSession;
import io.opentracing.util.GlobalTracer;

@Stateless
@Remote(RemoteCalculator.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class EnhancedCalculatorBean implements RemoteCalculator {

    @Resource(lookup = "java:/jboss/DummyXaConnectionFactory")
    private XAConnectionFactory factory;

    @Resource(lookup = "java:/jms/queue/DummyQueue")
    private Queue queue;


    @PersistenceContext
    private EntityManager em;

    @Override
    public int add(int a, int b) {
        Span s = GlobalTracer.get().buildSpan("EnhancedCalculatorBean/add").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            jmsSend("invoked add() with result " + (a + b));
            return a + b;
        } finally {
            s.finish();
        }
    }

    @Override
    public int subtract(int a, int b) {
        Span s = GlobalTracer.get().buildSpan("EnhancedCalculatorBean/substract").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            dbSave(new DummyEntity("operation result: " + (a - b)));
            return a - b;
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
