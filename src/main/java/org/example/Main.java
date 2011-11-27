package org.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.example.forceauth.ForceAuthFilter;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

public class Main {
    public static void main(String[] args) throws IOException {
        
        final String baseUri = "http://localhost:"+(System.getenv("PORT")!=null?System.getenv("PORT"):"9998")+"/";
        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages","org.example.resources");
        initParams.put("com.sun.jersey.spi.container.ContainerRequestFilters", "org.example.forceauth.ForceAuthFilter");
        initParams.put(ForceAuthFilter.CLIENT_ID, System.getenv("CLIENT_ID"));
        initParams.put(ForceAuthFilter.CLIENT_SECRET, System.getenv("CLIENT_SECRET"));
        
        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        System.out.println(String.format("Jersey started with WADL available at %sapplication.wadl.",baseUri, baseUri));
    }
}
