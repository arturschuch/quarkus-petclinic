package com.arturschuch.petclinic.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class OwnerValidationResourceTest {

    @ParameterizedTest
    @MethodSource("invalidOwnerRequests")
    void rejectsInvalidOwnerCreateRequests(String payload) {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .when()
                .post("/api/owners")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @MethodSource("invalidOwnerRequests")
    void rejectsInvalidOwnerUpdateRequests(String payload) {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .when()
                .put("/api/owners/1")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @MethodSource("invalidPetRequests")
    void rejectsInvalidPetRequests(String payload) {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @ParameterizedTest
    @MethodSource("invalidVisitRequests")
    void rejectsInvalidVisitRequests(String payload) {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .when()
                .post("/api/owners/6/pets/7/visits")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsUnknownOwnerLookup() {
        given()
                .when()
                .get("/api/owners/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsUnknownOwnerUpdate() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validOwner("Missing"))
                .when()
                .put("/api/owners/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsUnknownOwnerDelete() {
        given()
                .when()
                .delete("/api/owners/999999")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsPetForUnknownOwner() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validPet("Ghost"))
                .when()
                .post("/api/owners/999999/pets")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsPetWithUnknownType() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "UnknownType",
                          "birthDate": "2021-01-01",
                          "typeId": 999999
                        }
                        """)
                .when()
                .post("/api/owners/1/pets")
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsDuplicatePetNameForSameOwnerIgnoringCase() {
        Long ownerId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validOwner("DuplicatePet"))
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validPet("Byte"))
                .when()
                .post("/api/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(201);

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validPet("byte"))
                .when()
                .post("/api/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(400);
    }

    @Test
    void rejectsVisitForUnknownOwner() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validVisit("unknown owner"))
                .when()
                .post("/api/owners/999999/pets/1/visits")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsVisitForUnknownPet() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validVisit("unknown pet"))
                .when()
                .post("/api/owners/1/pets/999999/visits")
                .then()
                .statusCode(404);
    }

    @Test
    void rejectsVisitForPetOwnedByDifferentOwner() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validVisit("wrong owner"))
                .when()
                .post("/api/owners/1/pets/2/visits")
                .then()
                .statusCode(404);
    }

    @Test
    void createdOwnerValidationPathStillReturnsUsableOwner() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(validOwner("ValidationFlow"))
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("lastName", equalTo("ValidationFlow"));
    }

    private static Stream<Arguments> invalidOwnerRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "lastName": "Invalid",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "6085550001"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": " ",
                          "lastName": "Invalid",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "6085550002"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "6085550003"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": " ",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "6085550004"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "city": "Madison",
                          "telephone": "6085550005"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "address": " ",
                          "city": "Madison",
                          "telephone": "6085550006"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "address": "1 Test St",
                          "telephone": "6085550007"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "address": "1 Test St",
                          "city": " ",
                          "telephone": "6085550008"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "123"
                        }
                        """),
                Arguments.of("""
                        {
                          "firstName": "Invalid",
                          "lastName": "Owner",
                          "address": "1 Test St",
                          "city": "Madison",
                          "telephone": "608-555-0009"
                        }
                        """));
    }

    private static Stream<Arguments> invalidPetRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "birthDate": "2021-01-01",
                          "typeId": 1
                        }
                        """),
                Arguments.of("""
                        {
                          "name": " ",
                          "birthDate": "2021-01-01",
                          "typeId": 1
                        }
                        """),
                Arguments.of("""
                        {
                          "name": "NoBirthDate",
                          "typeId": 1
                        }
                        """),
                Arguments.of("""
                        {
                          "name": "NoType",
                          "birthDate": "2021-01-01"
                        }
                        """),
                Arguments.of("""
                        {
                          "name": "BadDate",
                          "birthDate": "not-a-date",
                          "typeId": 1
                        }
                        """));
    }

    private static Stream<Arguments> invalidVisitRequests() {
        return Stream.of(
                Arguments.of("""
                        {
                          "date": "2026-04-29"
                        }
                        """),
                Arguments.of("""
                        {
                          "date": "2026-04-29",
                          "description": " "
                        }
                        """),
                Arguments.of("""
                        {
                          "date": "not-a-date",
                          "description": "bad date"
                        }
                        """));
    }

    private static String validOwner(String lastName) {
        return """
                {
                  "firstName": "Test",
                  "lastName": "%s",
                  "address": "1 Test St",
                  "city": "Madison",
                  "telephone": "6085551212"
                }
                """.formatted(lastName);
    }

    private static String validPet(String name) {
        return """
                {
                  "name": "%s",
                  "birthDate": "2021-01-01",
                  "typeId": 1
                }
                """.formatted(name);
    }

    private static String validVisit(String description) {
        return """
                {
                  "date": "2026-04-29",
                  "description": "%s"
                }
                """.formatted(description);
    }

}
