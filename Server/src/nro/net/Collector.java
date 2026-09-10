package nro.net;

import java.net.Socket;

import lombok.Setter;
import java.io.DataInputStream;
import java.io.IOException;
import nro.net.api.IMessageSendCollect;
import nro.net.api.ISession;
import nro.net.io.Message;

/**
 * Thread <b>đọc</b> của một session — chân trái của tam giác Collector/QueueHandler/Sender.
 *
 * <p><b>Vai trò:</b> đọc byte từ socket cho tới khi dựng đủ một {@link Message},
 * rồi <i>đẩy sang hàng đợi</i> chứ không xử lý. Nhờ tách như vậy, một gói tin
 * nghiệp vụ chạy chậm không làm nghẽn việc đọc socket.</p>
 *
 * <p><b>Ngoại lệ duy nhất:</b> gói {@link CommandMessage#GET_SESSION_ID} được xử
 * lý ngay tại đây (bắt tay khoá), không bao giờ tới {@code Controller}.</p>
 *
 * <p><b>Đây cũng là nơi phát hiện client rớt.</b> Khi socket đứt,
 * {@code readMessage} ném ngoại lệ, vòng lặp thoát, và phần code sau vòng lặp
 * chạy dọn dẹp — báo {@code sessionDisconnect} rồi {@code disconnect()}.
 * Không có đường nào khác phát hiện rớt kết nối.</p>
 */
public final class Collector implements Runnable {

    /** Session sở hữu thread này. Bị gán {@code null} trong {@link #dispose()}. */
    private ISession session;

    /**
     * Địa chỉ của phiên, chép lại ngay lúc tạo.
     *
     * <p>{@link #dispose()} gán {@link #session} về {@code null}, mà nó có thể
     * chạy <b>trong lúc</b> {@link #run()} còn đang gỡ ngăn xếp. Khi đó câu log
     * hỏi {@code session.getIP()} sẽ không có gì để hỏi và in ra dấu hỏi — đúng
     * dòng "Loi doc goi tin tu ?" mà không ai lần ra được là của ai.</p>
     */
    private final String ipGhiNho;

    /**
     * Bo dem nhip goi cua rieng phien nay.
     *
     * <p>Moi phien mot bo dem, va chi luong doc cua phien do dung toi, nen
     * khong can dong bo gi ca.</p>
     */
    /** Ngu bao lau khi mot phien DA DANG NHAP gui nhanh qua nguong. */
    private static final long NGU_KHI_NHANH_MS = 40L;

    private final nro.server.ChongDdos.NhipGoi nhipGoi
            = new nro.server.ChongDdos.NhipGoi();

    /** Luồng đọc của socket. */
    private DataInputStream dis;

    /**
     * Codec giải mã. Cùng một đối tượng với bên {@link Sender} — xem
     * {@code Session.setSendCollect}.
     */
    @Setter
    private IMessageSendCollect collect;

    /** Tạo collector và mở luồng đọc từ socket. */
    public Collector(ISession session, Socket socket) {
        this.session = session;
        String ip = "?";
        try {
            if (socket != null && socket.getInetAddress() != null) {
                ip = socket.getInetAddress().getHostAddress();
            }
        } catch (Exception ignored) {
            // Socket dut ngay luc nhan ket noi -> khong co dia chi, giu "?".
        }
        this.ipGhiNho = ip;
        this.setSocket(socket);
    }

    /**
     * Gắn (hoặc thay) socket và mở lại luồng đọc.
     *
     * <p>Trả về {@code this} để gọi nối chuỗi trong constructor của {@code Session}.
     * Lỗi mở luồng bị nuốt, khi đó {@code dis} còn {@code null} và
     * {@link #run()} sẽ thoát ngay ở vòng đầu.</p>
     */
    public Collector setSocket(Socket socket) {
        try {
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException ignored) {
        }
        return this;
    }

    /**
     * Vòng đọc chính. Chạy tới khi session ngắt hoặc socket lỗi.
     *
     * <p><b>Không có {@code sleep}</b> — {@code readMessage} tự chặn cho tới khi có
     * byte, nên vòng lặp này không quay không tải CPU.</p>
     *
     * <p>Nhánh {@code GET_SESSION_ID} rẽ theo vai của session:</p>
     * <ul>
     *   <li>{@code SERVER} — {@code sendKey()}: server gửi khoá xuống client.</li>
     *   <li>{@code CLIENT} — {@code setKey(msg)}: đọc khoá server gửi lên.</li>
     * </ul>
     *
     * <p>Mọi gói tin khác đi vào {@code queueHandler.addMessage(msg)} và được
     * thread {@link QueueHandler} xử lý.</p>
     *
     * <p><b>Nợ kỹ thuật:</b> hai khối {@code catch (Exception ignored)} rỗng gộp
     * chung mọi thứ — rớt mạng bình thường và bug thật (NPE, lỗi giải mã) trông
     * y hệt nhau và đều biến mất không dấu vết.</p>
     */
    @Override
    public void run() {
        try {
            while (session != null && session.isConnected()) {
                final Message msg = this.collect.readMessage(this.session, this.dis);
                // Tran NHIP goi cua mot phien.
                //
                // Tran theo IP chan duoc lu ket noi, nhung mot ket noi da
                // vao duoc roi thi no bom bao nhieu goi cung duoc: moi goi
                // la mot muc trong hang doi va mot luot xu ly. Mot khach
                // sua doi chi can MOT ket noi la du lam nghen may chu.
                //
                // NHUNG: nguoi da dang nhap thi KHONG duoc da ra.
                //
                // Mua mot lan chin muoi chin mon, nhat mot bai do roi, hay
                // bam danh lien tuc deu vuot nguong nay de dang — va ban cu
                // cat thang ket noi, client hien "May chu tat hoac mat song".
                // Dung la loi "mua nhieu thi vang game": khong phai may chu
                // qua tai, ma la chinh van chong lu tu da nguoi choi that.
                //
                // Nguoi da vao game thi HAM lai thay vi cat: ngu mot nhip
                // ngan ngay tren luong doc. Doc cham lai la TCP tu don goi
                // ben gui — day moi la cach ep nhip dung, va goi khong mat.
                // Chi phien CHUA dang nhap moi bi cat, vi do moi la lu that.
                if (!nhipGoi.them()) {
                    boolean daVaoGame = this.session != null
                            && ((nro.net.session.MySession) this.session).player != null;
                    if (daVaoGame) {
                        try {
                            Thread.sleep(NGU_KHI_NHANH_MS);
                        } catch (InterruptedException boQua) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        nro.server.ChongDdos.viPham(this.ipGhiNho,
                                "gui goi qua nhanh");
                        msg.cleanup();
                        break;
                    }
                }
                if (msg.command == CommandMessage.GET_SESSION_ID) {
                    if (session.getSocketType() == SocketType.SERVER) {
                        this.session.sendKey();
                    } else {
                        this.session.setKey(msg);
                    }
                    msg.cleanup();
                } else {
                    this.session.getQueueHandler().addMessage(msg);
                }
            }
        } catch (java.io.EOFException | java.net.SocketException
                | java.nio.channels.ClosedChannelException binhThuong) {
            // Ba dang NGAT KET NOI BINH THUONG, khong phai loi:
            //
            //   EOFException            client dong socket va gui FIN
            //   SocketException         duong truyen dut giua chung
            //   ClosedChannelException  CHINH MAY CHU dong socket trong khi
            //                           thread nay dang chan o read() — do la
            //                           duong di binh thuong cua disconnect().
            //
            // ClosedChannelException (va lop con AsynchronousCloseException)
            // KHONG phai con chau cua SocketException, nen truoc day no roi
            // xuong nhanh duoi va bi in ra do kem nguyen ngan xep — trong y
            // het mot vu sap, trong khi that ra chi la mot nguoi choi thoat.
            nro.core.log.Logger.log("[NET] " + ipGhiNho + " ngat ket noi: "
                    + binhThuong.getClass().getSimpleName() + "\n");
        } catch (Exception ex) {
            // MOI loi khac o day deu lam mat ket noi ma truoc gio khong de lai
            // dau vet nao — client treo, log sach tron, khong biet bat dau tu dau.
            nro.core.log.Logger.logException(Collector.class, ex,
                    "Loi doc goi tin tu " + ipGhiNho + " — phien bi ngat");
        }
        try {
            Network.gI().getAcceptHandler().sessionDisconnect(session);
        } catch (Exception ignored) {
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    /** Đóng luồng đọc. Việc này làm {@code readMessage} đang chặn bật ra ngay. */
    public void close() {
        if (dis != null) {
            try {
                dis.close();
            } catch (IOException ignored) {
            }
        }
    }

    /** Xoá mọi tham chiếu để GC thu hồi. Gọi sau {@link #close()}. */
    public void dispose() {
        session = null;
        dis = null;
        collect = null;
    }
}
