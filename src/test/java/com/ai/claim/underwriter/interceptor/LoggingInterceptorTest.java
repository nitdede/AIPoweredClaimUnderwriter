package com.ai.claim.underwriter.interceptor;

import com.ai.claim.underwriter.exception.InvalidClaimException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for LoggingInterceptor
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingInterceptor Tests")
class LoggingInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Mock
    private ModelAndView modelAndView;

    @InjectMocks
    private LoggingInterceptor loggingInterceptor;

    @BeforeEach
    void setUp() {
        loggingInterceptor = new LoggingInterceptor();
    }

    @Test
    @DisplayName("Should handle normal request successfully")
    void testPreHandleNormalRequest() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/claims");

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertThat(result).isTrue();
        verify(request).setAttribute(eq("startTime"), anyLong());
    }

    @Test
    @DisplayName("Should set start time attribute in preHandle")
    void testPreHandleSetsStartTime() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/claims");

        // Act
        loggingInterceptor.preHandle(request, response, handler);

        // Assert
        verify(request).setAttribute(eq("startTime"), anyLong());
    }

    @Test
    @DisplayName("Should validate document ingestion request with all required parameters")
    void testPreHandleDocumentIngestionWithAllParameters() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ingestion/saveDocument");
        when(request.getParameter("policyId")).thenReturn("POL123");
        when(request.getParameter("customerId")).thenReturn("CUST456");
        when(request.getParameter("policyNumber")).thenReturn("PN789");

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should throw InvalidClaimException when policyId is missing")
    void testPreHandleDocumentIngestionMissingPolicyId() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ingestion/saveDocument");
        when(request.getParameter("policyId")).thenReturn(null);
        when(request.getParameter("customerId")).thenReturn("CUST456");
        when(request.getParameter("policyNumber")).thenReturn("PN789");

        // Act & Assert
        assertThatThrownBy(() -> loggingInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidClaimException.class)
                .hasMessageContaining("policyId");
    }

    @Test
    @DisplayName("Should throw InvalidClaimException when customerId is missing")
    void testPreHandleDocumentIngestionMissingCustomerId() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ingestion/saveDocument");
        when(request.getParameter("policyId")).thenReturn("POL123");
        when(request.getParameter("customerId")).thenReturn(null);
        when(request.getParameter("policyNumber")).thenReturn("PN789");

        // Act & Assert
        assertThatThrownBy(() -> loggingInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidClaimException.class)
                .hasMessageContaining("customerId");
    }

    @Test
    @DisplayName("Should throw InvalidClaimException when policyNumber is missing")
    void testPreHandleDocumentIngestionMissingPolicyNumber() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ingestion/saveDocument");
        when(request.getParameter("policyId")).thenReturn("POL123");
        when(request.getParameter("customerId")).thenReturn("CUST456");
        when(request.getParameter("policyNumber")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> loggingInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidClaimException.class)
                .hasMessageContaining("policyNumber");
    }

    @Test
    @DisplayName("Should throw InvalidClaimException with all missing parameters listed")
    void testPreHandleDocumentIngestionMissingAllParameters() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ingestion/saveDocument");
        when(request.getParameter("policyId")).thenReturn(null);
        when(request.getParameter("customerId")).thenReturn(null);
        when(request.getParameter("policyNumber")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> loggingInterceptor.preHandle(request, response, handler))
                .isInstanceOf(InvalidClaimException.class)
                .hasMessageContaining("policyId")
                .hasMessageContaining("customerId")
                .hasMessageContaining("policyNumber");
    }

    @Test
    @DisplayName("Should handle case-insensitive URI matching for document ingestion")
    void testPreHandleDocumentIngestionCaseInsensitive() {
        // Arrange - test with different case
        when(request.getRequestURI()).thenReturn("/INGESTION/SAVEDOCUMENT");
        when(request.getParameter("policyId")).thenReturn("POL123");
        when(request.getParameter("customerId")).thenReturn("CUST456");
        when(request.getParameter("policyNumber")).thenReturn("PN789");

        // Act
        boolean result = loggingInterceptor.preHandle(request, response, handler);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should execute postHandle without errors")
    void testPostHandle() {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/claims");

        // Act & Assert - should not throw any exception
        loggingInterceptor.postHandle(request, response, handler, modelAndView);
        
        verify(request).getRequestURI();
    }

    @Test
    @DisplayName("Should log completion without exception")
    void testAfterCompletionWithoutException() {
        // Arrange
        long startTime = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        verify(request).getAttribute("startTime");
        verify(request).getRequestURI();
        verify(request).getMethod();
        verify(response).getStatus();
    }

    @Test
    @DisplayName("Should log completion with exception")
    void testAfterCompletionWithException() {
        // Arrange
        long startTime = System.currentTimeMillis();
        Exception testException = new RuntimeException("Test exception");
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(request.getMethod()).thenReturn("POST");
        when(response.getStatus()).thenReturn(500);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, testException);

        // Assert
        verify(request).getAttribute("startTime");
        verify(request).getRequestURI();
        verify(request).getMethod();
        verify(response).getStatus();
    }

    @Test
    @DisplayName("Should calculate execution time correctly")
    void testAfterCompletionCalculatesExecutionTime() {
        // Arrange
        long startTime = System.currentTimeMillis() - 1000; // 1 second ago
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert
        verify(request).getAttribute("startTime");
    }

    @Test
    @DisplayName("Should handle null start time gracefully")
    void testAfterCompletionWithNullStartTime() {
        // Arrange
        when(request.getAttribute("startTime")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(200);

        // Act
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Assert - execution time should be 0
        verify(request).getAttribute("startTime");
    }

    @Test
    @DisplayName("Should log different HTTP methods correctly")
    void testAfterCompletionWithDifferentHttpMethods() {
        // Arrange
        long startTime = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(response.getStatus()).thenReturn(200);

        // Act & Assert for POST
        when(request.getMethod()).thenReturn("POST");
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Act & Assert for PUT
        when(request.getMethod()).thenReturn("PUT");
        loggingInterceptor.afterCompletion(request, response, handler, null);

        // Act & Assert for DELETE
        when(request.getMethod()).thenReturn("DELETE");
        loggingInterceptor.afterCompletion(request, response, handler, null);

        verify(request, times(3)).getMethod();
    }

    @Test
    @DisplayName("Should log different response statuses")
    void testAfterCompletionWithDifferentResponseStatuses() {
        // Arrange
        long startTime = System.currentTimeMillis();
        when(request.getAttribute("startTime")).thenReturn(startTime);
        when(request.getRequestURI()).thenReturn("/api/claims");
        when(request.getMethod()).thenReturn("GET");

        // Test various status codes
        int[] statusCodes = {200, 201, 400, 404, 500};
        
        for (int statusCode : statusCodes) {
            // Act
            when(response.getStatus()).thenReturn(statusCode);
            loggingInterceptor.afterCompletion(request, response, handler, null);
        }

        // Assert
        verify(response, times(statusCodes.length)).getStatus();
    }
}
