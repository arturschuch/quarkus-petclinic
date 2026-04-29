package com.arturschuch.petclinic.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DomainModelTest {

    @Test
    void newEntityHasNoIdentifier() {
        Owner owner = new Owner();

        assertTrue(owner.isNew());
    }

    @Test
    void persistedEntityHasIdentifier() {
        Owner owner = new Owner();
        owner.id = 42L;

        assertFalse(owner.isNew());
    }

    @Test
    void namedEntityStringUsesName() {
        PetType type = new PetType();
        type.name = "cat";

        assertEquals("cat", type.toString());
    }

    @Test
    void ownerStartsWithoutPets() {
        Owner owner = new Owner();

        assertTrue(owner.pets.isEmpty());
    }

    @Test
    void differentOwnersDoNotSharePetCollections() {
        Owner first = new Owner();
        Owner second = new Owner();

        assertNotSame(first.pets, second.pets);
    }

    @Test
    void ownerAddsPetToAggregate() {
        Owner owner = new Owner();
        Pet pet = pet(1L, "Leo");

        owner.addPet(pet);

        assertEquals(pet, owner.pets.get(0));
    }

    @Test
    void ownerFindsPetByIdentifier() {
        Owner owner = new Owner();
        Pet pet = pet(1L, "Leo");
        owner.addPet(pet);

        assertEquals(pet, owner.findPet(1L).orElseThrow());
    }

    @Test
    void ownerDoesNotFindPetWithMissingIdentifier() {
        Owner owner = new Owner();
        owner.addPet(pet(1L, "Leo"));

        assertTrue(owner.findPet(2L).isEmpty());
    }

    @Test
    void ownerFindsPetByNameIgnoringCase() {
        Owner owner = new Owner();
        Pet pet = pet(1L, "Leo");
        owner.addPet(pet);

        assertEquals(pet, owner.findPetByName("leo", true).orElseThrow());
    }

    @Test
    void ownerCanIgnoreNewPetWhenSearchingByName() {
        Owner owner = new Owner();
        Pet newPet = pet(null, "Leo");
        owner.addPet(newPet);

        assertTrue(owner.findPetByName("Leo", true).isEmpty());
        assertEquals(newPet, owner.findPetByName("Leo", false).orElseThrow());
    }

    @Test
    void ownerSkipsPetsWithoutNamesWhenSearchingByName() {
        Owner owner = new Owner();
        owner.addPet(pet(1L, null));

        assertTrue(owner.findPetByName("Leo", false).isEmpty());
    }

    @Test
    void petStartsWithoutVisits() {
        Pet pet = pet(1L, "Leo");

        assertTrue(pet.visits.isEmpty());
    }

    @Test
    void petAddsVisitToAggregate() {
        Pet pet = pet(1L, "Leo");
        Visit visit = new Visit();
        visit.description = "checkup";

        pet.addVisit(visit);

        assertTrue(pet.visits.contains(visit));
    }

    @Test
    void visitDefaultsToCurrentDate() {
        Visit visit = new Visit();

        assertEquals(LocalDate.now(), visit.date);
    }

    @Test
    void differentPetsDoNotShareVisitCollections() {
        Pet first = pet(1L, "Leo");
        Pet second = pet(2L, "Basil");

        assertNotSame(first.visits, second.visits);
    }

    private static Pet pet(Long id, String name) {
        Pet pet = new Pet();
        pet.id = id;
        pet.name = name;
        pet.birthDate = LocalDate.of(2020, 1, 1);
        return pet;
    }

}
