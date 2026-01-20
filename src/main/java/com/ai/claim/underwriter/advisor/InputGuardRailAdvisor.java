package com.ai.claim.underwriter.advisor;

import com.ai.claim.underwriter.config.GuardrailProperties;
import com.ai.claim.underwriter.exception.SensitiveKeywordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

import java.util.Locale;

public class InputGuardRailAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(InputGuardRailAdvisor.class);
    private final GuardrailProperties guardrailProperties;

    public InputGuardRailAdvisor(GuardrailProperties guardrailProperties) {
        this.guardrailProperties = guardrailProperties;
        logger.info("InputGuardRailAdvisor initialized with {} injection keywords and {} blocked topics",
                guardrailProperties.getInjectionKeywords().size(),
                guardrailProperties.getBlockedTopics().size());
    }

    @Override
    public String getName() {
        return "InputGuardrailAdvisor";
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userMessaeg = chatClientRequest.prompt().getUserMessage().getText().toLowerCase(Locale.ROOT);

        logger.debug("Checking user message: {}", userMessaeg);
        logger.debug("Injection keywords to check: {}", guardrailProperties.getInjectionKeywords());
        logger.debug("Blocked topics to check: {}", guardrailProperties.getBlockedTopics());

        for(String blockedTopic : guardrailProperties.getBlockedTopics()) {
            if(userMessaeg.contains(blockedTopic.toLowerCase(Locale.ROOT))) {
                logger.warn("Blocked topic detected: {}", blockedTopic);
                throw new SensitiveKeywordException("Input contains blocked topic: " + blockedTopic);
            }
        }

        for(String injectionKeyword : guardrailProperties.getInjectionKeywords()) {
            if(userMessaeg.contains(injectionKeyword.toLowerCase(Locale.ROOT))) {
                logger.warn("Injection keyword detected: {}", injectionKeyword);
                throw new SensitiveKeywordException("Input contains potential injection keyword: " + injectionKeyword);
            }
        }

        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }


    // Lower value => request me sabse pehle chalega (response me last)  [oai_citation:4‡Home](https://docs.spring.io/spring-ai/reference/api/advisors.html?utm_source=chatgpt.com)
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
