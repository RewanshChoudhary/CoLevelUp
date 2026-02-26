package com.ResearchBuddy.AIproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ResearchBuddy.AIproject.persistence.dto.ResearchJobAcceptedResponse;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchJobStatusResponse;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchRequest;
import com.ResearchBuddy.AIproject.service.ResearchRequestService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/research")
public class ClientResearchController {
  private final ResearchRequestService requestService;

  @PostMapping("/send")
  public ResponseEntity<ResearchJobAcceptedResponse> sendResearchJobRequest(
      @RequestBody ResearchRequest researchRequest) {
    String jobId = requestService.createRequest(researchRequest);
    ResearchJobAcceptedResponse response=requestResponseService.
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(response);

  }
  @GetMapping("/jobs/{jobId}")
  public ResponseEntity<ResearchJobStatusResponse> fetchResearchStatus(@PathVariable String jobId){

  }

}
