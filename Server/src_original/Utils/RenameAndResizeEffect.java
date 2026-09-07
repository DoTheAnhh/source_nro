package Utils;

/**
 * @Author: MaiTienDung
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

public class RenameAndResizeEffect {

    public static void main(String[] args) {
        String dirPathA = "D:\\resizeimage\\a";
        String dirPathB = "D:\\resizeimage\\b";
        String dirPathC = "D:\\resizeimage\\c";
        int n = 47;

        File directoryA = new File(dirPathA);
        File[] filesA = directoryA.listFiles();

        if (filesA == null) {
            System.out.println("Không tìm thấy thư mục đầu vào.");
            return;
        }

        // Sắp xếp file theo số thứ tự trong tên
        Arrays.sort(filesA, Comparator.comparingInt(f -> Integer.parseInt(f.getName().replaceAll("\\D+", ""))));

        Map<String, String> fileNames = new HashMap<>();

        for (int i = 0; i < filesA.length; i++) {
            File file = filesA[i];
            String fileName = file.getName();
            String imgNumber = "ImgEffect_" + (n + i);
            String newFileName = imgNumber + ".png";
            Path source = file.toPath();

            String targetDirPath = dirPathB + File.separator + "x4";
            createDirectoryIfNotExists(targetDirPath);

            Path target = new File(targetDirPath, newFileName).toPath();

            try {
                Files.move(source, target);
                resizeImage(target.toString(), dirPathB, imgNumber);

                fileNames.put(fileName.replaceAll("\\D+", "") + ".png", imgNumber);

                System.out.println("Đã đổi tên \"" + fileName + "\" thành \"" + newFileName + "\" và lưu vào thư mục B.");
            } catch (IOException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }

        String jsonFileNames = buildJsonFileNames(fileNames);

        System.out.println("Danh sách file cũ → mới: " + jsonFileNames);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(dirPathC + "/filename.json"))) {
            out.write(jsonFileNames.getBytes());
        } catch (IOException e) {
            System.out.println("Lỗi ghi file JSON: " + e.getMessage());
        }
    }

    private static String buildJsonFileNames(Map<String, String> fileNames) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        for (Map.Entry<String, String> entry : fileNames.entrySet()) {
            jsonBuilder.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (jsonBuilder.length() > 1) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }

    private static void resizeImage(String imagePath, String dirPathB, String newFileName) {
        try {
            File imageFile = new File(imagePath);
            BufferedImage inputImage = ImageIO.read(imageFile);

            for (int i = 1; i <= 3; i++) {
                double scaleFactor = (double) i / 4.0;
                BufferedImage resizedImage = resizeImage(inputImage, scaleFactor);

                String dirPath = dirPathB + File.separator + "x" + i;
                saveImage(resizedImage, dirPath, newFileName);

                System.out.println("Đã resize thành công: " + dirPath + File.separator + newFileName + ".png");
            }
        } catch (IOException e) {
            System.out.println("Lỗi resize ảnh: " + e.getMessage());
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Lỗi tạo thư mục: " + dirPath);
        }
    }

    public static BufferedImage resizeImage(BufferedImage image, double scale) {
        if (scale <= 0 || scale > 1) {
            throw new IllegalArgumentException("Tỷ lệ scale phải từ 0 đến 1.");
        }

        int newWidth = Math.max(1, (int) (image.getWidth() * scale));
        int newHeight = Math.max(1, (int) (image.getHeight() * scale));

        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(scaledImage, 0, 0, null);
        graphics.dispose();

        return resizedImage;
    }

    public static void saveImage(BufferedImage image, String folderPath, String name) {
        try {
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File outputfile = new File(folderPath + File.separator + name + ".png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            System.out.println("Lỗi lưu ảnh: " + e.getMessage());
        }
    }
}