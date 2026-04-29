package com.arturschuch.petclinic.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

    @Column(name = "visit_date", nullable = false)
    @NotNull
    public LocalDate date = LocalDate.now();

    @Column(nullable = false)
    @NotBlank
    public String description;

}
