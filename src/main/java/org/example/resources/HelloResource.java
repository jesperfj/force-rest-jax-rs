package org.example.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.example.forceauth.ForcePrincipal;

import com.force.api.ApiConfig;
import com.force.api.DataApi;
import com.sun.jersey.spi.container.servlet.WebConfig;

@Path("/hello")
public class HelloResource {

	@Context
	WebConfig config;
	
    @GET
    @Produces("text/plain")
    public String handleGreeting(@Context SecurityContext ctx) {
    	System.out.println("API ENDPOINT IN SECURITY CONTEXT: "+((ForcePrincipal) ctx.getUserPrincipal()).getApiSession().getApiEndpoint());
    	return "Hello, World - "+((ForcePrincipal) ctx.getUserPrincipal()).getDataApi().getIdentity().getFirstName();

    }
    
}
