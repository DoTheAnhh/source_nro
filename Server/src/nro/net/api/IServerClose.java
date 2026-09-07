package nro.net.api;

/**
 * Callback chạy khi socket lắng nghe của server đóng.
 *
 * <p>Gắn qua {@link INetwork#setDoSomeThingWhenClose(IServerClose)}.
 * Cài đặt hiện tại trong {@code ServerManager} chỉ ghi log rồi
 * {@code System.exit(0)} — tức là <b>không có tắt êm</b>: các thread game, boss,
 * bot bị giết ngang, dữ liệu chưa lưu sẽ mất. Xem giai đoạn 7 của kế hoạch
 * tái cấu trúc.</p>
 */
public interface IServerClose {

    void serverClose();
}
