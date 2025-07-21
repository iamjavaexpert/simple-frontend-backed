package com.example.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * Configuration class that provides beans for the application context.
 *
 * This class is used to define beans that are managed by the Spring container.
 * In this case, it defines a bean for the RestTemplate, which is used for making HTTP requests.
 */
@Configuration
class AppConfig {

    /**
     * Creates and returns a RestTemplate bean.
     *
     * The RestTemplate is a central class for making HTTP requests in Spring.
     * By defining this bean in the application context, Spring will manage the lifecycle of
     * the RestTemplate instance, allowing it to be injected wherever needed in the application.
     *
     * @return A new instance of RestTemplate.
     */
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}