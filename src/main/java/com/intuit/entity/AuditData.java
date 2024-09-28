package com.intuit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;


public class AuditData {
    private List<Audit> audits;

    public List<Audit> getAudits() {
        return audits;
    }

    public void setAudits(List<Audit> audits) {
        this.audits = audits;
    }
}
