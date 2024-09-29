package com.intuit.controller;

import com.intuit.dal.AuditRepository;
import com.intuit.entity.S3Metadata;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ObjectMetadataController.class)
class ObjectMetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetObjectMetadataSuccess() throws Exception {
        // Mocking S3Client response
        HeadObjectResponse mockResponse = HeadObjectResponse.builder()
                .lastModified(Instant.now())
                .partsCount(2)
                .storageClass("STANDARD")
                .serverSideEncryption("AES256")
                .versionId("v1")
                .build();

        // Mock the behavior of S3Client when headObject is called
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(mockResponse);
        // Perform GET request
        mockMvc.perform(get("/object_metadata/file1.txt")
                         .sessionAttr("user", "testUser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastModifiedDate").exists())
                .andExpect(jsonPath("$.partsCount", is(2)))
                .andExpect(jsonPath("$.storageClass", is("STANDARD")))
                .andExpect(jsonPath("$.serverSideEncryption", is("AES256")))
                .andExpect(jsonPath("$.versionId", is("v1")));
    }

    @Test
    void testGetObjectMetadataError() throws Exception {
        // Mock S3Client to throw an exception
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(new RuntimeException("S3 error"));

        // Perform GET request and expect an error field in the response
        mockMvc.perform(get("/object_metadata/file1.txt")
                        .sessionAttr("user", "testuser1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error", is("java.lang.RuntimeException: S3 error")));
    }
}
