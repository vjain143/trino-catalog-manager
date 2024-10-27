package com.example.gitservice.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class OnboardingInput {

    @NotNull(message = "Jira ticket is required")
    @Size(min = 1, message = "Jira ticket cannot be empty")
    private String jiraTicket;

    @NotNull(message = "Content is required")
    @Size(min = 1, message = "Content cannot be empty")
    private String content;

    @NotNull(message = "Hostname is required")
    @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid hostname format")
    private String hostname;

    @NotNull(message = "Proxy host is required")
    @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid proxy host format")
    private String proxyHost;

    @NotNull(message = "Proxy port is required")
    private Integer proxyPort;

    @NotNull(message = "Name of secret key is required")
    @Size(min = 1, message = "Name of secret key cannot be empty")
    private String nameOfSecretKey;

    @NotNull(message = "Value of secret key is required")
    @Size(min = 1, message = "Value of secret key cannot be empty")
    private String nameOfSecretKeyValue;

    @NotNull(message = "Datasource type is required")
    @Pattern(regexp = "^(api-internal|api-external|non-api|database)$", message = "Datasource type must be one of: api-internal, api-external, non-api, database")
    private String datasourceType;

    public @NotNull(message = "Jira ticket is required") @Size(min = 1, message = "Jira ticket cannot be empty") String getJiraTicket() {
        return jiraTicket;
    }

    public void setJiraTicket(@NotNull(message = "Jira ticket is required") @Size(min = 1, message = "Jira ticket cannot be empty") String jiraTicket) {
        this.jiraTicket = jiraTicket;
    }

    public @NotNull(message = "Content is required") @Size(min = 1, message = "Content cannot be empty") String getContent() {
        return content;
    }

    public void setContent(@NotNull(message = "Content is required") @Size(min = 1, message = "Content cannot be empty") String content) {
        this.content = content;
    }

    public @NotNull(message = "Hostname is required") @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid hostname format") String getHostname() {
        return hostname;
    }

    public void setHostname(@NotNull(message = "Hostname is required") @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid hostname format") String hostname) {
        this.hostname = hostname;
    }

    public @NotNull(message = "Proxy host is required") @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid proxy host format") String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(@NotNull(message = "Proxy host is required") @Pattern(regexp = "^(?!-)[A-Za-z0-9.-]{1,63}(?<!-)$", message = "Invalid proxy host format") String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public @NotNull(message = "Proxy port is required") Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(@NotNull(message = "Proxy port is required") Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public @NotNull(message = "Name of secret key is required") @Size(min = 1, message = "Name of secret key cannot be empty") String getNameOfSecretKey() {
        return nameOfSecretKey;
    }

    public void setNameOfSecretKey(@NotNull(message = "Name of secret key is required") @Size(min = 1, message = "Name of secret key cannot be empty") String nameOfSecretKey) {
        this.nameOfSecretKey = nameOfSecretKey;
    }

    public @NotNull(message = "Value of secret key is required") @Size(min = 1, message = "Value of secret key cannot be empty") String getNameOfSecretKeyValue() {
        return nameOfSecretKeyValue;
    }

    public void setNameOfSecretKeyValue(@NotNull(message = "Value of secret key is required") @Size(min = 1, message = "Value of secret key cannot be empty") String nameOfSecretKeyValue) {
        this.nameOfSecretKeyValue = nameOfSecretKeyValue;
    }

    public @NotNull(message = "Datasource type is required") @Pattern(regexp = "^(api-internal|api-external|non-api|database)$", message = "Datasource type must be one of: api-internal, api-external, non-api, database") String getDatasourceType() {
        return datasourceType;
    }

    public void setDatasourceType(@NotNull(message = "Datasource type is required") @Pattern(regexp = "^(api-internal|api-external|non-api|database)$", message = "Datasource type must be one of: api-internal, api-external, non-api, database") String datasourceType) {
        this.datasourceType = datasourceType;
    }
}