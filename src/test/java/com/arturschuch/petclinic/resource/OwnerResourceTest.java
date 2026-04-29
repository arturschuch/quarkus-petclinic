package com.arturschuch.petclinic.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;

@QuarkusTest
class OwnerResourceTest {

    @Test
    void findsOwnersByLastName() {
        given()
                .queryParam("lastName", "Davis")
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("lastName", hasItems("Davis", "Davis"));
    }

    @ParameterizedTest
    @CsvSource({
            "Davis,2",
            "Dav,2",
            "dav,2",
            "Franklin,1",
            "Rodriquez,1",
            "NoMatch,0"
    })
    void findsOwnersByLastNamePrefix(String lastName, int expectedMatches) {
        given()
                .queryParam("lastName", lastName)
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("", hasSize(expectedMatches));
    }

    @Test
    void findsAllOwnersWhenLastNameIsMissing() {
        given()
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(10))
                .body("lastName", hasItems("Franklin", "Davis", "Rodriquez"));
    }

    @Test
    void findsAllOwnersWhenLastNameIsBlank() {
        given()
                .queryParam("lastName", " ")
                .when()
                .get("/api/owners")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(10))
                .body("lastName", hasItems("Black", "Coleman", "Escobito"));
    }

    @ParameterizedTest
    @CsvSource({
            "1,George,Franklin,110 W. Liberty St.,Madison,6085551023,Leo",
            "2,Betty,Davis,638 Cardinal Ave.,Sun Prairie,6085551749,Basil",
            "3,Eduardo,Rodriquez,2693 Commerce St.,McFarland,6085558763,Rosy",
            "4,Harold,Davis,563 Friendly St.,Windsor,6085553198,Iggy",
            "5,Peter,McTavish,2387 S. Fair Way,Madison,6085552765,George",
            "6,Jean,Coleman,105 N. Lake St.,Monona,6085552654,Samantha",
            "7,Jeff,Black,1450 Oak Blvd.,Monona,6085555387,Lucky",
            "8,Maria,Escobito,345 Maple St.,Madison,6085557683,Mulligan",
            "9,David,Schroeder,2749 Blackhawk Trail,Madison,6085559435,Freddy",
            "10,Carlos,Estaban,2335 Independence La.,Waunakee,6085555487,Lucky"
    })
    void getsSeededOwnerById(int ownerId, String firstName, String lastName, String address, String city,
            String telephone, String petName) {
        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("id", equalTo(ownerId))
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName))
                .body("address", equalTo(address))
                .body("city", equalTo(city))
                .body("telephone", equalTo(telephone))
                .body("pets.name", hasItems(petName));
    }

    @Test
    void ownerCrudFlow() {
        String createdOwner = """
                {
                  "firstName": "Ada",
                  "lastName": "Quarkus",
                  "address": "1 Native Way",
                  "city": "Madison",
                  "telephone": "6085559999"
                }
                """;

        Long ownerId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdOwner)
                .when()
                .post("/api/owners")
                .then()
                .statusCode(201)
                .body("firstName", equalTo("Ada"))
                .body("id", notNullValue())
                .header("Location", startsWith("http://localhost"))
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .when()
                .get("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("telephone", equalTo("6085559999"));

        String updatedOwner = """
                {
                  "firstName": "Ada",
                  "lastName": "Quarkus",
                  "address": "2 Dev Services Ave",
                  "city": "Monona",
                  "telephone": "6085551111"
                }
                """;

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedOwner)
                .when()
                .put("/api/owners/{ownerId}", ownerId)
                .then()
                .statusCode(200)
                .body("address", equalTo("2 Dev Services Ave"))
                .body("telephone", equalTo("6085551111"));

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

}
