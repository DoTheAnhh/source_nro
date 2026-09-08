using System.Collections;
using System.Threading;
using UnityEngine;

namespace Game6.God
{
    /*Author: HairMod*/
    public class PlayerInfo
    {
        private static PlayerInfo instance { get; set; }
        public bool canLogin;
        private long timeLogin, timeWait;
        public static PlayerInfo getInstance()
        {
            return (instance == null) ? (instance = new PlayerInfo()) : instance;
        }
        private void drawString(mGraphics g, string s, bool a, int x, int y)
        {
            mFont.tahoma_7_green2.drawString(g, a ? s + " Bật" : s + " Tắt", x, y, 0);
        }

        /// <summary>Mép trái của hàng chip, ngay sau nút bánh răng.</summary>
        private const int X_CHIP = 212;

        /// <summary>Con trỏ của hàng chip trạng thái.</summary>
        private int xChip, yChip;

        /// <summary>Đặt lại hàng chip về đầu. Gọi một lần mỗi khung hình.</summary>
        private void batDauChip()
        {
            xChip = X_CHIP;
            yChip = 4 + mGraphics.addYWhenOpenKeyBoard;
        }

        /// <summary>
        /// Một chip trạng thái: chữ ngắn trên nền bo góc.
        /// </summary>
        /// <remarks>
        /// <para>Thay cho các dòng "Tự Động Hồi Sinh: Bật" xếp dọc ở cạnh trái.
        /// Bật nhiều tuỳ chọn thì cột đó dài xuống tận hàng ô kỹ năng và đè lên
        /// chúng — đúng cảnh trong ảnh.</para>
        ///
        /// <para>Chỉ vẽ khi tuỳ chọn đang BẬT, nên không cần chữ "Bật" nữa: có
        /// mặt tức là đang bật. Nhờ vậy mỗi chip ngắn hơn hẳn và xếp ngang được.</para>
        ///
        /// <para>Xuống dòng trước khi chạm khung mục tiêu ở giữa cạnh trên —
        /// khung đó rộng khoảng 190 điểm và nằm giữa, nên mốc là
        /// <c>w/2 - 105</c>.</para>
        /// </remarks>
        /// <summary>
        /// Vẽ một chip trạng thái — <b>đang tắt hết</b>.
        /// </summary>
        /// <remarks>
        /// Hàng chip nằm cạnh nút bánh răng, và ở cửa sổ hẹp nó tràn xuống
        /// đè lên khung nhân vật. Chủ máy chủ chọn bỏ hẳn.
        ///
        /// Tắt ở ĐÂY chứ không xoá từng lời gọi: hơn chục chỗ gọi, mỗi chỗ
        /// kèm một điều kiện riêng — xoá hết là mất luôn thông tin "tính năng
        /// nào đang bật", mà bật lại chỉ cần bỏ một dòng return.
        /// </remarks>
        private void veChip(mGraphics g, string ten)
        {
            if (true)
            {
                return;
            }
            int w = mFont.tahoma_7b_white.getWidth(ten) + 10;
            int mocPhai = GameCanvas.w / 2 - 105;
            if (xChip + w > mocPhai && xChip > X_CHIP)
            {
                xChip = X_CHIP;
                yChip += 15;
            }
            // Vien bo goc: to mot hinh bo goc lam vien roi to long de len. Dung
            // `drawRect` thi vien ra khung vuong quanh mot cai long bo tron.
            g.setColor(0xC8933C, 0.9f);
            g.fillRect(xChip, yChip, w, 13, 6);
            g.setColor(0x241809, 0.85f);
            g.fillRect(xChip + 1, yChip + 1, w - 2, 11, 5);
            mFont.tahoma_7b_white.drawString(g, ten, xChip + w / 2, yChip + 1,
                    mFont.CENTER);
            xChip += w + 3;
        }
        public void paintInfoPlayer(mGraphics g)

        {
            // Ten + mau cua ban than va cua doi tuong dang chon do
            // GameScr.veKhungNguoiChoi / veKhungMucTieu lo het.
            //
            // Bon dong cu o day — so HP cam, so KI xanh, ten map, so khu — ve o
            // toa do co dinh (90,4) (90,17) (85,30) (85,40), dung ngay tren cho
            // khung moi dung. Ket qua la chu chong len chu, doc khong ra chu nao.
            // Gio chi con phan khung moi KHONG lo: dong Clan cua nguoi choi dang
            // chon, va bang ten ban do dat duoi khung.
            if (Char.myCharz().charFocus != null
                    && Char.myCharz().charFocus.clanID != -1)
            {
                mFont.tahoma_7b_red.drawStringBd(g,
                        "Clan " + Char.myCharz().charFocus.clanID,
                        GameCanvas.w / 2, 40, mFont.CENTER, mFont.tahoma_7_grey);
            }


            // Hang chip trang thai nam CANH NUT BANH RANG, khong con xep doc o
            // canh trai: bat nhieu tuy chon thi cot doc do dai xuong tan hang o
            // ky nang va de len chung.
            batDauChip();
            if (Revive.getInstance().getRevive())
            {
                veChip(g, "Hồi sinh");
            }
            if (nSkill.getInstance().canAttack)
            {
                veChip(g, "Tự đánh");
            }
            if (Mobs.IsTanSat)
            {
                veChip(g, "Tàn sát");
            }
            if (Mobs.tsPlayer)
            {
                veChip(g, "TS người");
            }
            if (VoiceChat.kenhDangBat != VoiceChat.KENH_TAT)
            {
                veChip(g, "Voice " + VoiceChat.tenKenh(VoiceChat.kenhDangBat));
                // Hai chip nay chi hien khi dang bat kenh: luc voice tat thi loa
                // hay mic bat tat deu khong co y nghia gi.
                int kV = VoiceChat.chiMuc(VoiceChat.kenhDangBat);
                if (VoiceChat.batLoa[kV])
                {
                    veChip(g, "Loa");
                }
                if (!VoiceChat.micTat[kV])
                {
                    veChip(g, "Mic");
                }
            }
            if (PetService.getInstance().getUp())
            {
                veChip(g, "Up đệ");
            }
            if (Boss.getInstance().isShow)
            {
                veChip(g, "BOSS");
            }
            if (ListChars.getInstance().isShow)
            {
                veChip(g, "D.s NV");
            }
            if (Mobs.IsAutoPickItems)
            {
                veChip(g, "Auto nhặt");
            }
            if (ListChars.getInstance().HideMap)
            {
                veChip(g, "Giảm ĐH");
            }
            if (canLogin)
            {
                veChip(g, "Auto login");
            }
        }
        public void Update()
        {
            if (canLogin) Login();
        }
        private void Login()
        {
            if(GameCanvas.currentScreen is LoginScr || GameCanvas.currentScreen is ServerListScreen)
            {
                if(GameCanvas.loginScr == null)
                {
                    GameCanvas.loginScr = new LoginScr();
                }
                GameCanvas.loginScr.switchToMe();
                if (mSystem.currentTimeMillis() - timeWait >= 15000L)
                {
                    timeWait = mSystem.currentTimeMillis();
                    if (mSystem.currentTimeMillis() - timeLogin >= 2000L)
                    {
                        timeLogin = mSystem.currentTimeMillis();
                        if (GameCanvas.currentScreen is LoginScr)
                            GameCanvas.loginScr.doLogin();
                    }
                }
            }
        }
    }
}
