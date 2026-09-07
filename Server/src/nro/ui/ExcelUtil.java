package nro.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Đọc và ghi tệp {@code .xlsx} ở mức tối giản, không cần thư viện ngoài.
 *
 * <p><b>Vì sao tự viết.</b> Thư mục {@code lib/} không có Apache POI, mà kéo POI
 * về là thêm khoảng 15 MB jar cùng một chuỗi phụ thuộc, chỉ để xuất/nhập vài
 * bảng cấu hình. Định dạng xlsx thực chất là một tệp ZIP chứa vài tệp XML — đủ
 * đơn giản để viết tay phần mình cần.</p>
 *
 * <p><b>Phạm vi.</b> Ô chữ, tiêu đề tô nền, bề rộng cột, và khoá dòng tiêu đề.
 * Không công thức, không kiểu số — mọi ô là chuỗi. Chủ ý: Excel hay tự đoán kiểu
 * rồi biến "01" thành 1 hoặc đổi chuỗi có dấu chấm thành ngày tháng.</p>
 *
 * <p><b>Đọc thì phải hiểu cả hai cách Excel lưu chuỗi.</b> Tệp lớp này ghi ra
 * dùng chuỗi nội tuyến ({@code inlineStr}). Nhưng khi người dùng mở bằng Excel
 * rồi bấm Lưu, Excel viết lại toàn bộ theo bảng chuỗi dùng chung
 * ({@code sharedStrings.xml}). Chỉ đọc một kiểu thì nhập lại đúng tệp vừa sửa sẽ
 * ra bảng rỗng.</p>
 */
public final class ExcelUtil {

    private ExcelUtil() {
    }

    /** Một trang tính: tên, các dòng, và bề rộng cột. */
    public static final class Sheet {

        public final String ten;
        public final List<List<String>> dong = new ArrayList<>();
        /** Bề rộng từng cột, tính theo số ký tự. Rỗng = để Excel tự lo. */
        public int[] rongCot = new int[0];
        /** Số dòng đầu được tô như tiêu đề. */
        public int soDongTieuDe = 1;

        public Sheet(String ten) {
            this.ten = ten;
        }

        public Sheet rong(int... r) {
            this.rongCot = r;
            return this;
        }

        public Sheet them(Object... o) {
            List<String> d = new ArrayList<>(o.length);
            for (Object x : o) {
                d.add(x == null ? "" : String.valueOf(x));
            }
            dong.add(d);
            return this;
        }
    }

    // =================================================================== GHI

    public static void ghi(File tep, List<Sheet> sheets) throws Exception {
        if (sheets == null || sheets.isEmpty()) {
            throw new IllegalArgumentException("Không có trang tính nào để ghi");
        }
        try (ZipOutputStream z = new ZipOutputStream(new FileOutputStream(tep))) {
            them(z, "[Content_Types].xml", contentTypes(sheets.size()));
            them(z, "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            them(z, "xl/workbook.xml", workbook(sheets));
            them(z, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            them(z, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++) {
                them(z, "xl/worksheets/sheet" + (i + 1) + ".xml",
                        sheetXml(sheets.get(i)));
            }
        }
    }

    private static void them(ZipOutputStream z, String ten, String noiDung)
            throws Exception {
        z.putNextEntry(new ZipEntry(ten));
        z.write(noiDung.getBytes(StandardCharsets.UTF_8));
        z.closeEntry();
    }

    private static String contentTypes(int soSheet) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
                .append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                .append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                .append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
                .append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= soSheet; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return sb.append("</Types>").toString();
    }

    /**
     * Bảng kiểu: 0 thường, 1 tiêu đề (chữ trắng đậm trên nền xanh đậm),
     * 2 tiêu đề phụ (chữ đậm trên nền vàng nhạt).
     */
    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<fonts count=\"3\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><color rgb=\"FFFFFFFF\"/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><color rgb=\"FF5A3A10\"/><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"4\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E6F9E\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFCE8C0\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"2\"><border/>"
                + "<border><left style=\"thin\"><color rgb=\"FFBFBFBF\"/></left>"
                + "<right style=\"thin\"><color rgb=\"FFBFBFBF\"/></right>"
                + "<top style=\"thin\"><color rgb=\"FFBFBFBF\"/></top>"
                + "<bottom style=\"thin\"><color rgb=\"FFBFBFBF\"/></bottom></border>"
                + "</borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"3\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\">"
                + "<alignment vertical=\"center\" wrapText=\"1\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\">"
                + "<alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\">"
                + "<alignment vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "</styleSheet>";
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
                .append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<sheet name=\"").append(thoat(sheets.get(i).ten))
                    .append("\" sheetId=\"").append(i + 1)
                    .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return sb.append("</sheets></workbook>").toString();
    }

    private static String workbookRels(int soSheet) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= soSheet; i++) {
            sb.append("<Relationship Id=\"rId").append(i)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
                    .append(i).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(soSheet + 1)
                .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        return sb.append("</Relationships>").toString();
    }

    private static String sheetXml(Sheet s) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");

        // Khoa dong tieu de: cuon xuong van thay ten cot.
        if (s.soDongTieuDe > 0) {
            sb.append("<sheetViews><sheetView workbookViewId=\"0\">")
                    .append("<pane ySplit=\"").append(s.soDongTieuDe)
                    .append("\" topLeftCell=\"A").append(s.soDongTieuDe + 1)
                    .append("\" activePane=\"bottomLeft\" state=\"frozen\"/>")
                    .append("</sheetView></sheetViews>");
        }
        if (s.rongCot != null && s.rongCot.length > 0) {
            sb.append("<cols>");
            for (int i = 0; i < s.rongCot.length; i++) {
                sb.append("<col min=\"").append(i + 1).append("\" max=\"")
                        .append(i + 1).append("\" width=\"").append(s.rongCot[i])
                        .append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols>");
        }
        sb.append("<sheetData>");
        for (int r = 0; r < s.dong.size(); r++) {
            List<String> d = s.dong.get(r);
            int kieu = (r < s.soDongTieuDe) ? 1 : (laDongMuc(d) ? 2 : 0);
            sb.append("<row r=\"").append(r + 1).append("\"")
                    .append(kieu == 1 ? " ht=\"22\" customHeight=\"1\"" : "")
                    .append(">");
            for (int c = 0; c < d.size(); c++) {
                sb.append("<c r=\"").append(cot(c)).append(r + 1)
                        .append("\" s=\"").append(kieu)
                        .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                        .append(thoat(d.get(c)))
                        .append("</t></is></c>");
            }
            sb.append("</row>");
        }
        return sb.append("</sheetData></worksheet>").toString();
    }

    /** Dòng mở đầu một mục trong trang tra cứu, dạng {@code == TÊN MỤC ==}. */
    private static boolean laDongMuc(List<String> d) {
        return !d.isEmpty() && d.get(0) != null && d.get(0).startsWith("==");
    }

    /** Tên cột kiểu A, B, ... Z, AA, AB... */
    private static String cot(int i) {
        StringBuilder sb = new StringBuilder();
        int n = i;
        while (true) {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = n / 26 - 1;
            if (n < 0) {
                break;
            }
        }
        return sb.toString();
    }

    private static String thoat(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // =================================================================== DOC

    private static final Pattern P_O =
            Pattern.compile("<c\\b([^>]*)>(.*?)</c>|<c\\b([^>]*)/>",
                    Pattern.DOTALL);
    private static final Pattern P_DONG =
            Pattern.compile("<row\\b[^>]*>(.*?)</row>", Pattern.DOTALL);
    private static final Pattern P_T =
            Pattern.compile("<t[^>]*>(.*?)</t>", Pattern.DOTALL);
    private static final Pattern P_V =
            Pattern.compile("<v>(.*?)</v>", Pattern.DOTALL);
    private static final Pattern P_SI =
            Pattern.compile("<si\\b[^>]*>(.*?)</si>", Pattern.DOTALL);

    /** Đọc trang tính thứ {@code chiSo} (đếm từ 0) về danh sách dòng. */
    public static List<List<String>> doc(File tep, int chiSo) throws Exception {
        List<List<String>> ketQua = new ArrayList<>();
        try (ZipFile zip = new ZipFile(tep)) {
            List<String> chungs = docChuoiDungChung(zip);
            ZipEntry e = zip.getEntry("xl/worksheets/sheet" + (chiSo + 1) + ".xml");
            if (e == null) {
                return ketQua;
            }
            String xml = docHet(zip.getInputStream(e));
            Matcher md = P_DONG.matcher(xml);
            while (md.find()) {
                String thanDong = md.group(1);
                List<String> dong = new ArrayList<>();
                Matcher mo = P_O.matcher(thanDong);
                while (mo.find()) {
                    String thuocTinh = mo.group(1) != null ? mo.group(1) : mo.group(3);
                    String than = mo.group(2) != null ? mo.group(2) : "";
                    dong.add(giaTriO(thuocTinh, than, chungs));
                }
                ketQua.add(dong);
            }
        }
        return ketQua;
    }

    private static String giaTriO(String thuocTinh, String than, List<String> chungs) {
        boolean laChungs = thuocTinh != null && thuocTinh.contains("t=\"s\"");
        if (laChungs) {
            Matcher mv = P_V.matcher(than);
            if (mv.find()) {
                try {
                    int i = Integer.parseInt(mv.group(1).trim());
                    if (i >= 0 && i < chungs.size()) {
                        return chungs.get(i);
                    }
                } catch (NumberFormatException boQua) {
                    // O hong -> tra chuoi rong, khong lam sap ca lan nhap.
                }
            }
            return "";
        }
        Matcher mt = P_T.matcher(than);
        if (mt.find()) {
            return boThoat(mt.group(1));
        }
        Matcher mv = P_V.matcher(than);
        if (mv.find()) {
            return boThoat(mv.group(1));
        }
        return "";
    }

    private static List<String> docChuoiDungChung(ZipFile zip) throws Exception {
        List<String> ds = new ArrayList<>();
        ZipEntry e = zip.getEntry("xl/sharedStrings.xml");
        if (e == null) {
            return ds;
        }
        String xml = docHet(zip.getInputStream(e));
        Matcher m = P_SI.matcher(xml);
        while (m.find()) {
            // Mot <si> co the gom nhieu <t> khi Excel chia chuoi theo dinh dang.
            StringBuilder sb = new StringBuilder();
            Matcher mt = P_T.matcher(m.group(1));
            while (mt.find()) {
                sb.append(mt.group(1));
            }
            ds.add(boThoat(sb.toString()));
        }
        return ds;
    }

    private static String docHet(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) > 0) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String boThoat(String s) {
        return s.replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&");
    }
}
