package com.arturschuch.petclinic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

@MappedSuperclass
public abstract class Person extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    @NotBlank
    public String firstName;

    @Column(name = "last_name", nullable = false)
    @NotBlank
    public String lastName;

}
