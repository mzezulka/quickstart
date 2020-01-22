/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.ejb.remote.stateless;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;

@Stateless
@Remote(RemoteCalculator.class)
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class CalculatorBean implements RemoteCalculator {

    @Override
    public int add(int a, int b) {
        Span s = GlobalTracer.get().buildSpan("CalculatorBean/add").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            return a + b;
        } finally {
            s.finish();
        }
    }

    @Override
    public int subtract(int a, int b) {
        Span s = GlobalTracer.get().buildSpan("CalculatorBean/substract").start();
        try(Scope sc = GlobalTracer.get().activateSpan(s)) {
            return a - b;
        } finally {
            s.finish();
        }
    }
}
