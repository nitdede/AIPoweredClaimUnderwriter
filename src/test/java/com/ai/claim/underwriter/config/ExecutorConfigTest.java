package com.ai.claim.underwriter.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for ExecutorConfig
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutorConfig Tests")
class ExecutorConfigTest {

    @InjectMocks
    private ExecutorConfig executorConfig;

    @BeforeEach
    void setUp() {
        executorConfig = new ExecutorConfig();
    }

    @Test
    @DisplayName("Should create blocking task executor with correct configuration")
    void testBlockingTaskExecutor() {
        // Arrange & Act
        Executor executor = executorConfig.blockingTaskExecutor();

        // Assert
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(threadPoolExecutor.getCorePoolSize()).isEqualTo(4);
        assertThat(threadPoolExecutor.getMaxPoolSize()).isEqualTo(10);
        assertThat(threadPoolExecutor.getQueueCapacity()).isEqualTo(200);
        assertThat(threadPoolExecutor.getThreadNamePrefix()).isEqualTo("claim-Exec-");
    }

    @Test
    @DisplayName("Should create vector task executor with correct configuration")
    void testVectorTaskExecutor() {
        // Arrange & Act
        Executor executor = executorConfig.vectorTaskExecutor();

        // Assert
        assertThat(executor).isNotNull();
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;
        assertThat(threadPoolExecutor.getCorePoolSize()).isEqualTo(2);
        assertThat(threadPoolExecutor.getMaxPoolSize()).isEqualTo(6);
        assertThat(threadPoolExecutor.getQueueCapacity()).isEqualTo(200);
        assertThat(threadPoolExecutor.getThreadNamePrefix()).isEqualTo("vector-search-");
    }

    @Test
    @DisplayName("Should initialize blocking task executor properly")
    void testBlockingTaskExecutorInitialization() {
        // Arrange & Act
        Executor executor = executorConfig.blockingTaskExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        // Assert
        assertThat(threadPoolExecutor.getThreadPoolExecutor()).isNotNull();
        assertThat(threadPoolExecutor.getThreadPoolExecutor().isShutdown()).isFalse();
    }

    @Test
    @DisplayName("Should initialize vector task executor properly")
    void testVectorTaskExecutorInitialization() {
        // Arrange & Act
        Executor executor = executorConfig.vectorTaskExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        // Assert
        assertThat(threadPoolExecutor.getThreadPoolExecutor()).isNotNull();
        assertThat(threadPoolExecutor.getThreadPoolExecutor().isShutdown()).isFalse();
    }

    @Test
    @DisplayName("Should create independent executor instances")
    void testExecutorsAreIndependent() {
        // Arrange & Act
        Executor blockingExecutor = executorConfig.blockingTaskExecutor();
        Executor vectorExecutor = executorConfig.vectorTaskExecutor();

        // Assert
        assertThat(blockingExecutor).isNotSameAs(vectorExecutor);
    }
}
