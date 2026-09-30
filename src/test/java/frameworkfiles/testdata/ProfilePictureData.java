package frameworkfiles.testdata;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ProfilePictureData {
    private static final int MAX_DIMENSION = 512;
    private static final float JPEG_QUALITY = 0.72f;
    private static final Path OPTIMIZED_IMAGE = Path.of(
            "target", "test-data", "profile-picture.jpg");

    private ProfilePictureData() {
    }

    public static Path prepare(Path source) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null) {
            throw new IOException("Profile picture is not a supported image: " + source);
        }

        double scale = Math.min(1.0, MAX_DIMENSION / (double) Math.max(original.getWidth(), original.getHeight()));
        int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(original, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
            original.flush();
        }

        Files.createDirectories(OPTIMIZED_IMAGE.getParent());
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ImageOutputStream output = ImageIO.createImageOutputStream(OPTIMIZED_IMAGE.toFile())) {
            writer.setOutput(output);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(JPEG_QUALITY);
            writer.write(null, new IIOImage(resized, null, null), parameters);
        } finally {
            writer.dispose();
            resized.flush();
        }
        return OPTIMIZED_IMAGE;
    }
}
