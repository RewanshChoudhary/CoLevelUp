package com.ResearchBuddy.AIproject.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .setValidator(validator)
        .build();
  }

  @Test
  void returnsSimpleErrorForIllegalArgumentException() throws Exception {
    mockMvc.perform(get("/test/illegal"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Invalid request payload"))
        .andExpect(jsonPath("$.path").value("/test/illegal"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void returnsSimpleErrorWithDetailsForValidationFailure() throws Exception {
    mockMvc.perform(post("/test/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.path").value("/test/validate"))
        .andExpect(jsonPath("$.details.name").exists());
  }

  @Test
  void returnsSimpleErrorForUnexpectedException() throws Exception {
    mockMvc.perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("Internal server error"))
        .andExpect(jsonPath("$.path").value("/test/unexpected"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @RestController
  static class TestController {

    @PostMapping("/test/validate")
    String validate(@Valid @RequestBody TestBody body) {
      return "ok";
    }

    @org.springframework.web.bind.annotation.GetMapping("/test/illegal")
    String illegal() {
      throw new IllegalArgumentException("Invalid request payload");
    }

    @org.springframework.web.bind.annotation.GetMapping("/test/unexpected")
    String unexpected() {
      throw new RuntimeException("boom");
    }
  }

  static class TestBody {
    @NotBlank
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
