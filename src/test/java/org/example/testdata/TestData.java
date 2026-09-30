package org.example.testdata;

import java.nio.file.Files;
import java.nio.file.Path;

public final class TestData {
    private TestData() {
    }

    public static String siteUrl() {
        return System.getProperty("site.url",
                environmentOrDefault("SITE_URL", "https://ndosisimplifiedautomation.vercel.app/"));
    }

    public static String apiBaseUrl() {
        return environmentOrDefault("API_BASE_URL", "https://www.ndosiautomation.co.za/APIDEV");
    }

    public static String username() {
        return environmentOrDefault("SITE_USERNAME", "laylayt@gmail.com");
    }

    public static String password() {
        return requiredEnvironmentVariable("SITE_PASSWORD");
    }

    public static Path profilePicture() throws java.io.IOException {
        String configuredPath = environmentOrDefault(
                "PROFILE_PICTURE_PATH", "src/test/resources/test-data/profile-picture.jpg");
        Path image = Path.of(configuredPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(image)) {
            throw new IllegalStateException("Set PROFILE_PICTURE_PATH to the supplied profile image; "
                    + "file not found: " + image);
        }
        return ProfilePictureData.prepare(image);
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Set the " + name + " environment variable to run this scenario.");
        }
        return value;
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
