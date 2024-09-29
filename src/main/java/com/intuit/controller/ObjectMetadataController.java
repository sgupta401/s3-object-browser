package com.intuit.controller;

import com.intuit.entity.S3Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@RestController

public class ObjectMetadataController {

  @Autowired S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  // This endpoint is protected and requires authentication
  @GetMapping(value = "/object_metadata/{fileName}", produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody S3Metadata getObjectMetadata(@PathVariable String fileName) {
    S3Metadata s3Metadata = null;
    // Define the bucket name and object key

    String objectKey = fileName;

    // Create a HeadObjectRequest to get object metadata
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();

    try {
      // Fetch object metadata
      HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

      if (headObjectResponse != null) {
        s3Metadata = new S3Metadata();

        s3Metadata.setLastModifiedDate(headObjectResponse.lastModified());
        s3Metadata.setPartsCount(headObjectResponse.partsCount());
        s3Metadata.setStorageClass(headObjectResponse.storageClassAsString());
        s3Metadata.setServerSideEncryption(headObjectResponse.serverSideEncryptionAsString());
        s3Metadata.setVersionId(headObjectResponse.versionId());
      }
    } catch (Exception e) {
      s3Metadata = new S3Metadata();
      s3Metadata.setError(e.toString());
    }
    return s3Metadata;
  }
 }
