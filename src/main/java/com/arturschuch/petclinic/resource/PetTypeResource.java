package com.arturschuch.petclinic.resource;

import java.util.List;

import com.arturschuch.petclinic.service.ClinicService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/pet-types")
@Produces(MediaType.APPLICATION_JSON)
public class PetTypeResource {

    @Inject
    ClinicService clinic;

    @GET
    public List<PetTypeResponse> listPetTypes() {
        return clinic.listPetTypes().stream()
                .map(petType -> new PetTypeResponse(petType.id, petType.name))
                .toList();
    }

    public record PetTypeResponse(Long id, String name) {
    }

}
