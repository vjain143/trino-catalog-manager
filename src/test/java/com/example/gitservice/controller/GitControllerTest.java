package com.example.gitservice.controller;

import com.example.gitservice.model.OnboardingInput;
import com.example.gitservice.service.GitService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GitControllerTest {

    @Mock
    private GitService gitService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private GitController gitController;

    public GitControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDatasource_Success() {
        OnboardingInput input = new OnboardingInput();
        input.setJiraTicket("TEST-123");
        input.setContent("Test content");
        input.setHostname("example.com");
        input.setProxyHost("proxy.example.com");
        input.setProxyPort(8080);
        input.setNameOfSecretKey("SECRET_KEY");
        input.setNameOfSecretKeyValue("secret-value");
        input.setDatasourceType("api-internal");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(gitService.createBranchAndPushContent(input)).thenReturn("feature/TEST-123");

        ResponseEntity<?> response = gitController.createDatasource(input, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Content, egress rule, patch file, and properties file (if applicable) pushed successfully to branch: feature/TEST-123", response.getBody());
    }

    @Test
    void testCreateDatasource_ValidationError() {
        OnboardingInput input = new OnboardingInput();
        when(bindingResult.hasErrors()).thenReturn(true);

        ResponseEntity<?> response = gitController.createDatasource(input, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetDatasource_Success() {
        String featureBranch = "feature/TEST-123";
        when(gitService.compareBranches(featureBranch)).thenReturn(List.of("file1.txt", "file2.txt"));

        ResponseEntity<?> response = gitController.getDatasource(featureBranch);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("file1.txt", "file2.txt"), response.getBody());
    }
}