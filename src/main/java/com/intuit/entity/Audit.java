package com.intuit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String fileName;
    private String accessBy;
    private String accessTime;

    public Audit() {

    }

    public Audit(String fileName, String accessBy, String accessTime) {
        this.fileName = fileName;
        this.accessBy = accessBy;
        this.accessTime = accessTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAccessBy() {
        return accessBy;
    }

    public void setAccessBy(String user) {
        this.accessBy = user;
    }

    public String getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(String accessTime) {
        this.accessTime = accessTime;
    }
}
