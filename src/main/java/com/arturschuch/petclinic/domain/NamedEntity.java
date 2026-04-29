package com.arturschuch.petclinic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

@MappedSuperclass
public abstract class NamedEntity extends BaseEntity {

    @Column(nullable = false)
    @NotBlank
    public String name;

    @Override
    public String toString() {
        return name;
    }

}
