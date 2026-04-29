package com.arturschuch.petclinic.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class PetVisitFlowTest {

    @Test
    void addsPetAndVisitToOwner() {
        Long ownerId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "firstName": "Grace",
                          "lastName": "Hopper",
                          "address": "9 Compiler Ct",
                          "city": "Arlington",
                          "telephone": "7035551234"
                        }
                        """)
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        Long petId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "Cobol",
                          "birthDate": "2020-12-09",
                          "typeId": 2
                        }
                        """)
                .when()
                .post("/api/owners/{ownerId}/pets", ownerId)
                .then()
                .statusCode(201)
                .body("name", equalTo("Cobol"))
                .body("type.name", equalTo("dog"))
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "date": "2026-04-29",
                          "description": "annual checkup"
                        }
                        """)
                .when()
                .post("/api/owners/{ownerId}/pets/{petId}/visits", ownerId, petId)
                .then()
                .statusCode(201)
                .body("description", equalTo("annual checkup"));

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("pets.name", hasItem("Cobol"))
                .body("pets.find { it.name == 'Cobol' }.visits.description", hasItem("annual checkup"));
    }

}
