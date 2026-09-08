using System;
using UnityEngine;

namespace Game5.God
{
    /// <summary>
    /// Hộp cấu hình VoiceChat — vẽ đè lên màn chơi, không chen vào ngăn xếp màn hình.
    /// </summary>
    /// <remarks>
    /// <para>Vẽ thẳng trong <c>ClientManager.GUI</c> và tự bắt chạm, thay vì dựng
    /// một <c>mScreen</c> riêng: engine này đổi màn hình là dọn cả trạng thái điều
    /// khiển, mở hộp cấu hình mà nhân vật đứng khựng thì khó chịu. Đè lên thì
    /// người chơi vẫn thấy game chạy phía sau.</para>
    ///
    /// <para><b>Thiết bị ra:</b> Unity không có API chọn thiết bị phát — cả
    /// <c>AudioSettings</c> lẫn <c>AudioSource</c> đều đi theo thiết bị mặc định
    /// của hệ điều hành. Nên ô đó hiện cấu hình đang chạy (tần số, số kênh) để
    /// biết máy đang phát ra đâu, kèm câu nhắc đổi ở cài đặt hệ thống. Muốn đổi
    /// ngay trong game thì phải nhúng thư viện gốc (FMOD/NAudio) cho từng nền
    /// tảng — việc đó lớn hơn hẳn phần còn lại.</para>
    /// </remarks>
    public class VoiceConfigUI
    {
        private static VoiceConfigUI instance;

        public static VoiceConfigUI getInstance()
        {
            return instance ?? (instance = new VoiceConfigUI());
        }

        public bool dangMo;

        // Khung hộp
        private const int RONG = 250;
        private const int CAO = 232;
        private const int MAU_NEN = 3355443;
        private const int MAU_VIEN = 16755200;
        private const int MAU_O = 6710886;
        private const int MAU_O_BAT = 3381606;

        private int x0, y0;
        private int micChon;

        public void mo()
        {
            dangMo = true;
            // Bat lai vi tri mic dang dung, de mui ten trai/phai chay tiep tu do.
            string[] ds = VoiceChat.danhSachMic();
            micChon = 0;
            for (int i = 0; i < ds.Length; i++)
            {
                if (ds[i] == VoiceChat.tenMic)
                {
                    micChon = i;
                    break;
                }
            }
        }

        public void dong()
        {
            dangMo = false;
        }

        // ------------------------------------------------------------------
        //  Vẽ
        // ------------------------------------------------------------------
        public void ve(mGraphics g)
        {
            if (!dangMo)
            {
                return;
            }
            x0 = GameCanvas.w / 2 - RONG / 2;
            y0 = GameCanvas.h / 2 - CAO / 2;

            g.setColor(0, 0.55f);
            g.fillRect(0, 0, GameCanvas.w, GameCanvas.h);
            g.setColor(MAU_NEN, 0.95f);
            g.fillRect(x0, y0, RONG, CAO);
            g.setColor(MAU_VIEN);
            g.drawRect(x0, y0, RONG, CAO);

            mFont.tahoma_7b_yellow.drawString(g, "Cấu hình VoiceChat",
                    x0 + RONG / 2, y0 + 4, mFont.CENTER);
            veNut(g, x0 + RONG - 18, y0 + 3, 15, 12, "X", false);

            int y = y0 + 20;

            // ---- thiết bị vào ----
            mFont.tahoma_7b_white.drawString(g, "Mic:", x0 + 6, y, mFont.LEFT);
            veNut(g, x0 + 6, y + 11, 14, 13, "<", false);
            veNut(g, x0 + RONG - 20, y + 11, 14, 13, ">", false);
            string[] ds = VoiceChat.danhSachMic();
            string tenMic = ds.Length == 0 ? "(máy không có mic)"
                    : ds[Mathf.Clamp(micChon, 0, ds.Length - 1)];
            g.setColor(MAU_O);
            g.fillRect(x0 + 22, y + 11, RONG - 44, 13);
            mFont.tahoma_7b_green2.drawString(g, catBot(tenMic, 30),
                    x0 + RONG / 2, y + 13, mFont.CENTER);
            y += 28;

            // ---- thiết bị ra ----
            mFont.tahoma_7b_white.drawString(g, "Loa:", x0 + 6, y, mFont.LEFT);
            g.setColor(MAU_O);
            g.fillRect(x0 + 6, y + 11, RONG - 12, 13);
            mFont.tahoma_7b_green2.drawString(g, catBot(moTaLoa(), 34),
                    x0 + RONG / 2, y + 13, mFont.CENTER);
            y += 26;
            mFont.tahoma_7_grey.drawString(g,
                    "Đổi loa ở cài đặt âm thanh của máy", x0 + RONG / 2, y,
                    mFont.CENTER);
            y += 14;

            // ---- kênh ----
            mFont.tahoma_7b_white.drawString(g, "Kênh (chỉ bật một):", x0 + 6, y,
                    mFont.LEFT);
            y += 12;
            int rongNut = (RONG - 24) / 3;
            veNut(g, x0 + 6, y, rongNut, 15, "Khu",
                    VoiceChat.kenhDangBat == VoiceChat.KENH_KHU);
            veNut(g, x0 + 12 + rongNut, y, rongNut, 15, "Map",
                    VoiceChat.kenhDangBat == VoiceChat.KENH_MAP);
            veNut(g, x0 + 18 + rongNut * 2, y, rongNut, 15, "Bang",
                    VoiceChat.kenhDangBat == VoiceChat.KENH_BANG);
            y += 21;

            // ---- loa / mic bật tắt ----
            // Ghi ten kenh vao nhan: loa va mic gio nho rieng tung kenh,
            // khong ghi ra thi bam xong khong biet vua doi cho kenh nao.
            int k = VoiceChat.chiMuc(VoiceChat.kenhXem);
            mFont.tahoma_7_grey.drawString(g,
                    "Riêng kênh " + VoiceChat.tenKenh(VoiceChat.kenhXem),
                    x0 + RONG - 6, y + 3, mFont.RIGHT);
            veNut(g, x0 + 6, y, 110, 15,
                    VoiceChat.batLoa[k] ? "Loa: bật" : "Loa: tắt",
                    VoiceChat.batLoa[k]);
            veNut(g, x0 + 122, y, 110, 15,
                    VoiceChat.micTat[k] ? "Mic: tắt" : "Mic: bật",
                    !VoiceChat.micTat[k]);
            y += 21;


            // ---- hai thanh âm lượng ----
            // Tach loa va mic thanh hai thanh: mot thanh chung thi van to nho
            // cung luc, khong xu ly duoc canh mic thu be ma loa da du to.
            mFont.tahoma_7b_white.drawString(g,
                    "Âm thanh loa: " + (int)(VoiceChat.amLuongLoa * 100),
                    x0 + 6, y, mFont.LEFT);
            y += 12;
            veThanh(g, y, VoiceChat.amLuongLoa);
            y += 14;

            mFont.tahoma_7b_white.drawString(g,
                    "Âm thanh mic: " + (int)(VoiceChat.amLuongMic * 100),
                    x0 + 6, y, mFont.LEFT);
            y += 12;
            veThanh(g, y, VoiceChat.amLuongMic);
            y += 14;

            mFont.tahoma_7_grey.drawString(g, trangThai(), x0 + RONG / 2, y,
                    mFont.CENTER);
        }

        /// <summary>Một thanh trượt 0..2, đầy tới đâu là giá trị tới đó.</summary>
        private void veThanh(mGraphics g, int y, float giaTri)
        {
            g.setColor(MAU_O);
            g.fillRect(x0 + 6, y, RONG - 12, 8);
            g.setColor(MAU_VIEN);
            g.fillRect(x0 + 6, y, (int)((RONG - 12) * Mathf.Clamp01(giaTri / 2f)), 8);
            g.setColor(MAU_NEN);
            g.drawRect(x0 + 6, y, RONG - 12, 8);
        }


        private void veNut(mGraphics g, int x, int y, int w, int h, string chu,
                bool bat)
        {
            g.setColor(bat ? MAU_O_BAT : MAU_O);
            g.fillRect(x, y, w, h);
            g.setColor(MAU_VIEN);
            g.drawRect(x, y, w, h);
            mFont.tahoma_7b_white.drawString(g, chu, x + w / 2, y + h / 2 - 5,
                    mFont.CENTER);
        }

        private static string catBot(string s, int toiDa)
        {
            if (s == null)
            {
                return "";
            }
            return s.Length <= toiDa ? s : s.Substring(0, toiDa - 1) + "…";
        }

        private static string moTaLoa()
        {
            try
            {
                AudioConfiguration c = AudioSettings.GetConfiguration();
                return "Mặc định hệ thống — " + c.sampleRate + " Hz";
            }
            catch (Exception)
            {
                return "Mặc định hệ thống";
            }
        }

        private static string trangThai()
        {
            if (VoiceChat.kenhDangBat == VoiceChat.KENH_TAT)
            {
                return "Mic đang tắt";
            }
            return "Đang mở mic — kênh "
                    + VoiceChat.tenKenh(VoiceChat.kenhDangBat);
        }

        // ------------------------------------------------------------------
        //  Chạm
        // ------------------------------------------------------------------
        /// <summary>Bắt chạm. Trả về true nếu đã nuốt sự kiện, chỗ gọi đừng xử tiếp.</summary>
        /// <remarks>
        /// Cộng dồn <c>y</c> theo <b>đúng thứ tự và đúng bước nhảy</b> của hàm
        /// vẽ. Ghi số tuyệt đối cho từng hàng thì thêm một hàng vào giữa là mọi
        /// vùng chạm phía dưới lệch đi mà không có gì báo.
        /// </remarks>
        public bool capNhatCham()
        {
            if (!dangMo)
            {
                return false;
            }
            // Nuot MOI cham khi dang mo, ke ca cham ra ngoai khung: bam xuyen
            // qua hop ma nhan vat chay di thi rat kho chiu.
            if (!GameCanvas.isPointerJustRelease)
            {
                return true;
            }

            if (cham(x0 + RONG - 18, y0 + 3, 15, 12))
            {
                dong();
                return true;
            }

            int y = y0 + 20;

            // mic: hai mũi tên
            string[] ds = VoiceChat.danhSachMic();
            if (cham(x0 + 6, y + 11, 14, 13))
            {
                doiMic(ds, -1);
                return true;
            }
            if (cham(x0 + RONG - 20, y + 11, 14, 13))
            {
                doiMic(ds, 1);
                return true;
            }
            y += 28;    // qua hàng mic
            y += 26;    // qua ô loa
            y += 14;    // qua dòng nhắc
            y += 12;    // qua nhãn "Kênh"

            int rongNut = (RONG - 24) / 3;
            if (cham(x0 + 6, y, rongNut, 15))
            {
                VoiceChat.doiKenh(VoiceChat.KENH_KHU);
                return true;
            }
            if (cham(x0 + 12 + rongNut, y, rongNut, 15))
            {
                VoiceChat.doiKenh(VoiceChat.KENH_MAP);
                return true;
            }
            if (cham(x0 + 18 + rongNut * 2, y, rongNut, 15))
            {
                VoiceChat.doiKenh(VoiceChat.KENH_BANG);
                return true;
            }
            y += 21;

            if (cham(x0 + 6, y, 110, 15))
            {
                int kk = VoiceChat.chiMuc(VoiceChat.kenhXem);
                VoiceChat.batLoa[kk] = !VoiceChat.batLoa[kk];
                return true;
            }
            if (cham(x0 + 122, y, 110, 15))
            {
                int km = VoiceChat.chiMuc(VoiceChat.kenhXem);
                VoiceChat.micTat[km] = !VoiceChat.micTat[km];
                return true;
            }
            y += 21;

            y += 12;    // qua nhãn "Âm thanh loa"
            if (cham(x0 + 6, y - 3, RONG - 12, 14))
            {
                VoiceChat.amLuongLoa = tinhMuc();
                return true;
            }
            y += 14;

            y += 12;    // qua nhãn "Âm thanh mic"
            if (cham(x0 + 6, y - 3, RONG - 12, 14))
            {
                VoiceChat.amLuongMic = tinhMuc();
                return true;
            }
            return true;
        }

        /// <summary>Mức 0..2 theo vị trí bấm trên thanh.</summary>
        /// <remarks>
        /// Bấm vào đâu nhảy tới đó thay vì kéo thả: kéo thả trong engine này
        /// phải tự theo dõi trạng thái giữa các khung hình, mà bấm là đủ dùng.
        /// </remarks>
        private float tinhMuc()
        {
            int tuong = GameCanvas.pxLast - (x0 + 6);
            return Mathf.Clamp(tuong * 2f / (RONG - 12), 0f, 2f);
        }

        private void doiMic(string[] ds, int buoc)
        {
            if (ds.Length == 0)
            {
                return;
            }
            micChon = (micChon + buoc + ds.Length) % ds.Length;
            VoiceChat.tenMic = ds[micChon];
            // Đang mở mic thì mở lại bằng thiết bị mới, không thì đổi xong vẫn
            // thu bằng mic cũ cho tới lần bật sau.
            if (VoiceChat.kenhDangBat != VoiceChat.KENH_TAT)
            {
                int kenh = VoiceChat.kenhDangBat;
                VoiceChat.doiKenh(kenh);
                VoiceChat.doiKenh(kenh);
            }
        }

        private static bool cham(int x, int y, int w, int h)
        {
            if (GameCanvas.isPointerHoldIn(x, y, w, h))
            {
                GameCanvas.clearAllPointerEvent();
                return true;
            }
            return false;
        }
    }
}
