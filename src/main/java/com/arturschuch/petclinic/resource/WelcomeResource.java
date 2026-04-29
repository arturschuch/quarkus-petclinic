package com.arturschuch.petclinic.resource;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class WelcomeResource {

    @GET
    public Response welcome() {
        return Response.seeOther(URI.create("/q/swagger-ui/")).build();
    }

}
