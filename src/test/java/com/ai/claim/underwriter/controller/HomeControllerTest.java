package com.ai.claim.underwriter.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private HomeController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void home_returnsForwardToIndexHtml() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void home_directCall_returnsCorrectString() {
        // Act
        String result = controller.home();

        // Assert
        assertThat(result).isEqualTo("forward:/index.html");
    }

    @Test
    void home_withQueryParameters_stillForwardsToIndex() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/")
                        .param("test", "value")
                        .param("another", "param"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void home_withTrailingSlash_forwards() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void home_multipleCallsReturnSameResult() {
        // Act
        String result1 = controller.home();
        String result2 = controller.home();
        String result3 = controller.home();

        // Assert
        assertThat(result1).isEqualTo("forward:/index.html");
        assertThat(result2).isEqualTo("forward:/index.html");
        assertThat(result3).isEqualTo("forward:/index.html");
        assertThat(result1).isEqualTo(result2).isEqualTo(result3);
    }
}
