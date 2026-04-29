package com.arturschuch.petclinic.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "types")
public class PetType extends NamedEntity {

    public static List<PetType> listOrdered() {
        return list("order by name");
    }

}
