package com.example.gitservice.service;

import com.example.gitservice.exception.FileProcessingException;
import com.example.gitservice.exception.GitOperationException;
import com.example.gitservice.model.OnboardingInput;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GitServiceTest {

    @InjectMocks
    private GitService gitService;

    @Mock
    private Git gitMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateBranchAndPushContent_Success() throws GitAPIException, IOException {
        OnboardingInput input = new OnboardingInput();
        input.setJiraTicket("TEST-123");
        input.setContent("Test content");
        input.setHostname("example.com");
        input.setProxyHost("proxy.example.com");
        input.setProxyPort(8080);
        input.setNameOfSecretKey("SECRET_KEY");
        input.setNameOfSecretKeyValue("secret-value");
        input.setDatasourceType("api-internal");

        // Mock Git operations
        when(gitMock.checkout()).thenReturn(null);
        when(gitMock.add()).thenReturn(null);
        when(gitMock.commit()).thenReturn(null);
        when(gitMock.push()).thenReturn(null);

        String branchName = gitService.createBranchAndPushContent(input);

        assertEquals("feature/TEST-123", branchName);
        verify(gitMock, times(1)).checkout();
        verify(gitMock, times(1)).add();
        verify(gitMock, times(1)).commit();
        verify(gitMock, times(1)).push();
    }

    @Test
    void testCreateBranchAndPushContent_FileProcessingException() {
        OnboardingInput input = new OnboardingInput();
        input.setJiraTicket("TEST-124");

        doThrow(new IOException("File error")).when(gitMock).commit();

        assertThrows(FileProcessingException.class, () -> gitService.createBranchAndPushContent(input));
    }

    @Test
    void testCompareBranches_GitOperationException() {
        String featureBranch = "nonexistent-branch";

        doThrow(new GitAPIException("Branch not found") {}).when(gitMock).checkout();

        assertThrows(GitOperationException.class, () -> gitService.compareBranches(featureBranch));
    }
}