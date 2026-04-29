package com.arturschuch.petclinic.resource;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import com.arturschuch.petclinic.domain.Owner;
import com.arturschuch.petclinic.domain.Pet;
import com.arturschuch.petclinic.domain.Visit;
import com.arturschuch.petclinic.service.ClinicService;
import com.arturschuch.petclinic.service.ClinicService.OwnerCommand;
import com.arturschuch.petclinic.service.ClinicService.PetCommand;
import com.arturschuch.petclinic.service.ClinicService.VisitCommand;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/owners")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OwnerResource {

    @Inject
    ClinicService clinic;

    @GET
    public List<OwnerResponse> findOwners(@QueryParam("lastName") String lastName) {
        return clinic.findOwners(lastName).stream()
                .map(OwnerResponse::from)
                .toList();
    }

    @POST
    public Response createOwner(@Valid OwnerRequest request) {
        Owner owner = clinic.createOwner(request.toCommand());
        return Response.created(URI.create("/api/owners/" + owner.id))
                .entity(OwnerResponse.from(owner))
                .build();
    }

    @GET
    @Path("/{ownerId}")
    public OwnerResponse getOwner(@PathParam("ownerId") Long ownerId) {
        return OwnerResponse.from(clinic.getOwner(ownerId));
    }

    @PUT
    @Path("/{ownerId}")
    public OwnerResponse updateOwner(@PathParam("ownerId") Long ownerId, @Valid OwnerRequest request) {
        return OwnerResponse.from(clinic.updateOwner(ownerId, request.toCommand()));
    }

    @DELETE
    @Path("/{ownerId}")
    public Response deleteOwner(@PathParam("ownerId") Long ownerId) {
        clinic.deleteOwner(ownerId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{ownerId}/pets")
    public Response addPet(@PathParam("ownerId") Long ownerId, @Valid PetRequest request) {
        Pet pet = clinic.addPet(ownerId, request.toCommand());
        return Response.created(URI.create("/api/owners/" + ownerId + "/pets/" + pet.id))
                .entity(PetResponse.from(pet))
                .build();
    }

    @POST
    @Path("/{ownerId}/pets/{petId}/visits")
    public Response addVisit(@PathParam("ownerId") Long ownerId, @PathParam("petId") Long petId,
            @Valid VisitRequest request) {
        Visit visit = clinic.addVisit(ownerId, petId, request.toCommand());
        return Response.created(URI.create("/api/owners/" + ownerId + "/pets/" + petId + "/visits/" + visit.id))
                .entity(VisitResponse.from(visit))
                .build();
    }

    public record OwnerRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String address,
            @NotBlank String city,
            @NotBlank @Pattern(regexp = "\\d{10}") String telephone) {

        OwnerCommand toCommand() {
            return new OwnerCommand(firstName, lastName, address, city, telephone);
        }

    }

    public record PetRequest(
            @NotBlank String name,
            @NotNull LocalDate birthDate,
            @NotNull Long typeId) {

        PetCommand toCommand() {
            return new PetCommand(name, birthDate, typeId);
        }

    }

    public record VisitRequest(
            LocalDate date,
            @NotBlank String description) {

        VisitCommand toCommand() {
            return new VisitCommand(date, description);
        }

    }

    public record OwnerResponse(
            Long id,
            String firstName,
            String lastName,
            String address,
            String city,
            String telephone,
            List<PetResponse> pets) {

        static OwnerResponse from(Owner owner) {
            return new OwnerResponse(owner.id, owner.firstName, owner.lastName, owner.address, owner.city,
                    owner.telephone, owner.pets.stream().map(PetResponse::from).toList());
        }

    }

    public record PetResponse(
            Long id,
            String name,
            LocalDate birthDate,
            PetTypeResponse type,
            List<VisitResponse> visits) {

        static PetResponse from(Pet pet) {
            return new PetResponse(pet.id, pet.name, pet.birthDate, PetTypeResponse.from(pet.type),
                    pet.visits.stream().map(VisitResponse::from).toList());
        }

    }

    public record PetTypeResponse(Long id, String name) {

        static PetTypeResponse from(com.arturschuch.petclinic.domain.PetType petType) {
            return new PetTypeResponse(petType.id, petType.name);
        }

    }

    public record VisitResponse(Long id, LocalDate date, String description) {

        static VisitResponse from(Visit visit) {
            return new VisitResponse(visit.id, visit.date, visit.description);
        }

    }

}
