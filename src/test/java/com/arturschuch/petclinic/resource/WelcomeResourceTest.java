package com.arturschuch.petclinic.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class WelcomeResourceTest {

    @Test
    void redirectsRootToSwaggerUi() {
        given()
                .redirects()
                .follow(false)
                .when()
                .get("/")
                .then()
                .statusCode(303)
                .header("Location", endsWith("/q/swagger-ui/"));
    }

    @Test
    void servesSwaggerUi() {
        given()
                .when()
                .get("/q/swagger-ui/")
                .then()
                .statusCode(200)
                .body(containsString("OpenAPI UI"))
                .body(containsString("swagger-ui"));
    }

    @Test
    void exposesOpenApiDocument() {
        given()
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("Quarkus PetClinic API"));
    }

    @Test
    void openApiDocumentsOwnerResources() {
        given()
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("/api/owners"))
                .body(containsString("/api/owners/{ownerId}"))
                .body(containsString("/api/owners/{ownerId}/pets"));
    }

    @Test
    void openApiDocumentsPetTypeAndVisitResources() {
        given()
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(containsString("/api/pet-types"))
                .body(containsString("/api/owners/{ownerId}/pets/{petId}/visits"));
    }

    @Test
    void openApiDoesNotExposeSpringPackages() {
        given()
                .when()
                .get("/q/openapi")
                .then()
                .statusCode(200)
                .body(not(containsString("org.springframework")));
    }

}
