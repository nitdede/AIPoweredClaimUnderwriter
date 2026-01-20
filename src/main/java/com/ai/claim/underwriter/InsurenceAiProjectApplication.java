package com.ai.claim.underwriter;

import com.ai.claim.underwriter.config.GuardrailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GuardrailProperties.class)
public class InsurenceAiProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(InsurenceAiProjectApplication.class, args);
	}

}
