package org.jboss.as.quickstarts.ejb.remote.stateful;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class DummyEntity {
    @Id
    @GeneratedValue
    private Long id;

    private int cntr;

    public DummyEntity() {
    }

    public DummyEntity(int cntr) {
        this.cntr = cntr;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCntr() {
        return cntr;
    }

    public boolean isTransient() {
        return id == null;
    }
}
