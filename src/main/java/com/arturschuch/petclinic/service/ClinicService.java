package com.arturschuch.petclinic.service;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

import java.time.LocalDate;
import java.util.List;

import com.arturschuch.petclinic.domain.Owner;
import com.arturschuch.petclinic.domain.Pet;
import com.arturschuch.petclinic.domain.PetType;
import com.arturschuch.petclinic.domain.Visit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
@Transactional(SUPPORTS)
public class ClinicService {

    public List<Owner> findOwners(String lastName) {
        return Owner.findByLastName(lastName);
    }

    public Owner getOwner(Long ownerId) {
        return Owner.<Owner>findByIdOptional(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner %d was not found".formatted(ownerId)));
    }

    public List<PetType> listPetTypes() {
        return PetType.listOrdered();
    }

    @Transactional
    public Owner createOwner(OwnerCommand command) {
        Owner owner = new Owner();
        apply(owner, command);
        owner.persist();
        return owner;
    }

    @Transactional
    public Owner updateOwner(Long ownerId, OwnerCommand command) {
        Owner owner = getOwner(ownerId);
        apply(owner, command);
        return owner;
    }

    @Transactional
    public void deleteOwner(Long ownerId) {
        getOwner(ownerId).delete();
    }

    @Transactional
    public Pet addPet(Long ownerId, PetCommand command) {
        Owner owner = getOwner(ownerId);

        owner.findPetByName(command.name(), true)
                .ifPresent(existing -> {
                    throw new BadRequestException("Owner already has a pet named %s".formatted(command.name()));
                });

        PetType type = PetType.<PetType>findByIdOptional(command.typeId())
                .orElseThrow(() -> new BadRequestException("Pet type %d was not found".formatted(command.typeId())));

        Pet pet = new Pet();
        pet.name = command.name();
        pet.birthDate = command.birthDate();
        pet.type = type;

        owner.addPet(pet);
        return pet;
    }

    @Transactional
    public Visit addVisit(Long ownerId, Long petId, VisitCommand command) {
        Owner owner = getOwner(ownerId);
        Pet pet = owner.findPet(petId)
                .orElseThrow(() -> new NotFoundException("Pet %d was not found for owner %d".formatted(petId, ownerId)));

        Visit visit = new Visit();
        visit.date = command.date() == null ? LocalDate.now() : command.date();
        visit.description = command.description();

        pet.addVisit(visit);
        return visit;
    }

    private static void apply(Owner owner, OwnerCommand command) {
        owner.firstName = command.firstName();
        owner.lastName = command.lastName();
        owner.address = command.address();
        owner.city = command.city();
        owner.telephone = command.telephone();
    }

    public record OwnerCommand(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank @Pattern(regexp = "\\d{10}") String telephone) {
    }

    public record PetCommand(
            @NotBlank String name,
            @NotNull LocalDate birthDate,
            @NotNull Long typeId) {
    }

    public record VisitCommand(
            LocalDate date,
            @NotBlank String description) {
    }

}
