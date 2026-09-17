package com.pranit.github.security.endpoints;

@FunctionalInterface
public interface PublicEndpointProvider {

    String[] publicEndpoints();
}
