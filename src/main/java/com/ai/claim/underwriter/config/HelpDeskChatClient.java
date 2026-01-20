package com.ai.claim.underwriter.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class HelpDeskChatClient {

    @Value("classpath:/templates/helpDeskSystemPromptTemplate.st")
    Resource systemPromptTemplate;

    @Autowired
    ToolCallbackProvider toolCallbackProvider;

    @Bean(name = "helpDeskClient")
    public ChatClient helpDeskClient(ChatClient.Builder clientBuilder, ChatMemory memory, ToolCallbackProvider toolCallbackProvider) {
        return clientBuilder
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultSystem(systemPromptTemplate)
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(memory).build()))
                .build();
    }
}
