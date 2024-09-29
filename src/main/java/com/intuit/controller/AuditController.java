package com.intuit.controller;

import com.intuit.dal.AuditRepository;
import com.intuit.entity.Audit;
import com.intuit.entity.AuditData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
public class AuditController {
    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private HttpSession httpSession;

    @GetMapping(value = "/audit_log", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditData getAudits() {
        AuditData auditData = new AuditData();
        List<Audit> audits = auditRepository.findAll();
        auditData.setAudits(audits);
        auditData.setLoggedInUser((String)httpSession.getAttribute("user"));
        return auditData;
    }
}
