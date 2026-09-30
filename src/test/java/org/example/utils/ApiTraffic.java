package org.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.testdata.TestData;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApiTraffic {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, String> REQUEST_METHODS = new LinkedHashMap<>();
    private static final List<ApiResponse> RESPONSES = new ArrayList<>();
    private static final List<EndpointCheck> REQUIRED_ENDPOINTS = List.of(
            new EndpointCheck("POST", "/login"),
            new EndpointCheck("GET", "/profile"),
            new EndpointCheck("PUT", "/profile"),
            new EndpointCheck("POST", "/profile/image"));

    private ApiTraffic() {
    }

    public static void collectBrowserResponses(WebDriver driver) throws IOException {
        for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE)) {
            JsonNode message = JSON.readTree(entry.getMessage()).path("message");
            JsonNode params = message.path("params");
            String method = message.path("method").asText();
            String requestId = params.path("requestId").asText();

            if ("Network.requestWillBeSent".equals(method)) {
                JsonNode request = params.path("request");
                REQUEST_METHODS.put(requestId, request.path("method").asText("GET"));
            } else if ("Network.responseReceived".equals(method)) {
                JsonNode response = params.path("response");
                record(REQUEST_METHODS.getOrDefault(requestId, "GET"),
                        response.path("url").asText(),
                        response.path("status").asInt());
            }
        }
    }

    public static void record(String method, String url, int status) {
        URI uri = URI.create(url);
        URI apiBaseUri = URI.create(TestData.apiBaseUrl());
        if (!apiBaseUri.getHost().equalsIgnoreCase(uri.getHost())) {
            return;
        }
        String endpoint = uri.getPath();
        String basePath = apiBaseUri.getPath() == null ? "" : apiBaseUri.getPath().replaceAll("/+$", "");
        if (endpoint == null || !endpoint.startsWith(basePath + "/")) {
            return;
        }
        String relativeEndpoint = endpoint.substring(basePath.length());
        boolean isRequiredEndpoint = REQUIRED_ENDPOINTS.stream()
                .anyMatch(check -> check.method().equals(method)
                        && relativeEndpoint.equals(check.path()));
        if (!isRequiredEndpoint) {
            return;
        }
        ApiResponse response = new ApiResponse(method, endpoint, status);
        if (!RESPONSES.contains(response)) {
            RESPONSES.add(response);
        }
    }

    public static void assertRequiredEndpoints() {
        for (EndpointCheck endpoint : REQUIRED_ENDPOINTS) {
            assertEndpoint(endpoint.method(), endpoint.path());
        }
        for (ApiResponse response : RESPONSES) {
            if (response.status() < 200 || response.status() >= 300) {
                throw new AssertionError(response.method() + " " + response.endpoint()
                        + " returned HTTP " + response.status());
            }
        }
    }

    private static void assertEndpoint(String method, String suffix) {
        List<ApiResponse> matching = RESPONSES.stream()
                .filter(response -> response.method().equals(method)
                        && response.endpoint().endsWith(suffix))
                .toList();
        if (matching.isEmpty()) {
            throw new AssertionError("No response was captured for " + method + " " + suffix);
        }
    }

    public static void writeReport() throws IOException {
        Path report = Path.of("target", "cucumber-reports", "api-endpoints.txt");
        Files.createDirectories(report.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("API response checks for the profile-picture workflow");
        lines.add("Base URL: " + TestData.apiBaseUrl());
        lines.add("");
        if (RESPONSES.isEmpty()) {
            lines.add("No matching API responses were captured.");
        } else {
            RESPONSES.forEach(response -> lines.add(
                    response.method() + " " + response.endpoint() + " -> HTTP " + response.status()));
        }
        Files.write(report, lines);
    }

    public static void reset() {
        REQUEST_METHODS.clear();
        RESPONSES.clear();
    }

    private record ApiResponse(String method, String endpoint, int status) {
    }

    private record EndpointCheck(String method, String path) {
    }
}
