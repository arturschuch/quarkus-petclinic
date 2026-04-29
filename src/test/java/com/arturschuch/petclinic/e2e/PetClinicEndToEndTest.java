package com.arturschuch.petclinic.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class PetClinicEndToEndTest {

    @Test
    void ownerRegistrationSearchUpdateAndDeleteFlow() {
        Long ownerId = createOwner("E2EOwnerLifecycle");

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("firstName", equalTo("E2E"))
                .body("lastName", equalTo("E2EOwnerLifecycle"));

        given()
                .queryParam("lastName", "E2EOwner")
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("lastName", hasItem("E2EOwnerLifecycle"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "firstName": "E2E",
                          "lastName": "E2EOwnerLifecycle",
                          "address": "2 Updated Test St",
                          "city": "Monona",
                          "telephone": "6085554545"
                        }
                        """)
                .when()
                .put("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("address", equalTo("2 Updated Test St"))
                .body("city", equalTo("Monona"))
                .body("telephone", equalTo("6085554545"));

        given()
                .when()
                .delete("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(204);

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(404);
    }

    @Test
    void petRegistrationFlowProtectsOwnerAggregate() {
        Long ownerId = createOwner("E2EPetRegistration");

        Long petId = addPet(ownerId, "Byte", 2L);

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("pets.id", hasItem(petId.intValue()))
                .body("pets.find { it.id == %d }.name".formatted(petId), equalTo("Byte"))
                .body("pets.find { it.id == %d }.type.name".formatted(petId), equalTo("dog"));

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(petPayload("byte", 1L))
                .when()
                .post("/api/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(400);
    }

    @Test
    void visitRegistrationFlowRecordsCareHistory() {
        Long ownerId = createOwner("E2EVisitRegistration");
        Long petId = addPet(ownerId, "Checkup", 1L);

        Long firstVisitId = addVisit(ownerId, petId, """
                {
                  "date": "2026-04-29",
                  "description": "annual checkup"
                }
                """);

        Long secondVisitId = addVisit(ownerId, petId, """
                {
                  "description": "follow-up call"
                }
                """);

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("pets.find { it.id == %d }.visits.id".formatted(petId),
                        hasItems(firstVisitId.intValue(), secondVisitId.intValue()))
                .body("pets.find { it.id == %d }.visits.description".formatted(petId),
                        hasItems("annual checkup", "follow-up call"))
                .body("pets.find { it.id == %d }.visits.find { it.id == %d }.date".formatted(petId, firstVisitId),
                        equalTo("2026-04-29"))
                .body("pets.find { it.id == %d }.visits.find { it.id == %d }.date".formatted(petId, secondVisitId),
                        equalTo(LocalDate.now().toString()));
    }

    @Test
    void visitRegistrationRejectsPetOwnedByAnotherOwner() {
        Long ownerId = createOwner("E2ECrossOwnerA");
        Long otherOwnerId = createOwner("E2ECrossOwnerB");
        Long otherPetId = addPet(otherOwnerId, "Boundary", 1L);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "date": "2026-04-29",
                          "description": "wrong owner"
                        }
                        """)
                .when()
                .post("/api/owners/{ownerId}/pets/{petId}/visits", ownerId, otherPetId)
                .then()
                .statusCode(404);
    }

    @Test
    void seededCatalogAndClinicDataAreAvailable() {
        given()
                .when()
                .get("/api/pet-types")
                .then()
                .statusCode(200)
                .body("name", contains("bird", "cat", "dog", "hamster", "lizard", "snake"));

        given()
                .queryParam("lastName", "Davis")
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("lastName", hasItems("Davis", "Davis"))
                .body("pets.flatten().name", hasItems("Basil", "Iggy"));
    }

    private static Long createOwner(String lastName) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "firstName": "E2E",
                          "lastName": "%s",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "6085551212"
                        }
                        """.formatted(lastName))
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private static Long addPet(Long ownerId, String name, Long typeId) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(petPayload(name, typeId))
                .when()
                .post("/api/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private static Long addVisit(Long ownerId, Long petId, String payload) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .when()
                .post("/api/owners/{ownerId}/pets/{petId}/visits", ownerId, petId)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private static String petPayload(String name, Long typeId) {
        return """
                {
                  "name": "%s",
                  "birthDate": "2020-01-02",
                  "typeId": %d
                }
                """.formatted(name, typeId);
    }

}
