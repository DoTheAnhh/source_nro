package Utils;

/**
 * @Author: ducvupro
 * @YouTube: Nguyen Duc Vu Entertainment
 * @Coppy MaiTienDung
 */

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class RenameAndResizeImages {

    public static void main(String[] args) {
        String dirPathA = "D:\\resizeimage\\a";
        String dirPathB = "D:\\resizeimage\\b";
        String dirPathC = "D:\\resizeimage\\c";
        int n = 15342;

        File directoryA = new File(dirPathA);
        File[] filesA = directoryA.listFiles();

        if (filesA == null) {
            return;
        }

        Arrays.sort(filesA, Comparator.comparingInt(f -> Integer.valueOf(f.getName().replaceAll("\\D+", ""))));

        Map<String, String> fileNames = new HashMap<>();

        for (int i = 0; i < filesA.length; i++) {
            File file = filesA[i];
            String fileName = file.getName();
            String newFileName = (n + i) + ".png";
            Path source = file.toPath();

            String targetDirPath = dirPathB + File.separator + "x4";
            createDirectoryIfNotExists(targetDirPath);

            Path target = new File(targetDirPath, newFileName).toPath();

            try {
                Files.move(source, target);
                resizeImage(target.toString(), dirPathB, (n + i) + "");

                fileNames.put(fileName.replaceAll("\\D+", "") + ".png", newFileName.substring(0, newFileName.lastIndexOf(".")));

                System.out.println("Renamed \"" + fileName + "\" to \"" + newFileName + "\" and copied to directory B.");
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        String jsonFileNames = buildJsonFileNames(fileNames);

        System.out.println("List of unchanged and changed files: " + jsonFileNames);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(dirPathC + "/filename.json"))) {
            out.write(jsonFileNames.getBytes());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String buildJsonFileNames(Map<String, String> fileNames) {
        StringBuilder jsonFileNamesBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> entry : fileNames.entrySet()) {
            jsonFileNamesBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (jsonFileNamesBuilder.length() > 1) {
            jsonFileNamesBuilder.deleteCharAt(jsonFileNamesBuilder.length() - 1);
        }
        jsonFileNamesBuilder.append("}");
        return jsonFileNamesBuilder.toString();
    }

    private static void resizeImage(String path, String dirPathB, String newFileName) {
        try {
            for (int i = 1; i <= 3; i++) {
                File target = new File(path);

                double scaleFactor = (double) i / 4.0;

                BufferedImage inputImage = ImageIO.read(target);
                BufferedImage outputImage = resizeImage(inputImage, scaleFactor);

                String dirPath = dirPathB + File.separator + "x" + i;
                saveImage(outputImage, dirPath, newFileName);

                System.out.println("Resize successful: " + dirPath + File.separator + newFileName);
            }
        } catch (IOException e) {
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Error creating directory: " + dirPath);
        }
    }

    public static BufferedImage resizeImage(BufferedImage image, double scale) {
        if (scale <= 0 || scale > 1) {
            throw new IllegalArgumentException("Scale factor should be between 0 and 1.");
        }

        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);

        if (newWidth < 1) {
            newWidth = 1;
        }
        if (newHeight < 1) {
            newHeight = 1;
        }

        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(scaledImage, 0, 0, null);
        graphics.dispose();

        return resizedImage;
    }

    public static void saveImage(BufferedImage image, String pathFolder, String name) {
        try {
            File folder = new File(pathFolder);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File outputfile = new File(pathFolder + "/" + name + ".png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
        }
    }
}
