package com.arturschuch.petclinic.domain;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase implements Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    public Long id;

    public boolean isNew() {
        return id == null;
    }

}
