package frameworkfiles.testdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

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
        String password = System.getenv("SITE_PASSWORD");
        if (password != null && !password.isBlank()) {
            return password;
        }
        password = localPassword();
        if (password != null) {
            return password;
        }
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException(
                    "Set SITE_PASSWORD or add it to local.credentials.json to run this scenario.");
        }

        JPasswordField passwordField = new JPasswordField(24);
        JPanel prompt = new JPanel();
        prompt.add(passwordField);
        int result = JOptionPane.showConfirmDialog(null, prompt,
                "Password for " + username(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            throw new IllegalStateException("Password entry was cancelled.");
        }
        char[] characters = passwordField.getPassword();
        try {
            if (characters.length == 0) {
                throw new IllegalStateException("Password cannot be empty.");
            }
            return new String(characters);
        } finally {
            java.util.Arrays.fill(characters, '\0');
            passwordField.setText("");
        }
    }

    public static Path profilePicture() throws java.io.IOException {
        String configuredPath = environmentOrDefault("PROFILE_PICTURE_PATH",
                Path.of(System.getProperty("user.home"), "Downloads",
                        "DC9EBAC7-2F02-4445-944D-C336462CD73A.JPG").toString());
        Path image = Path.of(configuredPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(image)) {
            throw new IllegalStateException("Set PROFILE_PICTURE_PATH or place the profile image at "
                    + image + "; file not found.");
        }
        return ProfilePictureData.prepare(image);
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String localPassword() {
        Path credentialsFile = Path.of("local.credentials.json");
        if (!Files.isRegularFile(credentialsFile)) {
            return null;
        }
        try {
            JsonNode passwordNode = new ObjectMapper()
                    .readTree(credentialsFile.toFile())
                    .path("SITE_PASSWORD");
            if (!passwordNode.isTextual() || passwordNode.asText().isBlank()) {
                throw new IllegalStateException(
                        "SITE_PASSWORD is missing or empty in " + credentialsFile.toAbsolutePath());
            }
            return passwordNode.asText();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not read " + credentialsFile.toAbsolutePath(), exception);
        }
    }
}
