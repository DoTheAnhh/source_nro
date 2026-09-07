package nro.tool;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import javax.imageio.ImageIO;

/**
 * Biến một tấm ảnh thường thành icon vật phẩm đủ bốn mức phóng.
 *
 * <h2>Vì sao không chỉ chép tệp png vào là xong</h2>
 *
 * <p>Client giữ ảnh trong mảng {@code SmallImage.imgNew} và <b>đối chiếu bản
 * lưu trong máy với một bảng phiên bản</b> do máy chủ gửi lúc đăng nhập
 * (gói {@code -77}, đọc từ {@code data/smallimage_version/x*}). Luật đối chiếu
 * nằm ở {@code SmallImage.createImage}:</p>
 *
 * <pre>  array.Length % 127 != newSmallVersion[id]  thì tải lại</pre>
 *
 * <p>Nghĩa là byte thứ {@code 2 + id} của tệp phiên bản <b>phải</b> bằng
 * {@code cỡ tệp png % 127}, và cỡ tệp mỗi mức phóng một khác nên phải tính
 * riêng bốn lần. Chép png vào mà quên sửa bảng thì ảnh vẫn hiện, nhưng client
 * coi bản lưu là hỏng và tải lại icon đó <b>mỗi lần vào game</b>.</p>
 *
 * <h2>Giới hạn id</h2>
 *
 * <p>Client đọc cỡ mảng bằng {@code readShort} rồi cấp phát đúng chừng đó ô,
 * mà tệp phiên bản dài 32769 byte = 2 byte đầu cộng 32767 ô. Vậy id hợp lệ là
 * <b>0 đến 32766</b>, và dải đó đã dùng tới sát trần. Icon mới phải nhét vào
 * chỗ trống ở giữa; chạy với {@code --trong} để xem còn chỗ nào.</p>
 *
 * <h2>Dùng</h2>
 *
 * <pre>
 *   java -cp out nro.tool.ThemIcon --trong
 *   java -cp out nro.tool.ThemIcon anh.png
 *   java -cp out nro.tool.ThemIcon anh.png --id 25300 --rong 30 --cao 21
 * </pre>
 *
 * <p>Chạy từ thư mục {@code Server} để {@code data/...} trỏ đúng chỗ.</p>
 */
public final class ThemIcon {

    /** Ô id cuối cùng client cấp phát được. */
    private static final int ID_TOI_DA = 32766;

    /**
     * Khung icon ở mức x1, lấy theo đúng ô hành trang của client.
     *
     * <p>Ô vẽ vật phẩm trong hành trang rộng <b>34</b> và cao <b>23</b> (xem
     * {@code Panel.cs}: {@code num7 = 34}, {@code num8 = ITEM_HEIGHT - 1}).
     * Chừa mỗi bên vài điểm ảnh cho khỏi chạm mép, còn 30x21 — khoảng 90% ô.</p>
     *
     * <p>Trước đây dùng một hộp vuông 24 nên ảnh ngang bẹt như quả chanh co lại
     * còn 24x13, chỉ chiếm hơn nửa chiều cao ô và nhìn bé tí.</p>
     */
    private static final int RONG_MAC_DINH = 30;
    private static final int CAO_MAC_DINH = 21;

    /**
     * Thư mục {@code Server}, tự dò chứ không ghi cứng.
     *
     * <p>Ảnh <b>luôn</b> phải rơi vào {@code Server/data/icon} dù gọi công cụ
     * từ đâu. Chỉ viết {@code "data/icon"} là không đủ: đường dẫn tương đối nở
     * ra theo thư mục hiện hành, chạy nhầm chỗ thì tệp bay đi nơi khác, hoặc
     * tệ hơn là tạo ra một cây {@code data/data/...} rỗng. Mà ghi cứng ổ F thì
     * gửi cả thư mục sang máy khác là hỏng.</p>
     */
    private static final File GOC = timGoc();

    private ThemIcon() {
    }

    /**
     * Tìm thư mục chứa {@code data/icon}, thử lần lượt ba đường.
     *
     * <p>Ưu tiên vị trí tệp {@code .class} đang chạy — đó là chỗ chắc chắn
     * nhất, vì {@code out/} nằm ngay trong {@code Server}. Không thấy thì lần
     * ngược từ thư mục hiện hành. Vẫn không thấy thì đành lấy thư mục hiện
     * hành và để phần sau báo lỗi tử tế.</p>
     */
    private static File timGoc() {
        String chiDinh = System.getProperty("nro.goc");
        if (chiDinh != null && coDuLieu(new File(chiDinh))) {
            return new File(chiDinh);
        }
        try {
            File noiChayMa = new File(ThemIcon.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File tim = lanNguoc(noiChayMa);
            if (tim != null) {
                return tim;
            }
        } catch (Exception boQua) {
            // Vai kieu nap lop khong cho biet minh nam o dau. Con duong duoi.
        }
        File tim = lanNguoc(new File(".").getAbsoluteFile());
        return tim != null ? tim : new File(".").getAbsoluteFile();
    }

    /** Đi ngược lên cây thư mục cho tới khi gặp thư mục có {@code data/icon}. */
    private static File lanNguoc(File tu) {
        for (File d = tu; d != null; d = d.getParentFile()) {
            if (coDuLieu(d)) {
                return d;
            }
        }
        return null;
    }

    private static boolean coDuLieu(File d) {
        return new File(d, "data/icon").isDirectory()
                && new File(d, "data/smallimage_version").isDirectory();
    }

    static File tepIcon(int id, int zoom) {
        return new File(GOC, "data/icon/x" + zoom + "/" + id + ".png");
    }

    /** Thư mục {@code Server} đã dò được, để panel dùng chung một gốc. */
    public static File goc() {
        return GOC;
    }

    /** {@code true} nếu id này chưa có ảnh ở mức phóng nào. */
    public static boolean conTrong(int id) {
        return idConTrong(id);
    }

    /** Id nhỏ nhất chưa có ảnh, hoặc {@code -1} nếu hết chỗ. */
    public static int idTrongDauTien() {
        return timIdTrong();
    }

    /** Mức phóng lớn nhất, để nơi khác khỏi đoán. */
    public static int soMucPhong() {
        return 4;
    }

    /** Id lớn nhất client cấp phát được. */
    public static int idToiDa() {
        return ID_TOI_DA;
    }

    /**
     * Tập id đang có ảnh, đọc bằng <b>một</b> lần liệt kê thư mục.
     *
     * <p>Hỏi {@code File.isFile()} cho từng id là hơn ba vạn lượt chạm đĩa,
     * nhân bốn mức phóng nữa; gọi trong luồng giao diện thì panel đứng hình
     * vài giây. Liệt kê một lần rồi lọc trong bộ nhớ thì tức thì.</p>
     *
     * <p>Chỉ soi mức x2 — đó là mức panel dùng để vẽ, và bốn mức luôn được ghi
     * cùng lúc nên có cái này là có cả bốn.</p>
     */
    public static java.util.Set<Integer> idDaCoAnh() {
        java.util.Set<Integer> ra = new java.util.HashSet<>();
        String[] ten = new File(GOC, "data/icon/x2").list();
        if (ten == null) {
            return ra;
        }
        for (String t : ten) {
            if (!t.endsWith(".png")) {
                continue;
            }
            try {
                ra.add(Integer.parseInt(t.substring(0, t.length() - 4)));
            } catch (NumberFormatException boQua) {
                // Tep la trong thu muc anh thi khong phai icon, bo qua.
            }
        }
        return ra;
    }

    /**
     * Xoá một icon ở cả bốn mức phóng.
     *
     * <p>Đặt lại byte phiên bản về 255 cho khớp với những ô chưa dùng bao giờ.
     * Bỏ qua bước này thì ô trống mang một con số phiên bản vô nghĩa, và ngày
     * nào đó có ảnh mới ghi vào đúng id ấy mà cỡ tệp tình cờ trùng dư số thì
     * client tưởng bản lưu cũ vẫn đúng và không tải ảnh mới về.</p>
     *
     * @return câu báo lỗi, hoặc {@code null} nếu xong xuôi
     */
    public static String xoa(int id) {
        if (id < 0 || id > ID_TOI_DA) {
            return "id phải từ 0 đến " + ID_TOI_DA + ".";
        }
        try {
            for (int z = 1; z <= 4; z++) {
                File f = tepIcon(id, z);
                if (f.isFile() && !f.delete()) {
                    return "Không xoá được " + f.getAbsolutePath();
                }
                datPhienBan(z, id, 255);
            }
            return null;
        } catch (Exception ex) {
            return "Lỗi khi xoá ảnh: " + ex;
        }
    }

    /** Chiều rộng khung mặc định ở mức x1. */
    public static int canhMacDinh() {
        return RONG_MAC_DINH;
    }

    /** Chiều cao khung mặc định ở mức x1. */
    public static int caoMacDinh() {
        return CAO_MAC_DINH;
    }

    /**
     * Chuyển một tấm ảnh thành icon đủ bốn mức, đúng đường mà dòng lệnh đi.
     *
     * <p>Tách riêng khỏi {@link #main(String[])} để panel gọi được cùng một
     * đoạn mã đã chạy thật, thay vì chép lại rồi lệch nhau về sau.</p>
     *
     * @param nhatKy nơi ghi từng dòng kết quả, để nơi gọi hiện lại cho người dùng
     * @return câu báo lỗi, hoặc {@code null} nếu xong xuôi
     */
    public static String chuyen(File nguon, int id, int rong, int cao, boolean cat,
            java.util.List<String> nhatKy) {
        if (!coDuLieu(GOC)) {
            return "Không tìm thấy thư mục data/icon quanh " + GOC.getAbsolutePath();
        }
        if (nguon == null || !nguon.isFile()) {
            return "Không thấy tệp ảnh.";
        }
        if (id < 0 || id > ID_TOI_DA) {
            return "id phải từ 0 đến " + ID_TOI_DA + ".";
        }
        if (rong < 8 || rong > 64 || cao < 8 || cao > 64) {
            return "Khung phải từ 8 đến 64 mỗi chiều.";
        }
        try {
            BufferedImage anh = ImageIO.read(nguon);
            if (anh == null) {
                return "Không đọc được ảnh — cần png, jpg hoặc gif.";
            }
            anh = sangArgb(anh);
            if (cat) {
                soDiemNenDaXoa = 0;
                anh = boNenDong(anh);
                if (soDiemNenDaXoa > 0) {
                    nhatKy.add("Đã xoá nền một màu, " + soDiemNenDaXoa + " điểm ảnh.");
                }
                anh = catVienTrong(anh);
            }
            if (anh == null) {
                return "Ảnh trống hoàn toàn sau khi cắt viền.";
            }
            nhatKy.add("Ảnh gốc " + anh.getWidth() + "x" + anh.getHeight()
                    + ", khung " + rong + "x" + cao + " ở mức x1.");
            for (int z = 1; z <= 4; z++) {
                nhatKy.add(moTaMotMuc(anh, id, z, rong * z, cao * z));
            }
            return null;
        } catch (Exception ex) {
            return "Lỗi khi chuyển ảnh: " + ex;
        }
    }

    private static File tepPhienBan(int zoom) {
        return new File(GOC, "data/smallimage_version/x" + zoom
                + "/smallimage_version_data");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            inCachDung();
            return;
        }
        if (!coDuLieu(GOC)) {
            System.out.println("Không tìm thấy thư mục dữ liệu.");
            System.out.println("Đã dò quanh: " + GOC.getAbsolutePath());
            System.out.println("Chạy lại từ thư mục Server, hoặc chỉ đường bằng");
            System.out.println("  java -Dnro.goc=<thư mục Server> -cp out nro.tool.ThemIcon ...");
            return;
        }
        System.out.println("Thư mục ảnh: "
                + new File(GOC, "data/icon").getAbsolutePath());
        if ("--trong".equals(args[0])) {
            inChoTrong();
            return;
        }

        File nguon = new File(args[0]);
        int id = -1;
        int rong = RONG_MAC_DINH;
        int cao = CAO_MAC_DINH;
        boolean cat = true;
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--id":
                    id = Integer.parseInt(args[++i]);
                    break;
                case "--rong":
                    rong = Integer.parseInt(args[++i]);
                    break;
                case "--cao":
                    cao = Integer.parseInt(args[++i]);
                    break;
                case "--khong-cat":
                    cat = false;
                    break;
                default:
                    System.out.println("Không hiểu tham số: " + args[i]);
                    return;
            }
        }

        if (!nguon.isFile()) {
            System.out.println("Không thấy tệp: " + nguon.getAbsolutePath());
            return;
        }

        if (id < 0) {
            id = timIdTrong();
            if (id < 0) {
                System.out.println("Hết chỗ trống, không còn id nào chưa dùng.");
                return;
            }
            System.out.println("Tự chọn id còn trống: " + id);
        } else if (id > ID_TOI_DA) {
            System.out.println("id phải từ 0 đến " + ID_TOI_DA
                    + ", client không cấp phát quá chừng đó ô.");
            return;
        } else if (tepIcon(id, 2).isFile()) {
            System.out.println("id " + id + " đang có ảnh rồi. Ghi đè sẽ đổi icon"
                    + " của mọi vật phẩm đang dùng id này.");
        }

        java.util.List<String> nhatKy = new java.util.ArrayList<>();
        String loi = chuyen(nguon, id, rong, cao, cat, nhatKy);
        for (String dong : nhatKy) {
            System.out.println("  " + dong);
        }
        if (loi != null) {
            System.out.println(loi);
            return;
        }
        System.out.println("Xong. Đặt icon_id = " + id + " cho vật phẩm trong panel.");
        System.out.println("Người chơi đang đăng nhập phải thoát ra vào lại thì"
                + " client mới nhận bảng phiên bản mới.");
    }

    /** Vẽ và ghi một mức phóng, rồi cập nhật đúng ô của nó trong bảng phiên bản. */
    private static String moTaMotMuc(BufferedImage goc, int id, int zoom,
            int hopRong, int hopCao)
            throws Exception {
        // Vua chieu ngang VUA chieu doc: khung o hanh trang khong vuong (34x23)
        // nen ep vao mot hop vuong se phi mat mot chieu.
        double ti = Math.min((double) hopRong / goc.getWidth(),
                (double) hopCao / goc.getHeight());
        int w = Math.max(1, (int) Math.round(goc.getWidth() * ti));
        int h = Math.max(1, (int) Math.round(goc.getHeight() * ti));

        BufferedImage ra = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ra.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        // SCALE_SMOOTH lay trung binh ca vung nen thu nho manh van con net;
        // ve thang bang drawImage se rang cua o muc x1.
        g.drawImage(goc.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        File dich = tepIcon(id, zoom);
        dich.getParentFile().mkdirs();
        ImageIO.write(ra, "png", dich);

        int ban = (int) (dich.length() % 127);
        datPhienBan(zoom, id, ban);
        return "x" + zoom + ": " + w + "x" + h + ", "
                + dich.length() + " byte, phiên bản " + ban;
    }

    /**
     * Ghi byte phiên bản của một icon.
     *
     * <p>Ghi đúng một byte tại chỗ chứ không đọc rồi viết lại cả tệp: bảng này
     * dùng chung cho hơn ba vạn ảnh, viết lại cả tệp mà lỡ đứt tay giữa chừng
     * là hỏng ảnh của toàn bộ máy chủ.</p>
     */
    private static void datPhienBan(int zoom, int id, int ban) throws Exception {
        File f = tepPhienBan(zoom);
        if (!f.isFile()) {
            System.out.println("  ! thiếu " + f.getPath() + ", bỏ qua phiên bản");
            return;
        }
        try (RandomAccessFile r = new RandomAccessFile(f, "rw")) {
            r.seek(2 + id);
            r.write(ban);
        }
    }

    /** Bảo đảm ảnh có kênh trong suốt để cắt viền và ghi png không mất nền. */
    private static BufferedImage sangArgb(BufferedImage v) {
        if (v.getType() == BufferedImage.TYPE_INT_ARGB) {
            return v;
        }
        BufferedImage ra = new BufferedImage(v.getWidth(), v.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ra.createGraphics();
        g.drawImage(v, 0, 0, null);
        g.dispose();
        return ra;
    }

    /**
     * Xoá nền một màu, ví dụ ảnh chụp trên nền trắng.
     *
     * <p>Chỉ làm khi <b>cả bốn góc cùng một màu đục</b>. Điều kiện chặt như vậy
     * để ảnh vốn đã có nền trong suốt, hay ảnh mà góc là một phần của hình, thì
     * không bị đục thủng oan.</p>
     */
    private static BufferedImage boNenDong(BufferedImage v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int[] goc = {v.getRGB(0, 0), v.getRGB(w - 1, 0),
            v.getRGB(0, h - 1), v.getRGB(w - 1, h - 1)};
        // Goc nao trong suot thi anh von da tach nen roi, khong dung vao nua.
        for (int g : goc) {
            if ((g >>> 24) < 250) {
                return v;
            }
        }
        // Truoc day doi bon goc GIONG HET nhau moi chiu lam. Anh chup hay anh
        // nen JPEG gan nhu khong bao gio dat dieu kien do — nen ham nay tuong
        // nhu chay ma thuc te khong lam gi ca.
        //
        // Gio chi doi bon goc GAN giong nhau, roi lay mau trung binh lam nen.
        int nr = 0;
        int ng = 0;
        int nb = 0;
        for (int g : goc) {
            nr += (g >> 16) & 0xFF;
            ng += (g >> 8) & 0xFF;
            nb += g & 0xFF;
        }
        nr /= 4;
        ng /= 4;
        nb /= 4;
        for (int g : goc) {
            int d = Math.abs(((g >> 16) & 0xFF) - nr)
                    + Math.abs(((g >> 8) & 0xFF) - ng)
                    + Math.abs((g & 0xFF) - nb);
            if (d > 40) {
                return v;
            }
        }
        int xoa = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = v.getRGB(x, y);
                int dr = Math.abs(((p >> 16) & 0xFF) - nr);
                int dg = Math.abs(((p >> 8) & 0xFF) - ng);
                int db = Math.abs((p & 0xFF) - nb);
                // Nguong 60 thay vi 24: anh nen JPEG co vien lem quanh hinh, de
                // 24 thi con lai mot khung mo mo quanh icon.
                if (dr + dg + db <= 60) {
                    v.setRGB(x, y, p & 0x00FFFFFF);
                    xoa++;
                }
            }
        }
        soDiemNenDaXoa = xoa;
        return v;
    }

    /** Số điểm ảnh nền vừa bị xoá, chỉ để báo lại cho người dùng. */
    private static int soDiemNenDaXoa;

    /** Cắt bỏ viền trong suốt để icon lấp đầy ô, trả {@code null} nếu ảnh rỗng. */
    private static BufferedImage catVienTrong(BufferedImage v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int trai = w;
        int phai = -1;
        int tren = h;
        int duoi = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((v.getRGB(x, y) >>> 24) > 8) {
                    if (x < trai) {
                        trai = x;
                    }
                    if (x > phai) {
                        phai = x;
                    }
                    if (y < tren) {
                        tren = y;
                    }
                    if (y > duoi) {
                        duoi = y;
                    }
                }
            }
        }
        if (phai < 0) {
            return null;
        }
        if (trai == 0 && tren == 0 && phai == w - 1 && duoi == h - 1) {
            return v;
        }
        return v.getSubimage(trai, tren, phai - trai + 1, duoi - tren + 1);
    }

    /** Id đầu tiên chưa có ảnh ở cả bốn mức phóng. */
    private static int timIdTrong() {
        for (int id = 1; id <= ID_TOI_DA; id++) {
            if (idConTrong(id)) {
                return id;
            }
        }
        return -1;
    }

    private static boolean idConTrong(int id) {
        for (int z = 1; z <= 4; z++) {
            if (tepIcon(id, z).isFile()) {
                return false;
            }
        }
        return true;
    }

    /** Liệt kê những dải id chưa dùng, dải to xếp trước. */
    private static void inChoTrong() {
        java.util.List<int[]> dai = new java.util.ArrayList<>();
        int dau = -1;
        for (int id = 1; id <= ID_TOI_DA + 1; id++) {
            boolean trong = id <= ID_TOI_DA && idConTrong(id);
            if (trong && dau < 0) {
                dau = id;
            } else if (!trong && dau >= 0) {
                dai.add(new int[]{dau, id - 1});
                dau = -1;
            }
        }
        dai.sort((a, b) -> (b[1] - b[0]) - (a[1] - a[0]));
        int tong = 0;
        for (int[] d : dai) {
            tong += d[1] - d[0] + 1;
        }
        System.out.println("Còn " + tong + " id trống trong khoảng 1 tới " + ID_TOI_DA
                + ". Mười dải rộng nhất:");
        for (int i = 0; i < dai.size() && i < 10; i++) {
            int[] d = dai.get(i);
            System.out.println("  " + d[0] + " .. " + d[1]
                    + "   (" + (d[1] - d[0] + 1) + " chỗ)");
        }
    }

    private static void inCachDung() {
        System.out.println("Cách dùng, chạy từ thư mục Server:");
        System.out.println("  java -cp out nro.tool.ThemIcon --trong");
        System.out.println("      xem còn dải id nào chưa dùng");
        System.out.println("  java -cp out nro.tool.ThemIcon anh.png");
        System.out.println("      tự chọn id trống, khung " + RONG_MAC_DINH
                + "x" + CAO_MAC_DINH + " ở mức x1");
        System.out.println("  java -cp out nro.tool.ThemIcon anh.png --id 25300 --rong 30 --cao 21");
        System.out.println("      chỉ định id và cạnh");
        System.out.println("  thêm --khong-cat để giữ nguyên viền trong suốt");
    }
}
