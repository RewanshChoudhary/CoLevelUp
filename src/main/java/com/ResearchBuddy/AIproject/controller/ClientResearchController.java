package com.ResearchBuddy.AIproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ResearchBuddy.AIproject.auth.RequestUserContext;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchJobAcceptedResponse;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchJobStatusResponse;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchRequest;
import com.ResearchBuddy.AIproject.persistence.entity.UserEntity;
import com.ResearchBuddy.AIproject.service.ResearchRequestService;

import dev.langchain4j.model.output.Response;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/research")
public class ClientResearchController {
  private final ResearchRequestService requestService;

  @PostMapping("/send")
  public ResponseEntity<ResearchJobAcceptedResponse> addRequest(@Valid @RequestBody ResearchRequest researchRequest,
      HttpServletRequest request) {
    UserEntity user = RequestUserContext.getAuthenticatedUser(request).orElseThrow();
    ResearchJobAcceptedResponse response = requestService.createRequest(user, researchRequest);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .header("Location", response.getPollUrl())
        .body(response);

  }

  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<ResearchJobStatusResponse> fetchResearchStatus(@PathVariable String jobId,
      HttpServletRequest request) {
    UserEntity user = RequestUserContext.getAuthenticatedUser(request).orElseThrow();
    return ResponseEntity.ok(requestService.getStatus(jobId, user.getId()));

  }

}
