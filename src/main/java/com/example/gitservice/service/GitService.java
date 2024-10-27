package com.example.gitservice.service;

import com.example.gitservice.exception.FileProcessingException;
import com.example.gitservice.exception.GitOperationException;
import com.example.gitservice.model.OnboardingInput;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class GitService {

    @Value("${github.repo.url}")
    private String repoUrl;

    @Value("${github.username}")
    private String username;

    @Value("${github.token}")
    private String token;

    public String createBranchAndPushContent(OnboardingInput input) {
        String branchName = "feature/" + input.getJiraTicket();
        File localRepoDir = new File("local-repo");

        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localRepoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                .call()) {

            // Create and switch to the new branch
            git.checkout().setCreateBranch(true).setName(branchName).call();

            // Create and add a new file with content
            File newFile = new File(localRepoDir, input.getJiraTicket() + "_file.txt");
            try (FileWriter writer = new FileWriter(newFile)) {
                writer.write(input.getContent());
            } catch (IOException e) {
                throw new FileProcessingException("Failed to write content to file", e);
            }

            // Create additional files based on datasourceType
            updateEgressRuleFile(localRepoDir, input.getHostname(), input.getProxyHost(), input.getProxyPort());
            updateKubernetesPatchFile(localRepoDir, input.getHostname(), input.getNameOfSecretKey(), input.getNameOfSecretKeyValue(), input.getDatasourceType());

            if ("database".equalsIgnoreCase(input.getDatasourceType())) {
                createDatabasePropertiesFile(localRepoDir, input.getHostname(), input.getProxyHost(), input.getProxyPort());
                updateKustomizationFile(localRepoDir);
            }

            // Stage, commit, and push changes
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Add content for " + input.getJiraTicket()).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();

        } catch (GitAPIException e) {
            throw new GitOperationException("Error during Git operation", e);
        }

        return branchName;
    }

    public List<String> compareBranches(String featureBranchName) {
        List<String> diffFiles = new ArrayList<>();
        File localRepoDir = new File("local-repo");

        try (Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localRepoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                .call()) {

            // Checkout master and fetch the latest changes
            git.checkout().setName("master").call();
            git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();

            // Checkout the feature branch and fetch the latest changes
            git.checkout().setName(featureBranchName).call();
            git.pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token)).call();

            // Compare files between master and feature branch
            ObjectId masterHead = git.getRepository().resolve("refs/heads/master^{tree}");
            ObjectId featureHead = git.getRepository().resolve("refs/heads/" + featureBranchName + "^{tree}");
            diffFiles = getDiffFiles(git.getRepository(), masterHead, featureHead);

        } catch (GitAPIException | IOException e) {
            throw new GitOperationException("Error comparing branches", e);
        }

        return diffFiles;
    }

    private List<String> getDiffFiles(Repository repository, ObjectId oldHead, ObjectId newHead) throws GitAPIException, IOException {
        List<String> diffFiles = new ArrayList<>();

        try (Git git = new Git(repository)) {
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldHead);
            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newHead);

            List<DiffEntry> diffEntries = git.diff()
                    .setOldTree(oldTreeParser)
                    .setNewTree(newTreeParser)
                    .call();

            for (DiffEntry entry : diffEntries) {
                if (entry.getChangeType() == DiffEntry.ChangeType.ADD) {
                    diffFiles.add(entry.getNewPath());
                }
            }
        }

        return diffFiles;
    }

    private AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (var reader = repository.newObjectReader()) {
            treeParser.reset(reader, objectId);
        }
        return treeParser;
    }

    private void updateEgressRuleFile(File repoDir, String hostname, String proxyHost, int proxyPort) {
        File egressFile = new File(repoDir, "egress-rules.yaml");

        Map<String, Object> egressRule = new HashMap<>();
        egressRule.put("apiVersion", "networking.k8s.io/v1");
        egressRule.put("kind", "NetworkPolicy");
        egressRule.put("metadata", Map.of("name", "allow-egress-to-hostname"));

        Map<String, Object> spec = new HashMap<>();
        spec.put("podSelector", Map.of());

        Map<String, Object> toHostRule = Map.of("ipBlock", Map.of("cidr", hostname + "/32"));
        Map<String, Object> toProxyRule = Map.of("ipBlock", Map.of("cidr", proxyHost + ":" + proxyPort + "/32"));

        spec.put("egress", new Object[]{toHostRule, toProxyRule});
        egressRule.put("spec", spec);

        try (FileWriter writer = new FileWriter(egressFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(options).dump(egressRule, writer);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write egress rule file", e);
        }
    }

    private void updateKubernetesPatchFile(File repoDir, String hostname, String nameOfSecretKey, String nameOfSecretKeyValue, String datasourceType) {
        File patchFile = new File(repoDir, "patch.yaml");

        Map<String, Object> patchData = new HashMap<>();
        patchData.put("apiVersion", "apps/v1");
        patchData.put("kind", "Deployment");
        patchData.put("metadata", Map.of("name", "example-deployment"));

        Map<String, Object> spec = new HashMap<>();
        Map<String, Object> template = new HashMap<>();
        Map<String, Object> specTemplate = new HashMap<>();
        Map<String, Object> containers = new HashMap<>();

        containers.put("name", "example-container");

        if ("database".equalsIgnoreCase(datasourceType)) {
            containers.put("env", new Object[]{Map.of("name", "DB_CONFIG_FILE", "value", "/config/db-config.properties")});
        }

        specTemplate.put("containers", new Object[]{containers});
        template.put("spec", specTemplate);
        spec.put("template", template);
        patchData.put("spec", spec);

        try (FileWriter writer = new FileWriter(patchFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(options).dump(patchData, writer);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write Kubernetes patch file", e);
        }
    }

    private void createDatabasePropertiesFile(File repoDir, String hostname, String proxyHost, int proxyPort) {
        File dbConfigFile = new File(repoDir, "db-config.properties");

        Properties properties = new Properties();
        properties.setProperty("database.host", hostname);
        properties.setProperty("database.proxyHost", proxyHost);
        properties.setProperty("database.proxyPort", String.valueOf(proxyPort));

        try (FileWriter writer = new FileWriter(dbConfigFile)) {
            properties.store(writer, "Database Configuration");
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write database properties file", e);
        }
    }

    private void updateKustomizationFile(File repoDir) {
        File kustomizationFile = new File(repoDir, "kustomization.yaml");

        Map<String, Object> kustomizationData = new HashMap<>();
        kustomizationData.put("resources", new Object[]{"patch.yaml"});
        kustomizationData.put("configMapGenerator", new Object[]{
                Map.of("name", "db-config", "files", new Object[]{"db-config.properties"})
        });

        // Write updated kustomization file
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(kustomizationFile)) {
            yaml.dump(kustomizationData, writer);
        } catch (IOException e) {
            throw new FileProcessingException("Failed to write kustomization file", e);
        }
    }
}