package Utils;

/**
 * @Author: ducvupro
 * @YouTube: Nguyen Duc Vu Entertainment
 * @Coppy Văn Khải
 */

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class DVP_Part {

    public static int idpart = 1900; //id part

    public static void main(String[] args) {

        String dirPathC = "D:\\resizeimage\\c";
        String filePart = "D:\\resizeimage\\NR_part";

        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(readFile(filePart)))) {
            int size = dis.readShort();
            Part[] parts = new Part[size];
            for (int i = 0; i < parts.length; i++) {
                byte type = dis.readByte();
                parts[i] = new Part(type);
                for (int j = 0; j < parts[i].pi.length; j++) {
                    parts[i].pi[j] = new PartImage(dis.readShort(), dis.readByte(), dis.readByte());
                }
                try (DataInputStream in = new DataInputStream(new FileInputStream(dirPathC + "/filename.json"))) {
                    byte[] data = new byte[in.available()];
                    in.readFully(data);
                    String json = new String(data, "UTF-8");
                    printPart(parts[i], json, i, type);
                } catch (IOException e) {
                    System.out.println("Lỗi: " + e.getMessage());
                }
            }
        } catch (Exception e) {
        }
    }

    public static void printPart(Part part, String json, int... is) {
        try {
            boolean check = false;
            int id;
            StringBuilder data = new StringBuilder();
            for (PartImage pi : part.pi) {
                try {
                    id = Integer.parseInt(new JSONObject(json).getString(pi.id + ".png"));
                    check = true;
                } catch (NumberFormatException | JSONException e) {
                    id = pi.id;
                }

                data.append("[").append(id).append(",").append(pi.dx).append(",").append(pi.dy).append("],");
            }
            data.deleteCharAt(data.length() - 1);
            if (check) {
                System.out.println("INSERT INTO `part`(`id`, `TYPE`, `DATA`) VALUES ");
                System.out.println("('" + (idpart++) + "','" + is[1] + "','" + "[" + data.toString() + "]" + "')" + ";");
            }
        } catch (Exception e) {
        }
    }

    public static byte[] readFile(String url) throws IOException {
        File file = new File(url);
        byte[] ab = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(ab);
        }
        return ab;
    }

    public static void writeFile(String url, byte[] ab) throws IOException {
        File f = new File(url);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
        try (FileOutputStream fos = new FileOutputStream(url)) {
            fos.write(ab);
        }
    }

    public static class PartImage {

        public int id;
        public byte dx;
        public byte dy;

        public PartImage(int id, byte dx, byte dy) {
            this.id = id;
            this.dx = dx;
            this.dy = dy;
        }
    }

    public static class Part {

        public int type;

        public PartImage[] pi;

        public Part(int type) {
            this.type = type;
            if (type == 0) {
                pi = new PartImage[3];
            }
            if (type == 1) {
                pi = new PartImage[17];
            }
            if (type == 2) {
                pi = new PartImage[14];
            }
            if (type == 3) {
                pi = new PartImage[2];
            }
        }
    }
}
