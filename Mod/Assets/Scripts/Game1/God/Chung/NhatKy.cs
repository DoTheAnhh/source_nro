namespace Game1.God
{
    /// <summary>
    /// Ghi vết ra <b>một tệp trên đĩa</b> để soi những lỗi chỉ tái hiện được lúc
    /// chơi thật.
    /// </summary>
    /// <remarks>
    /// <para><c>Res.outz</c> đi vào cửa sổ Console của Unity — người chơi phải tự
    /// chép lại rồi thuật lại, mà thuật lại thì mất đúng cái thứ tự và số lần
    /// vốn là dữ kiện cần. Ghi thẳng ra tệp thì đọc được nguyên văn.</para>
    ///
    /// <para>Đây là <b>công cụ tạm</b>. Tìm ra lỗi rồi thì gỡ các lời gọi đi,
    /// đừng để nó ghi đĩa suốt ván chơi.</para>
    ///
    /// <para>Mọi lỗi khi ghi đều bị nuốt: một cái vết không được phép làm treo
    /// game, kể cả khi thư mục không cho ghi.</para>
    /// </remarks>
    public static class NhatKy
    {
        private const string TEP = "F:/NgocRong/nhat-ky-client.txt";

        /// <summary>Bật/tắt toàn bộ việc ghi vết.</summary>
        public static bool bat = true;

        private static bool daXoaDauVan;

        public static void ghi(string chu)
        {
            if (!bat)
            {
                return;
            }
            try
            {
                if (!daXoaDauVan)
                {
                    daXoaDauVan = true;
                    System.IO.File.WriteAllText(TEP, "--- van moi ---\n");
                }
                System.IO.File.AppendAllText(TEP,
                        System.DateTime.Now.ToString("HH:mm:ss.fff") + "  "
                        + chu + "\n");
            }
            catch (System.Exception)
            {
                // Khong ghi duoc thi thoi.
            }
        }
    }
}
