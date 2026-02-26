package com.ResearchBuddy.AIproject.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.ResearchBuddy.AIproject.persistence.entity.UserEntity;
import com.ResearchBuddy.AIproject.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApiKeyAuthenticationInterceptorTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  @Test
  void preHandleReturnsUnauthorizedWhenHeaderMissing() throws Exception {
    AtomicInteger lookupCount = new AtomicInteger(0);
    UserRepository userRepository = createStubRepository(Optional.empty(), lookupCount);
    ApiKeyAuthenticationInterceptor interceptor =
        new ApiKeyAuthenticationInterceptor(userRepository, OBJECT_MAPPER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/research");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(lookupCount.get()).isZero();
    JsonNode responseBody = OBJECT_MAPPER.readTree(response.getContentAsString());
    assertThat(responseBody.path("errorCode").asText()).isEqualTo("MISSING_API_KEY");
    assertThat(responseBody.path("message").asText()).isEqualTo("API key header is missing");
    assertThat(responseBody.path("path").asText()).isEqualTo("/api/research");
  }

  @Test
  void preHandleReturnsUnauthorizedWhenApiKeyIsInvalid() throws Exception {
    AtomicInteger lookupCount = new AtomicInteger(0);
    UserRepository userRepository = createStubRepository(Optional.empty(), lookupCount);
    ApiKeyAuthenticationInterceptor interceptor =
        new ApiKeyAuthenticationInterceptor(userRepository, OBJECT_MAPPER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/research");
    request.addHeader(ApiKeyAuthenticationInterceptor.API_KEY_HEADER, "invalid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(lookupCount.get()).isEqualTo(1);
    assertThat(RequestUserContext.getAuthenticatedUser(request)).isEmpty();
    JsonNode responseBody = OBJECT_MAPPER.readTree(response.getContentAsString());
    assertThat(responseBody.path("errorCode").asText()).isEqualTo("INVALID_API_KEY");
    assertThat(responseBody.path("message").asText()).isEqualTo("API key was not found");
  }

  @Test
  void preHandleReturnsUnauthorizedWhenAccountIsInactive() throws Exception {
    AtomicInteger lookupCount = new AtomicInteger(0);
    UserEntity inactiveUser = UserEntity.builder()
        .email("inactive@example.com")
        .apiKey("inactive-key")
        .active(false)
        .build();
    UserRepository userRepository = createStubRepository(Optional.of(inactiveUser), lookupCount);
    ApiKeyAuthenticationInterceptor interceptor =
        new ApiKeyAuthenticationInterceptor(userRepository, OBJECT_MAPPER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/research");
    request.addHeader(ApiKeyAuthenticationInterceptor.API_KEY_HEADER, "inactive-key");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isFalse();
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(lookupCount.get()).isEqualTo(1);
    JsonNode responseBody = OBJECT_MAPPER.readTree(response.getContentAsString());
    assertThat(responseBody.path("errorCode").asText()).isEqualTo("INACTIVE_ACCOUNT");
    assertThat(responseBody.path("message").asText()).isEqualTo("User account is inactive");
  }

  @Test
  void preHandleAttachesUserToRequestContextWhenApiKeyIsValid() throws Exception {
    AtomicInteger lookupCount = new AtomicInteger(0);
    UserEntity activeUser = UserEntity.builder()
        .email("active@example.com")
        .apiKey("valid-key")
        .active(true)
        .build();
    UserRepository userRepository = createStubRepository(Optional.of(activeUser), lookupCount);
    ApiKeyAuthenticationInterceptor interceptor =
        new ApiKeyAuthenticationInterceptor(userRepository, OBJECT_MAPPER);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/research");
    request.addHeader(ApiKeyAuthenticationInterceptor.API_KEY_HEADER, "valid-key");
    MockHttpServletResponse response = new MockHttpServletResponse();

    boolean result = interceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(lookupCount.get()).isEqualTo(1);
    assertThat(RequestUserContext.getAuthenticatedUser(request)).contains(activeUser);
  }

  private UserRepository createStubRepository(Optional<UserEntity> user, AtomicInteger lookupCount) {
    return (UserRepository) Proxy.newProxyInstance(
        UserRepository.class.getClassLoader(),
        new Class<?>[] {UserRepository.class},
        (proxy, method, args) -> {
          if ("findByApiKey".equals(method.getName())) {
            lookupCount.incrementAndGet();
            return user;
          }
          if ("findByApiKeyAndActiveTrue".equals(method.getName())) {
            lookupCount.incrementAndGet();
            return user.filter(UserEntity::isActive);
          }
          if ("toString".equals(method.getName())) {
            return "UserRepositoryStub";
          }
          if ("hashCode".equals(method.getName())) {
            return System.identityHashCode(proxy);
          }
          if ("equals".equals(method.getName())) {
            return proxy == args[0];
          }
          throw new UnsupportedOperationException("Unexpected repository method: " + method.getName());
        });
  }
}
