package com.intuit.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.dal.AuditRepository;
import com.intuit.entity.Audit;
import com.intuit.entity.S3Metadata;
import com.intuit.service.RsaKeyService;
import jakarta.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class RestEndpointAuditAspectTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private HttpSession httpSession;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Logger logger;


    @Mock
    RsaKeyService rsaKeyService;

    @InjectMocks
    private RestEndpointAuditAspect restEndpointAuditAspect;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        // Set up mocks as needed
        Mockito.doNothing().when(rsaKeyService).init();

    }

    @Test
    void aroundAdvice_whenNoErrorInMetadata_shouldLogAndSaveAudit() throws Throwable {
        // Mock the arguments for joinPoint and httpSession
        String fileName = "test-file.txt";
        when(joinPoint.getArgs()).thenReturn(new Object[]{fileName});
        when(httpSession.getAttribute("user")).thenReturn("testUser");

        // Mock the result from joinPoint.proceed
        S3Metadata metadata = new S3Metadata();
        metadata.setError(null); // No error in metadata
        when(joinPoint.proceed()).thenReturn(metadata);

        // Call the method
        restEndpointAuditAspect.aroundAdvice(joinPoint);

        // Verify that audit is saved
        verify(auditRepository, times(1)).save(any(Audit.class));

        // Verify that logging occurred
        verify(logger, times(1)).info(contains("Audit Log"));
    }

    @Test
    void aroundAdvice_whenErrorInMetadata_shouldNotSaveAudit() throws Throwable {
        // Mock the arguments for joinPoint
        String fileName = "test-file.txt";
        when(joinPoint.getArgs()).thenReturn(new Object[]{fileName});

        // Mock the result from joinPoint.proceed with an error in metadata
        S3Metadata metadata = new S3Metadata();
        metadata.setError("some error");
        when(joinPoint.proceed()).thenReturn(metadata);

        // Call the method
        restEndpointAuditAspect.aroundAdvice(joinPoint);

        // Verify that audit is NOT saved
        verify(auditRepository, never()).save(any(Audit.class));

        // Verify that no logging occurred
        verify(logger, never()).info(anyString());
    }

    @Test
    void aroundAdvice_whenExceptionThrown_shouldLogError() throws Throwable {
        // Mock the arguments for joinPoint
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test-file.txt"});

        // Mock the exception thrown by joinPoint.proceed
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test Exception"));

        // Call the method and expect it to rethrow the exception
        try {
            restEndpointAuditAspect.aroundAdvice(joinPoint);
        } catch (Exception e) {
            // Expected exception
        }

        // Verify that the error is logged
        verify(logger, times(1)).error(contains("Exception caught in Aspect"));

        // Verify that audit is NOT saved due to the exception
        verify(auditRepository, never()).save(any(Audit.class));
    }
}