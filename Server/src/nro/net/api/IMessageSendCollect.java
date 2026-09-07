package nro.net.api;

import java.io.DataOutputStream;
import nro.net.io.Message;

import java.io.DataInputStream;

/**
 * Bộ mã hoá/giải mã khung tin (codec) — nơi duy nhất biết định dạng byte trên dây.
 *
 * <p><b>Khung tin trên dây:</b></p>
 * <pre>
 *   [1 byte  command]
 *   [2 byte  size]      -- hoặc 3 byte với các lệnh payload lớn, xem bên dưới
 *   [size byte payload]
 * </pre>
 *
 * <p><b>Mã hoá:</b> sau khi bắt tay khoá xong ({@code session.sentKey() == true}),
 * <i>mọi</i> byte — kể cả command và size — đều được XOR với khoá.
 * Con trỏ khoá đọc ({@code curR}) và ghi ({@code curW}) <b>tách rời</b> và mỗi cái
 * chạy vòng độc lập.</p>
 *
 * <p><b>Hệ quả quan trọng:</b> vì con trỏ khoá tăng theo <i>từng byte</i>, luồng
 * mã hoá là có trạng thái và <b>phải giữ đúng thứ tự</b>. Đây chính là lý do
 * {@code Sender.doSendMessage} phải {@code synchronized}: hai thread cùng gửi sẽ
 * làm lệch con trỏ khoá và client giải mã ra rác.</p>
 *
 * <p><b>Lệnh payload lớn:</b> một số lệnh dùng <b>3 byte</b> độ dài thay vì 2
 * (giới hạn 2 byte là 64 KiB, không đủ). Danh sách hiện tại trong
 * {@code MessageSendCollect.doSendMessage}:
 * {@code -32, -66, -74, 11, -67, -87, 66, -28}. Client Unity
 * ({@code Session_ME}) phải có <b>đúng cùng danh sách này</b>, nếu lệch một lệnh
 * là toàn bộ luồng lệch khung ngay sau đó.</p>
 */
public interface IMessageSendCollect {

    /**
     * Đọc trọn một gói tin từ luồng vào (chặn cho tới khi đủ byte).
     *
     * @param p0 session, dùng để lấy khoá và biết đã bắt tay xong chưa
     * @param p1 luồng đọc của socket
     * @return gói tin đã giải mã, sẵn sàng để {@code reader()}
     */
    Message readMessage(final ISession p0, final DataInputStream p1) throws Exception;

    /** Giải mã một byte và đẩy con trỏ khoá đọc lên một bước. */
    byte readKey(final ISession p0, final byte p1);

    /**
     * Mã hoá và ghi trọn một gói tin ra luồng, rồi {@code flush()}.
     * Phải gọi trong ngữ cảnh đồng bộ — xem ghi chú về con trỏ khoá ở trên.
     */
    void doSendMessage(final ISession p0, final DataOutputStream p1, final Message p2) throws Exception;

    /** Mã hoá một byte và đẩy con trỏ khoá ghi lên một bước. */
    byte writeKey(final ISession p0, final byte p1);
}
