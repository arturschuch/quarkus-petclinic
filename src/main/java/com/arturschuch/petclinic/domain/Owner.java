package com.arturschuch.petclinic.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "owners")
public class Owner extends Person {

    @Column(nullable = false)
    @NotBlank
    public String address;

    @Column(nullable = false)
    @NotBlank
    public String city;

    @Column(nullable = false)
    @NotBlank
    @Pattern(regexp = "\\d{10}")
    public String telephone;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "owner_id")
    @OrderBy("name")
    public List<Pet> pets = new ArrayList<>();

    public static List<Owner> findByLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            return list("order by lastName, firstName");
        }

        return find("lower(lastName) like ?1 order by lastName, firstName", lastName.toLowerCase() + "%").list();
    }

    public Optional<Pet> findPet(Long petId) {
        return pets.stream()
                .filter(pet -> pet.id != null && pet.id.equals(petId))
                .findFirst();
    }

    public Optional<Pet> findPetByName(String name, boolean ignoreNew) {
        return pets.stream()
                .filter(pet -> pet.name != null)
                .filter(pet -> pet.name.equalsIgnoreCase(name))
                .filter(pet -> !ignoreNew || !pet.isNew())
                .findFirst();
    }

    public void addPet(Pet pet) {
        pets.add(pet);
    }

}
