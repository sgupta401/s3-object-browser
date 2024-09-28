package com.intuit.controller;

import com.intuit.entity.S3Metadata;
import com.intuit.service.RsaKeyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    private final HttpSession httpSession;
    public LogoutController(HttpSession httpSession, CacheManager cacheManager, RsaKeyService rsaKeyService) {
        this.httpSession = httpSession;

    }


}
