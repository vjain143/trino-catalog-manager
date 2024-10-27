package com.example.gitservice.controller;

import com.example.gitservice.model.OnboardingInput;
import com.example.gitservice.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/git")
public class GitController {

    @Autowired
    private GitService gitService;

    // Renamed push-to-git to createdatasource
    @PostMapping("/createdatasource")
    public ResponseEntity<?> createDatasource(@Valid @RequestBody OnboardingInput inputData, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        try {
            String branchName = gitService.createBranchAndPushContent(inputData);
            return ResponseEntity.ok("Content, egress rule, patch file, and properties file (if applicable) pushed successfully to branch: " + branchName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while processing the request: " + e.getMessage());
        }
    }

    // Renamed compare-branches to getdatasource
    @GetMapping("/getdatasource")
    public ResponseEntity<?> getDatasource(@RequestParam String featureBranch) {
        try {
            List<String> diffFiles = gitService.compareBranches(featureBranch);
            return ResponseEntity.ok().body(diffFiles.isEmpty() ? "No differences found." : diffFiles);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while comparing branches: " + e.getMessage());
        }
    }
}