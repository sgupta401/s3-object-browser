package com.intuit.audit;

import com.intuit.dal.AuditRepository;
import com.intuit.entity.Audit;
import com.intuit.entity.S3Metadata;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import org.slf4j.Logger;


@Aspect
@Component
public class RestEndpointAuditAspect {

  @Autowired AuditRepository auditRepository;

  @Autowired private HttpSession httpSession;
  Logger logger = LoggerFactory.getLogger(RestEndpointAuditAspect.class);

//  @Pointcut("within(com.intuit.controller.ProtectedResourceController)")
//  public void restControllerMethods() {}

  @Around("execution(* com.intuit.controller.ObjectMetadataController.getObjectMetadata*(..))")
  public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = null;
    try {
      // This line will execute the method
      result = joinPoint.proceed();
    } catch (Throwable ex) {
      // This block will not run if the exception is caught within the method
      logger.error("Exception caught in Aspect: " + ex.getMessage());
      throw ex; // Optionally rethrow
    } finally {
      // This block will always execute, regardless of method outcome
      S3Metadata s3Metadata = (S3Metadata) result;
      if (StringUtils.isBlank(s3Metadata.getError())) {
        String user = (String) httpSession.getAttribute("user");
        String accessTime = Instant.now().toString();
        String fileName = (String) joinPoint.getArgs()[0];
        Audit audit = new Audit(fileName, user, accessTime);
        logger.info(
            "Audit Log: Object: "
                + fileName
                + " accessed by : "
                + user
                + ", access time :"
                + accessTime);
        auditRepository.save(audit);
      }
    }
    return result;
  }

//  @AfterReturning("restControllerMethods()")
//  public void afterRestControllerExecution(JoinPoint joinPoint) {
//    String user = (String) httpSession.getAttribute("user");
//    String accessTime = Instant.now().toString();
//    String fileName = (String) joinPoint.getArgs()[0];
//    Audit audit = new Audit(fileName, user, accessTime);
//    System.out.println(
//        "Audit Log: Object: "
//            + fileName
//            + " accessed by : "
//            + user
//            + ", access time :"
//            + accessTime);
//    auditRepository.save(audit);
//  }
}
