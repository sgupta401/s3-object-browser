package com.intuit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

@Service

public class RsaKeyService {

    private static final Logger logger = LoggerFactory.getLogger(RsaKeyService.class);

    private final ObjectMapper objectMapper;
    private RSAKey publicKey;  // Store the generated PublicKey
    @Value("${oidc.authorization.server.host}")
    private String oidcServer;
    private final Environment environment;
    public RsaKeyService(ObjectMapper objectMapper,  Environment environment) {

        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    // This method will be called after the RsaKeyService bean is created, during startup
    @PostConstruct
    public void init() {
        // Skip initialization if in the test profile
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }
        try {
            // Generate the RSA public key from the JWK
            this.publicKey = getPublicKeyFromJwk();
            logger.info("RSA Public Key generated on startup: " + this.publicKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA public key", e);
        }
    }

    public RSAKey getPublicKey() {
        return this.publicKey;  // Provide access to the public key
    }

    // The same method as before to fetch the public key from the JWK endpoint
    public RSAKey getPublicKeyFromJwk() throws Exception {
        // Fetch the JWKS JSON from the remote endpoint

        logger.info("OIDC Server" + oidcServer);
        String jwksUrl = oidcServer + "/oauth2/jwks";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(jwksUrl, String.class);

        // Parse the JSON response
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode keysNode = jsonNode.get("keys");

        // Assuming there is at least one key
        JsonNode rsaKeyNode = keysNode.get(0);

        // Convert the JSON object to an RSAKey using Nimbus
        JWK jwk = JWK.parse(rsaKeyNode.toString());

        if (!(jwk instanceof RSAKey)) {
            throw new IllegalArgumentException("JWK is not of type RSA");
        }

        // Cast to RSAKey and return the public key
        RSAKey rsaKey = (RSAKey) jwk;
        return rsaKey;
    }
}
