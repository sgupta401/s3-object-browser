package com.intuit.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    public S3HealthIndicator(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public Health health() {
        try {
            ListBucketsResponse buckets = s3Client.listBuckets();
            return Health.up().withDetail("buckets", buckets.buckets().size()).build();
        } catch (S3Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}