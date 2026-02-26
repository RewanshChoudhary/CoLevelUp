package com.ResearchBuddy.AIproject.config;

import com.ResearchBuddy.AIproject.auth.ApiKeyAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final ApiKeyAuthenticationInterceptor apiKeyAuthenticationInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiKeyAuthenticationInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/error");
  }
}
