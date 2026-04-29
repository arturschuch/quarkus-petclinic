package com.arturschuch.petclinic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.arturschuch.petclinic.domain.Owner;
import com.arturschuch.petclinic.domain.Pet;
import com.arturschuch.petclinic.domain.PetType;
import com.arturschuch.petclinic.domain.Visit;
import com.arturschuch.petclinic.service.ClinicService.OwnerCommand;
import com.arturschuch.petclinic.service.ClinicService.PetCommand;
import com.arturschuch.petclinic.service.ClinicService.VisitCommand;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@QuarkusTest
class ClinicServiceTest {

    @Inject
    ClinicService clinic;

    @ParameterizedTest
    @CsvSource({
            "1,George,Franklin,Leo",
            "2,Betty,Davis,Basil",
            "3,Eduardo,Rodriquez,Rosy",
            "4,Harold,Davis,Iggy",
            "5,Peter,McTavish,George",
            "6,Jean,Coleman,Samantha",
            "7,Jeff,Black,Lucky",
            "8,Maria,Escobito,Mulligan",
            "9,David,Schroeder,Freddy",
            "10,Carlos,Estaban,Lucky"
    })
    void loadsSeededOwnerAggregate(long ownerId, String firstName, String lastName, String petName) {
        Owner owner = clinic.getOwner(ownerId);

        assertEquals(firstName, owner.firstName);
        assertEquals(lastName, owner.lastName);
        assertTrue(owner.pets.stream().anyMatch(pet -> pet.name.equals(petName)));
    }

    @ParameterizedTest
    @CsvSource({
            "Davis,2",
            "dav,2",
            "Franklin,1",
            "Rod,1",
            "Schroeder,1",
            "Missing,0"
    })
    void searchesOwnersByCaseInsensitiveLastNamePrefix(String lastName, int expectedMatches) {
        assertEquals(expectedMatches, clinic.findOwners(lastName).size());
    }

    @Test
    void listsAllOwnersWhenSearchIsMissing() {
        List<Owner> owners = clinic.findOwners(null);

        assertTrue(owners.size() >= 10);
        assertTrue(owners.stream().anyMatch(owner -> owner.lastName.equals("Franklin")));
        assertTrue(owners.stream().anyMatch(owner -> owner.lastName.equals("Davis")));
    }

    @Test
    void listsAllOwnersWhenSearchIsBlank() {
        List<Owner> owners = clinic.findOwners(" ");

        assertTrue(owners.size() >= 10);
        assertTrue(owners.stream().anyMatch(owner -> owner.lastName.equals("Rodriquez")));
    }

    @Test
    void listsPetTypesAlphabetically() {
        assertEquals(List.of("bird", "cat", "dog", "hamster", "lizard", "snake"),
                clinic.listPetTypes().stream().map(type -> type.name).toList());
    }

    @ParameterizedTest
    @CsvSource({
            "5,bird",
            "1,cat",
            "2,dog",
            "6,hamster",
            "3,lizard",
            "4,snake"
    })
    void loadsSeededPetTypes(long typeId, String name) {
        PetType type = PetType.findById(typeId);

        assertNotNull(type);
        assertEquals(name, type.name);
    }

    @Test
    void createsOwner() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceCreate"));

        assertNotNull(owner.id);
        assertEquals("ServiceCreate", clinic.getOwner(owner.id).lastName);
    }

    @Test
    void createdOwnerStartsWithoutPets() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceNoPets"));

        assertTrue(clinic.getOwner(owner.id).pets.isEmpty());
    }

    @Test
    void updatesOwner() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceUpdateBefore"));

        Owner updated = clinic.updateOwner(owner.id,
                new OwnerCommand("Service", "ServiceUpdateAfter", "2 Updated St", "Monona", "6085553434"));

        assertEquals("ServiceUpdateAfter", updated.lastName);
        assertEquals("2 Updated St", clinic.getOwner(owner.id).address);
        assertEquals("6085553434", clinic.getOwner(owner.id).telephone);
    }

    @Test
    void deletesOwner() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceDelete"));

        clinic.deleteOwner(owner.id);

        assertThrows(NotFoundException.class, () -> clinic.getOwner(owner.id));
    }

    @Test
    void rejectsUnknownOwnerLookup() {
        assertThrows(NotFoundException.class, () -> clinic.getOwner(999999L));
    }

    @Test
    void rejectsUnknownOwnerDelete() {
        assertThrows(NotFoundException.class, () -> clinic.deleteOwner(999999L));
    }

    @Test
    void addsPetToOwner() {
        Owner owner = clinic.createOwner(ownerCommand("ServicePet"));

        Pet pet = clinic.addPet(owner.id, new PetCommand("Panache", LocalDate.of(2020, 1, 2), 2L));

        assertNotNull(pet.id);
        assertEquals("dog", pet.type.name);
        assertTrue(clinic.getOwner(owner.id).findPet(pet.id).isPresent());
    }

    @Test
    void rejectsDuplicatePetNameForOwner() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceDuplicatePet"));
        clinic.addPet(owner.id, new PetCommand("Arc", LocalDate.of(2020, 1, 2), 1L));

        assertThrows(BadRequestException.class,
                () -> clinic.addPet(owner.id, new PetCommand("arc", LocalDate.of(2021, 2, 3), 2L)));
    }

    @Test
    void rejectsUnknownPetType() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceUnknownPetType"));

        assertThrows(BadRequestException.class,
                () -> clinic.addPet(owner.id, new PetCommand("Unknown", LocalDate.of(2020, 1, 2), 999999L)));
    }

    @Test
    void addsVisitWithExplicitDate() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceVisit"));
        Pet pet = clinic.addPet(owner.id, new PetCommand("REST", LocalDate.of(2020, 1, 2), 1L));

        Visit visit = clinic.addVisit(owner.id, pet.id,
                new VisitCommand(LocalDate.of(2026, 4, 29), "explicit date"));

        assertNotNull(visit.id);
        assertEquals(LocalDate.of(2026, 4, 29), visit.date);
        assertEquals("explicit date", visit.description);
    }

    @Test
    void defaultsVisitDateWhenCommandDateIsMissing() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceDefaultVisitDate"));
        Pet pet = clinic.addPet(owner.id, new PetCommand("CDI", LocalDate.of(2020, 1, 2), 1L));

        Visit visit = clinic.addVisit(owner.id, pet.id, new VisitCommand(null, "default date"));

        assertEquals(LocalDate.now(), visit.date);
    }

    @Test
    void rejectsVisitForUnknownPet() {
        assertThrows(NotFoundException.class,
                () -> clinic.addVisit(1L, 999999L, new VisitCommand(LocalDate.of(2026, 4, 29), "missing pet")));
    }

    @Test
    void rejectsVisitForPetOwnedByDifferentOwner() {
        assertThrows(NotFoundException.class,
                () -> clinic.addVisit(1L, 2L, new VisitCommand(LocalDate.of(2026, 4, 29), "wrong owner")));
    }

    @Test
    void ownerDeletionRemovesOwnedPets() {
        Owner owner = clinic.createOwner(ownerCommand("ServiceDeletePets"));
        Pet pet = clinic.addPet(owner.id, new PetCommand("Cascade", LocalDate.of(2020, 1, 2), 1L));

        clinic.deleteOwner(owner.id);

        assertFalse(Pet.findByIdOptional(pet.id).isPresent());
    }

    private static OwnerCommand ownerCommand(String lastName) {
        return new OwnerCommand("Service", lastName, "1 Service St", "Madison", "6085552323");
    }

}
