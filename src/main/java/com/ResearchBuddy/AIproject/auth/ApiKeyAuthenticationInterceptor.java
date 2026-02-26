package com.ResearchBuddy.AIproject.auth;

import com.ResearchBuddy.AIproject.persistence.dto.ApiError;
import com.ResearchBuddy.AIproject.persistence.dto.enums.ErrorCodeType;
import com.ResearchBuddy.AIproject.persistence.entity.UserEntity;
import com.ResearchBuddy.AIproject.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationInterceptor implements HandlerInterceptor {

  public static final String API_KEY_HEADER = "X-API-Key";

  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String apiKey = request.getHeader(API_KEY_HEADER);
    if (apiKey == null || apiKey.isBlank()) {
      return writeAuthenticationError(
          request,
          response,
          ErrorCodeType.MISSING_API_KEY,
          "API key header is missing");
    }
    apiKey = apiKey.trim();

    Optional<UserEntity> user = userRepository.findByApiKey(apiKey);
    if (user.isEmpty()) {
      return writeAuthenticationError(
          request,
          response,
          ErrorCodeType.INVALID_API_KEY,
          "API key was not found");
    }

    if (!user.get().isActive()) {
      return writeAuthenticationError(
          request,
          response,
          ErrorCodeType.INACTIVE_ACCOUNT,
          "User account is inactive");
    }

    RequestUserContext.setAuthenticatedUser(request, user.get());
    return true;
  }

  private boolean writeAuthenticationError(
      HttpServletRequest request,
      HttpServletResponse response,
      ErrorCodeType errorCode,
      String message) throws Exception {
    ApiError error = ApiError.builder()
        .errorCode(errorCode)
        .message(message)
        .timestamp(Instant.now())
        .path(request.getRequestURI())
        .build();

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(error));
    return false;
  }
}
