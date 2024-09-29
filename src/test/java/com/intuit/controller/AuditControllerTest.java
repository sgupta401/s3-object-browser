package com.intuit.controller;

import com.intuit.dal.AuditRepository;
import com.intuit.entity.Audit;
import com.intuit.entity.AuditData;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(AuditController.class)  // WebMvcTest focuses only on the controller layer
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // This tells Spring to mock the AuditRepository
    private AuditRepository auditRepository;


//    @Mock
//    private AuditRepository auditRepository;

    @InjectMocks
    private AuditController auditController;


    @Mock
    private HttpSession httpSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
    }

    @Test
    void testGetAudits() throws Exception {
        // Mock data
        Audit audit1 = new Audit("file1.txt", "user1", "2023-09-28T10:15:30Z");
        Audit audit2 = new Audit("file2.txt", "user2", "2023-09-29T10:15:30Z");
        List<Audit> audits = Arrays.asList(audit1, audit2);

        // Mock repository behavior
        when(auditRepository.findAll()).thenReturn(audits);


        ResultActions actions =    mockMvc.perform(get("/audit_log")
                .contentType(MediaType.APPLICATION_JSON));
        actions
                .andExpect(status().isOk())  // Check that status is OK
                .andExpect(jsonPath("$.audits", hasSize(2)))  // Check that there are 2 audits
                .andExpect(jsonPath("$.audits[0].fileName", is("file1.txt")))  // Verify first audit
                .andExpect(jsonPath("$.audits[0].accessBy", is("user1")))  // Verify first audit's user
                .andExpect(jsonPath("$.audits[1].fileName", is("file2.txt")))  // Verify second audit
                .andExpect(jsonPath("$.audits[1].accessBy", is("user2")));  // Verify second audit's user
    }
}
