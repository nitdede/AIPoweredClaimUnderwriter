package com.ai.claim.underwriter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.guardrails")
public class GuardrailProperties {
    private List<String> injectionKeywords = List.of();
    private List<String> blockedTopics = List.of();

    public List<String> getInjectionKeywords() {
        return injectionKeywords;
    }

    public void setInjectionKeywords(List<String> injectionKeywords) {
        this.injectionKeywords = injectionKeywords;
    }

    public List<String> getBlockedTopics() {
        return blockedTopics;
    }

    public void setBlockedTopics(List<String> blockedTopics) {
        this.blockedTopics = blockedTopics;
    }
}
