import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Biên dịch NHANH cho run.bat — chỉ dịch tệp đã đổi và tệp nhắc tới chúng.
 *
 * <p>So từng tệp trong {@code src/} (cỡ + giờ sửa) với danh sách lưu ở
 * {@code out/.danh_sach_tep} của lần build trước:</p>
 * <ul>
 *   <li>không đổi gì → bỏ qua, chạy luôn;</li>
 *   <li>có tệp mới / sửa → dịch các tệp đó CÙNG mọi tệp có nhắc tên lớp của chúng
 *       (đổi chữ ký hàm, hằng số nội tuyến… thì tệp gọi được dịch lại theo);</li>
 *   <li>có tệp bị xoá, chưa có danh sách, hay đổi quá nhiều → xoá out/ dịch lại hết
 *       (tránh .class rác của lớp đã xoá còn nằm trên classpath).</li>
 * </ul>
 * <p>Dịch ngay trong tiến trình này (javax.tools) — khỏi mở thêm một JVM javac.</p>
 *
 * <p>Mã thoát: 0 = xong, 1 = lỗi biên dịch, 3 = công cụ vừa sửa, cần dịch lại chính nó.</p>
 */
public class BuildNhanh {

    static final Path SRC = Paths.get("src");
    static final Path OUT = Paths.get("out");
    static final Path DS = OUT.resolve(".danh_sach_tep");

    public static void main(String[] a) throws Exception {
        // Cong cu tu sua: .java moi hon .class thi bao run.bat dich lai no.
        File nguon = new File("tools/BuildNhanh.java");
        File lop = new File("tools/BuildNhanh.class");
        if (nguon.exists() && lop.exists() && nguon.lastModified() > lop.lastModified()) {
            System.exit(3);
        }
        long bd = System.currentTimeMillis();
        Map<String, String> moi = quet();
        Map<String, String> cu = docDanhSach();
        boolean coLop = Files.exists(OUT.resolve("nro/server/ServerManager.class"));

        List<String> doi = new ArrayList<>();
        boolean coXoa = false;
        if (cu != null) {
            for (Map.Entry<String, String> e : moi.entrySet()) {
                if (!e.getValue().equals(cu.get(e.getKey()))) {
                    doi.add(e.getKey());
                }
            }
            for (String k : cu.keySet()) {
                if (!moi.containsKey(k)) {
                    coXoa = true;
                    break;
                }
            }
        }
        if (cu != null && coLop && !coXoa && doi.isEmpty()) {
            System.out.println("[NHANH] src/ khong doi - bo qua bien dich.");
            return;
        }

        boolean hetCa = cu == null || !coLop || coXoa;
        List<String> dich = new ArrayList<>();
        if (!hetCa) {
            dich = kemTepNhac(doi, moi.keySet());
            if (dich.size() > moi.size() * 45 / 100) {
                hetCa = true;
            }
        }
        if (hetCa) {
            System.out.println("[BUILD] Dich lai toan bo " + moi.size() + " tep"
                    + (coXoa ? " (co tep bi xoa)" : cu == null ? " (lan dau)" : "") + "...");
            xoaThuMuc(OUT.toFile());
            Files.createDirectories(OUT);
            dich = new ArrayList<>(moi.keySet());
        } else {
            System.out.println("[BUILD] Dich " + dich.size() + " tep (" + doi.size() + " tep doi + tep nhac toi chung)...");
        }

        if (!dichTep(dich)) {
            System.out.println("[LOI] Build that bai! Kiem tra loi compile o tren.");
            System.exit(1);
        }
        if (hetCa) {
            chepThuMuc(SRC.resolve("icons"), OUT.resolve("icons"));
            chepThuMuc(SRC.resolve("images"), OUT.resolve("images"));
        }
        ghiDanhSach(moi);
        System.out.println("[OK] Build xong trong " + (System.currentTimeMillis() - bd) / 1000.0 + " giay.");
    }

    /** Mọi tệp .java trong src/: đường dẫn → "cỡ:giờ sửa". */
    static Map<String, String> quet() throws IOException {
        final Map<String, String> m = new TreeMap<>();
        Files.walkFileTree(SRC, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path p, java.nio.file.attribute.BasicFileAttributes at) {
                if (p.toString().endsWith(".java")) {
                    m.put(p.toString().replace('\\', '/'), at.size() + ":" + at.lastModifiedTime().toMillis());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return m;
    }

    static Map<String, String> docDanhSach() {
        if (!Files.exists(DS)) {
            return null;
        }
        try {
            Map<String, String> m = new HashMap<>();
            for (String d : Files.readAllLines(DS, StandardCharsets.UTF_8)) {
                int i = d.lastIndexOf('|');
                if (i > 0) {
                    m.put(d.substring(0, i), d.substring(i + 1));
                }
            }
            return m;
        } catch (IOException e) {
            return null;
        }
    }

    static void ghiDanhSach(Map<String, String> m) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : m.entrySet()) {
            sb.append(e.getKey()).append('|').append(e.getValue()).append('\n');
        }
        Files.write(DS, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    static final Pattern KHAI_BAO = Pattern.compile(
            "^(?:public\\s+|final\\s+|abstract\\s+)*(?:class|interface|enum|@interface)\\s+(\\w+)", Pattern.MULTILINE);

    /** Tệp đổi + mọi tệp có nhắc tên lớp cấp cao nhất khai báo trong tệp đổi. */
    static List<String> kemTepNhac(List<String> doi, Collection<String> tatCa) throws IOException {
        Set<String> ten = new HashSet<>();
        for (String t : doi) {
            String s = new String(Files.readAllBytes(Paths.get(t)), StandardCharsets.UTF_8);
            Matcher mt = KHAI_BAO.matcher(s);
            while (mt.find()) {
                ten.add(mt.group(1));
            }
            String f = Paths.get(t).getFileName().toString();
            ten.add(f.substring(0, f.length() - 5));
        }
        StringBuilder rx = new StringBuilder("\\b(?:");
        boolean dau = true;
        for (String n : ten) {
            rx.append(dau ? "" : "|").append(Pattern.quote(n));
            dau = false;
        }
        rx.append(")\\b");
        Pattern p = Pattern.compile(rx.toString());
        Set<String> kq = new TreeSet<>(doi);
        for (String t : tatCa) {
            if (kq.contains(t)) {
                continue;
            }
            String s = new String(Files.readAllBytes(Paths.get(t)), StandardCharsets.UTF_8);
            if (p.matcher(s).find()) {
                kq.add(t);
            }
        }
        return new ArrayList<>(kq);
    }

    static boolean dichTep(List<String> tep) throws Exception {
        Path ds = Paths.get("sources.txt");
        StringBuilder sb = new StringBuilder();
        for (String t : tep) {
            sb.append('"').append(t).append('"').append('\n');
        }
        Files.write(ds, sb.toString().getBytes(StandardCharsets.UTF_8));
        // Trinh dich trong tien trinh KHONG tu mo rong "lib\*" (chi trinh khoi chay java lam) — liet ke tung jar.
        StringBuilder cpb = new StringBuilder("out");
        File[] jars = new File("lib").listFiles();
        if (jars != null) {
            Arrays.sort(jars);
            for (File j : jars) {
                if (j.getName().toLowerCase().endsWith(".jar")) {
                    cpb.append(File.pathSeparator).append(j.getPath());
                }
            }
        }
        String cp = cpb.toString();
        String[] thamSo = {"-encoding", "UTF-8", "-nowarn", "-Xlint:none", "-g:source,lines",
            "-cp", cp, "-d", "out", "@sources.txt"};
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc != null) {
            return jc.run(null, null, null, thamSo) == 0;
        }
        // Khong co trinh dich trong JVM nay (JRE): goi javac ngoai.
        String javac = System.getenv("JAVAC_EXE");
        List<String> lenh = new ArrayList<>();
        lenh.add(javac != null && !javac.isEmpty() ? javac : "javac");
        lenh.add("-J-Xmx1536m");
        lenh.addAll(Arrays.asList(thamSo));
        Process pr = new ProcessBuilder(lenh).inheritIO().start();
        return pr.waitFor() == 0;
    }

    static void xoaThuMuc(File f) {
        if (!f.exists()) {
            return;
        }
        File[] con = f.listFiles();
        if (con != null) {
            for (File c : con) {
                xoaThuMuc(c);
            }
        }
        f.delete();
    }

    static void chepThuMuc(final Path tu, final Path toi) throws IOException {
        if (!Files.exists(tu)) {
            return;
        }
        Files.walkFileTree(tu, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path d, java.nio.file.attribute.BasicFileAttributes at) throws IOException {
                Files.createDirectories(toi.resolve(tu.relativize(d).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path p, java.nio.file.attribute.BasicFileAttributes at) throws IOException {
                Files.copy(p, toi.resolve(tu.relativize(p).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
