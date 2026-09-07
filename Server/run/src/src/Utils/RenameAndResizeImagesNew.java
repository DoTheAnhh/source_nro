package Utils;

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
import java.util.*;
import javax.imageio.ImageIO;

public class RenameAndResizeImagesNew {

    public static void main(String[] args) {
        String dirPathA = "D:\\resizeimage\\a"; // thư mục ảnh gốc
        String dirPathB = "D:\\resizeimage\\b"; // thư mục lưu ảnh resized
        String dirPathC = "D:\\resizeimage\\c"; // thư mục lưu json
        int n = 19248; // bắt đầu đánh số từ

        File directoryA = new File(dirPathA);
        File[] filesA = directoryA.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") ||
                name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".jpeg"));

        if (filesA == null || filesA.length == 0) {
            System.out.println("❌ Không tìm thấy ảnh trong thư mục: " + dirPathA);
            return;
        }

        // Sắp xếp: số đầu tên trước, chữ sau
        Arrays.sort(filesA, Comparator.comparing((File f) -> {
            String name = f.getName().toLowerCase();
            boolean startsWithDigit = Character.isDigit(name.charAt(0));
            if (startsWithDigit) {
                String numPart = name.replaceAll("^(\\d+).*", "$1");
                return String.format("0_%010d", Integer.parseInt(numPart));
            } else {
                return "1_" + name;
            }
        }));

        Map<String, String> fileNames = new LinkedHashMap<>();

        for (int i = 0; i < filesA.length; i++) {
            File file = filesA[i];
            String fileName = file.getName();
            String newFileName = (n + i) + ".png";
            Path source = file.toPath();

            String targetDirPath = dirPathB + File.separator + "x4";
            createDirectoryIfNotExists(targetDirPath);
            Path target = new File(targetDirPath, newFileName).toPath();

            try {
                // Đọc thử ảnh để chắc chắn ảnh hợp lệ
                BufferedImage testImage = ImageIO.read(file);
                if (testImage == null) {
                    System.out.println("⚠️ Bỏ qua ảnh lỗi hoặc không đọc được: " + fileName);
                    continue;
                }

                Files.move(source, target); // chuyển ảnh gốc vào thư mục x4
                resizeImage(target.toString(), dirPathB, String.valueOf(n + i)); // resize sang x1 x2 x3

                fileNames.put(fileName, String.valueOf(n + i)); // lưu ánh xạ

                System.out.println("✅ Đổi \"" + fileName + "\" thành \"" + newFileName + "\" và resize.");
            } catch (IOException e) {
                System.out.println("❌ Lỗi xử lý ảnh \"" + fileName + "\": " + e.getMessage());
            }
        }

        String jsonFileNames = buildJsonFileNames(fileNames);
        System.out.println("📝 File ánh xạ: " + jsonFileNames);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(dirPathC + "/filename.json"))) {
            out.write(jsonFileNames.getBytes());
        } catch (IOException e) {
            System.out.println("❌ Ghi file JSON thất bại: " + e.getMessage());
        }
    }

    private static String buildJsonFileNames(Map<String, String> fileNames) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : fileNames.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1); // xóa dấu phẩy cuối
        }
        json.append("}");
        return json.toString();
    }

    private static void resizeImage(String path, String dirPathB, String newFileName) {
        try {
            File original = new File(path);
            BufferedImage inputImage = ImageIO.read(original);

            if (inputImage == null) {
                System.out.println("⚠️ File không hợp lệ hoặc không đọc được: " + path);
                return;
            }

            for (int i = 1; i <= 3; i++) {
                double scaleFactor = (double) i / 4.0;
                BufferedImage outputImage = resizeImage(inputImage, scaleFactor);

                String dirPath = dirPathB + File.separator + "x" + i;
                saveImage(outputImage, dirPath, newFileName);

                System.out.println("   - Resize x" + i + " thành công: " + dirPath + File.separator + newFileName);
            }
        } catch (IOException e) {
            System.out.println("❌ Resize lỗi: " + e.getMessage());
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("❌ Không tạo được thư mục: " + dirPath);
        }
    }

    public static BufferedImage resizeImage(BufferedImage image, double scale) {
        int newWidth = Math.max(1, (int) (image.getWidth() * scale));
        int newHeight = Math.max(1, (int) (image.getHeight() * scale));

        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resized;
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
            System.out.println("❌ Lỗi lưu ảnh: " + e.getMessage());
        }
    }
}
