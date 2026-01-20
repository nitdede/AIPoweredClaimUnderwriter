package com.ai.claim.underwriter.config;

import com.ai.claim.underwriter.interceptor.LoggingInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for WebConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebConfig Tests")
class WebConfigTest {

    @Mock
    private LoggingInterceptor loggingInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Captor
    private ArgumentCaptor<String[]> pathPatternsCaptor;

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig(loggingInterceptor);
    }

    @Test
    @DisplayName("Should inject logging interceptor via constructor")
    void testConstructorInjection() {
        // Arrange & Act
        WebConfig config = new WebConfig(loggingInterceptor);

        // Assert
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Should register logging interceptor")
    void testAddInterceptors() {
        // Arrange
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistry).addInterceptor(loggingInterceptor);
    }

    @Test
    @DisplayName("Should apply interceptor to all paths")
    void testInterceptorAppliedToAllPaths() {
        // Arrange
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistration).addPathPatterns("/**");
    }

    @Test
    @DisplayName("Should exclude static resource paths")
    void testExcludeStaticResourcePaths() {
        // Arrange
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistration).excludePathPatterns("/static/**", "/css/**", "/js/**", "/node/**", "/images/**");
    }

    @Test
    @DisplayName("Should configure interceptor in correct order")
    void testInterceptorConfigurationOrder() {
        // Arrange
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // Act
        webConfig.addInterceptors(interceptorRegistry);

        // Assert - verify method call order
        var inOrder = inOrder(interceptorRegistry, interceptorRegistration);
        inOrder.verify(interceptorRegistry).addInterceptor(loggingInterceptor);
        inOrder.verify(interceptorRegistration).addPathPatterns("/**");
        inOrder.verify(interceptorRegistration).excludePathPatterns("/static/**", "/css/**", "/js/**", "/node/**", "/images/**");
    }

    @Test
    @DisplayName("Should handle null logging interceptor gracefully")
    void testNullLoggingInterceptor() {
        // Arrange
        WebConfig configWithNull = new WebConfig(null);
        when(interceptorRegistry.addInterceptor(any())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.addPathPatterns(anyString())).thenReturn(interceptorRegistration);
        when(interceptorRegistration.excludePathPatterns(any(String[].class))).thenReturn(interceptorRegistration);

        // Act
        configWithNull.addInterceptors(interceptorRegistry);

        // Assert
        verify(interceptorRegistry).addInterceptor(null);
    }
}
