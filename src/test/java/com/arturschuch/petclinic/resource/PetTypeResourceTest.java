package com.arturschuch.petclinic.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class PetTypeResourceTest {

    @Test
    void listsPetTypesAsJson() {
        given()
                .when()
                .get("/api/pet-types")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void listsPetTypesAlphabetically() {
        given()
                .when()
                .get("/api/pet-types")
                .then()
                .statusCode(200)
                .body("name", contains("bird", "cat", "dog", "hamster", "lizard", "snake"));
    }

    @Test
    void petTypesHaveNames() {
        given()
                .when()
                .get("/api/pet-types")
                .then()
                .statusCode(200)
                .body("name", everyItem(notNullValue()));
    }

    @ParameterizedTest
    @CsvSource({
            "0,5,bird",
            "1,1,cat",
            "2,2,dog",
            "3,6,hamster",
            "4,3,lizard",
            "5,4,snake"
    })
    void exposesSeededPetType(int index, int id, String name) {
        given()
                .when()
                .get("/api/pet-types")
                .then()
                .statusCode(200)
                .body("[%d].id".formatted(index), equalTo(id))
                .body("[%d].name".formatted(index), equalTo(name));
    }

}
