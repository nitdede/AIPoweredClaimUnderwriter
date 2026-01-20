package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.model.PolicyMataData;
import com.ai.claim.underwriter.service.PolicyIngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*@WebMvcTest(PolicyIngestionController.class)
class PolicyIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyIngestionService policyIngestionService;

    @Test
    void testEndpoint_returnsHealthMessage() throws Exception {
        mockMvc.perform(get("/ingestion/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is working!"));
    }

    @Test
    void saveDocument_returnsServiceResult() throws Exception {
        when(policyIngestionService.performRAG(eq("classpath:policy.txt"), any()))
                .thenReturn("Policy stored in vector DB. Chunks:1");

        PolicyMataData request = new PolicyMataData("pid-1", "cust-1", "pol-1");

        mockMvc.perform(post("/ingestion/saveDocument")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(is("Policy stored in vector DB. Chunks:1")));

        verify(policyIngestionService).performRAG(eq("classpath:policy.txt"), any(PolicyMataData.class));
    }
}
*/