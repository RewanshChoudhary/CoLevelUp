package com.ResearchBuddy.AIproject.auth;

import com.ResearchBuddy.AIproject.persistence.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public final class RequestUserContext {

  public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

  private RequestUserContext() {
  }

  public static void setAuthenticatedUser(HttpServletRequest request, UserEntity user) {
    request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
  }

  public static Optional<UserEntity> getAuthenticatedUser(HttpServletRequest request) {
    Object value = request.getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
    if (value instanceof UserEntity user) {
      return Optional.of(user);
    }
    return Optional.empty();
  }
}
