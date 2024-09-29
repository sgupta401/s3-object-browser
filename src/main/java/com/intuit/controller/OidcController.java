package com.intuit.controller;


import com.intuit.service.RsaKeyService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.text.ParseException;
import java.util.*;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class OidcController {

    private final HttpSession httpSession;
    private final CacheManager cacheManager;
    private final RsaKeyService rsaKeyService;

    @Value("${oidc.authorization.server.host}")
    private String oidcServer;

    @Value("${s3metadata.host}")
    private String s3Server;

    public OidcController(HttpSession httpSession, CacheManager cacheManager, RsaKeyService rsaKeyService) {
        this.httpSession = httpSession;
        this.cacheManager = cacheManager;
        this.rsaKeyService = rsaKeyService;
    }

    @GetMapping(value="/callback")
    public RedirectView handleCallback(@RequestParam String code, @RequestParam String state) {
        String tokenEndpoint = oidcServer + "/oauth2/token";
        String redirectUri =  "http://localhost:8080/callback";

        RestTemplate restTemplate = new RestTemplate();

        String credentials = "intuit-s3-object-browser:secret";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", redirectUri);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
        Map<String, Object> response = restTemplate.postForObject(tokenEndpoint, request, Map.class);

        // Extract access token and ID token
        String accessToken = (String) response.get("access_token");
        Cache cache  = cacheManager.getCache("usercache");
        Cache.ValueWrapper userState = cache.get(state);
        Map<String, String> stateMap = (Map)userState.get();
        String idToken = (String) response.get("id_token");

        try {
            // Validate the ID token
            if (!validateIdToken(idToken,stateMap.get("nonce"))) {
                throw new SecurityException("Invalid ID token");
            }


        } catch (Exception e) {
            return new RedirectView( "http://localhost:8080/error");
        }

        String sub = getSubjectFromIdToken(idToken);
        this.httpSession.setAttribute("user", sub);




        return new RedirectView(stateMap.get("ru"));
    }
    private String getUserInfo(String accessToken) {
        String userInfoEndpoint = oidcServer + "/oauth2/userinfo";
        
        RestTemplate restTemplate = new RestTemplate();
        String userInfoResponse = restTemplate.getForObject(userInfoEndpoint + "?access_token=" + accessToken, String.class);

        return userInfoResponse;
    }

    public static String getSubjectFromIdToken(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean validateIdToken(String idToken, String expectedNonce) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWSObject jwsObject = JWSObject.parse(idToken);


        RSAKey rsaKey = this.rsaKeyService.getPublicKey();
        RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);

        if (!jwsObject.verify(verifier)) {
            return false; // Signature verification failed
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // Verify issuer
        if (!oidcServer.equals(claims.getIssuer())) {
            return false;
        }

        // Verify audience
        if (!claims.getAudience().contains("intuit-s3-object-browser")) {
            return false;
        }

        // Verify nonce
        if (!expectedNonce.equals(claims.getStringClaim("nonce"))) {
            return false;
        }

        // Verify issuedAt (iat)
        Date issuedAt = claims.getIssueTime();
        if (issuedAt == null || issuedAt.after(new Date())) {
            return false;
        }

        // Verify expiration (exp)
        Date expiration = claims.getExpirationTime();
        if (expiration == null || expiration.before(new Date())) {
            return false;
        }

        return true;
    }
}