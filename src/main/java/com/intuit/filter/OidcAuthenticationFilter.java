package com.intuit.filter;

import jakarta.servlet.*;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class OidcAuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Autowired
    CacheManager cacheManager;

    @Value("${oidc.authorization.server.host}")
    private String oidcServer;

    @Value("${s3metadata.host}")
    private String s3Server;

    @Override
    public void doFilter(ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // If the request is for the OIDC callback endpoint, skip security checks
        String path = httpRequest.getRequestURI();
        if (path!=null && (path.equalsIgnoreCase("/callback") ||
                path.equalsIgnoreCase("/health"))) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false); // Do not create a session if it doesn't exist

        // Check if the user is authenticated
        if (session == null || session.getAttribute("user") == null) {
            // User is not authenticated, redirect to the OIDC authorization endpoint
            String authorizationUrl = buildAuthorizationUrl(httpRequest);
            httpResponse.sendRedirect(authorizationUrl);
            return;
        }

        // User is authenticated, continue to the next filter or resource
        chain.doFilter(request, response);
    }

    private String buildAuthorizationUrl(HttpServletRequest request) {
        String state = generateStateString();
        String nonce = generateStateString();
        Map<String, String> stateMap = new HashMap<>();
        stateMap.put("ru", request.getRequestURI());
        stateMap.put("nonce", nonce);
        Cache cache = cacheManager.getCache("usercache");
        cache.put(state, stateMap);
        String clientId = "intuit-s3-object-browser";
        String redirectUri = "http://localhost:8080" + "/callback";

        String scope = "openid profile";
        String authorizationEndpoint = "http://localhost:9000" + "/oauth2/authorize";

        // Construct the URL for redirecting to the authorization server
        return authorizationEndpoint + "?" +
                "response_type=code" + "&" +
                "client_id=" + clientId + "&" +
                "redirect_uri=" + redirectUri + "&" +
                "scope=" + scope + "&" +
                "state=" + state + "&" +
                "nonce=" + nonce;
    }


    protected String generateStateString() {
        String STATECHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * STATECHARS.length());
            salt.append(STATECHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    @Override
    public void destroy() {

    }
}