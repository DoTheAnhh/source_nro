using System.Collections.Generic;

namespace Game4.God
{
    /*Author: HAIRMOD*/
    public class Items : IActionListener, IChatable
    {
        private static Items instance { get; set; }
        public static Items getInstance()
        {
            return (instance == null) ? (instance = new Items()) : instance;
        }
        private List<ItemGroup> itemUses = new List<ItemGroup>();
        private List<ItemGroup> itemBuys = new List<ItemGroup>();
        private ItemGroup listUses;
        private ItemGroup listBuys;
        private string[] inputUses;
        private string[] inputBuys;
        private long timeUses, timeBuys;
        private int quantity;
        // ---------------- Dùng nhanh ----------------
        private string[] inputNhanh;
        private ItemGroup nhomNhanh;
        private int soLanConLai;
        private int idNhanh = -1;
        private short iconNhanh = -1;
        private long lucDungCuoi;
        private int soLuongTruoc = -1;
        private int soLanKhongGiam;

        /// <summary>Cách nhau bao lâu giữa hai lần dùng, tính bằng mili giây.</summary>
        /// <remarks>
        /// Máy chủ trừ vật phẩm rồi mới gửi lại hành trang; bấm dồn dập hơn nhịp
        /// này thì lần sau đọc số lượng vẫn là số cũ, tưởng dùng không được.
        /// </remarks>
        private const long NHIP_DUNG = 600L;

        /// <summary>Bao nhiêu lần dùng mà số lượng không giảm thì coi là dừng.</summary>
        /// <remarks>
        /// Đây là cách nhận ra <b>đã full cấp</b> mà không phải biết trước từng
        /// loại vật phẩm: dùng ở mức tối đa thì máy chủ từ chối và không trừ món
        /// nào. Cho hai lần cho chắc, kẻo một gói hành trang về muộn là dừng oan.
        /// </remarks>
        private const int TOI_DA_KHONG_GIAM = 2;

        public Items()
        {
            inputUses = new string[2] { "Tự Động Sử Dụng Vật Phẩm", "Nhập Thời Gian Sử Dụng (Giây)"};
            inputBuys = new string[2] { "Tự Động Mua Vật Phẩm", "Nhập Số Lượng Vật Phẩm Cần Mua"};
            inputNhanh = new string[2] { "Dùng Nhanh Vật Phẩm", "Nhập Số Lượng Muốn Dùng"};
        }

        /// <summary>Hỏi số lượng rồi bắt đầu dùng nhanh.</summary>
        private void batDauNhanh(ItemGroup groupItem)
        {
            GameCanvas.panel.hide();
            Utils.startChat(this, inputNhanh[0], inputNhanh[1], TField.INPUT_TYPE_NUMERIC);
            this.nhomNhanh = groupItem;
        }

        /// <summary>Món này còn trong hành trang không, và còn bao nhiêu.</summary>
        private static Item timTrongTui(int id)
        {
            foreach (var it in Char.myCharz().arrItemBag)
            {
                if (it != null && it.template != null && it.template.id == id)
                {
                    return it;
                }
            }
            return null;
        }

        /// <summary>Hiệu lực của món này còn đang chạy không.</summary>
        /// <remarks>
        /// Cuồng nộ và các món có thời gian tồn tại: dùng viên thứ hai lúc viên
        /// đầu còn hiệu lực là mất trắng viên đó. Nên chờ đồng hồ trên màn hình
        /// hết mới dùng tiếp — đó chính là danh sách <c>Char.vItemTime</c>.
        /// </remarks>
        private static bool buffConChay(short icon)
        {
            if (icon < 0)
            {
                return false;
            }
            for (int i = 0; i < Char.vItemTime.size(); i++)
            {
                ItemTime t = (ItemTime)Char.vItemTime.elementAt(i);
                if (t != null && t.idIcon == icon)
                {
                    return true;
                }
            }
            return false;
        }

        private void ketThucNhanh(string cau)
        {
            soLanConLai = 0;
            idNhanh = -1;
            iconNhanh = -1;
            soLuongTruoc = -1;
            soLanKhongGiam = 0;
            GameScr.info1.addInfo(cau, 0);
        }

        /// <summary>Mỗi khung hình: dùng thêm một món nếu tới lượt.</summary>
        private void dungNhanh()
        {
            if (soLanConLai <= 0 || idNhanh < 0)
            {
                return;
            }
            Item mon = timTrongTui(idNhanh);
            if (mon == null)
            {
                ketThucNhanh("Đã dùng hết vật phẩm");
                return;
            }
            // Con hieu luc thi cho, KHONG tinh la mot lan that bai.
            if (buffConChay(iconNhanh))
            {
                return;
            }
            if (mSystem.currentTimeMillis() - lucDungCuoi < NHIP_DUNG)
            {
                return;
            }
            if (soLuongTruoc >= 0)
            {
                if (mon.quantity >= soLuongTruoc)
                {
                    soLanKhongGiam++;
                    if (soLanKhongGiam >= TOI_DA_KHONG_GIAM)
                    {
                        ketThucNhanh("Không dùng thêm được (có thể đã đạt mức tối đa)");
                        return;
                    }
                }
                else
                {
                    soLanKhongGiam = 0;
                }
            }
            soLuongTruoc = mon.quantity;
            lucDungCuoi = mSystem.currentTimeMillis();
            Utils.UseItem(idNhanh);
            soLanConLai--;
            if (soLanConLai <= 0)
            {
                ketThucNhanh("Dùng nhanh: xong");
            }
        }

        private void autoUse()
        {
            if (itemUses == null || itemUses.Count == 0) return;
            foreach(var item in itemUses)
            {
                Utils.useItemWithTime(item.id, timeUses * 1000L);
            }
        }
        private void autoBuy()
        {
            if (itemBuys == null && itemBuys.Count == 0) return;
            foreach (var item in itemBuys)
            {
                if (quantity == 0)
                {
                    itemBuys.Remove(item);
                    GameScr.info1.addInfo("Xong!", 0);
                    break;
                }
                while (quantity > 0 && mSystem.currentTimeMillis() - timeBuys >= 200L)
                {
                    Service.gI().buyItem((sbyte)((item.buyGold) ? 0 : 1), item.id, 0);
                    quantity--;
                }
            }
        }
        private void addGroupItemToListUse(ItemGroup groupItem)
        {
            GameCanvas.panel.hide();
            Utils.startChat(this, inputUses[0], inputUses[1], TField.INPUT_TYPE_NUMERIC);
            this.listUses = groupItem;
        }
        private void addGroupItemToListBuy(ItemGroup groupItem)
        {
            NhatKy.ghi("MUA NHIEU: vao addGroupItemToListBuy");
            GameCanvas.panel.hide();
            Utils.startChat(this, inputBuys[0], inputBuys[1], TField.INPUT_TYPE_NUMERIC);
            this.listBuys = groupItem;
        }
        public bool checkContains(int id)
        {
            if (itemUses.Count == 0) return false;
            foreach (var item in itemUses)
            {
                if (item.id == id)
                {
                    return true;
                }
            }
            return false;
        }
        private void removeItem(int id)
        {
            for (int i = 0; i < itemUses.Count; i++)
            {
                if (itemUses[i].id == id)
                {
                    itemUses.RemoveAt(i);
                    break;
                }
            }
        }
        /// <summary>
        /// Nhận chuỗi người chơi vừa gõ.
        /// </summary>
        /// <remarks>
        /// Đọc tham số <paramref name="text"/>, <b>không</b> đọc
        /// <c>ChatTextField.tfChat.getText()</c>.
        ///
        /// Ô nhập giờ là <c>HopNhapChu</c>, nó đưa chuỗi vừa gõ xuống qua
        /// tham số này; ô <c>tfChat</c> cũ không còn ai gõ vào nên luôn rỗng.
        /// Đọc nhầm chỗ thì <c>int.Parse("")</c> ném lỗi ngay dòng đầu —
        /// bấm OK là hộp đóng và không mua gì cả, đúng triệu chứng đã gặp.
        /// </remarks>
        public void onChatFromMe(string text, string to)
        {
            ChatTextField chatTextField = ChatTextField.gI();
            int so;
            if (!int.TryParse((text == null) ? string.Empty : text.Trim(),
                    out so))
            {
                // Go chu vao o so thi bao roi thoi, dung nem loi giua luc ve.
                GameScr.info1.addInfo("Phải nhập một con số", 0);
                Utils.resetTF();
                return;
            }
            if (chatTextField.strChat.Equals(inputUses[0]))
            {
                int time = so;
                timeUses = time;
                GameScr.info1.addInfo($"Delay: {time} giây", 0);
                itemUses.Add(listUses);
                Utils.resetTF();
                return;
            }
            else if (chatTextField.strChat.Equals(inputNhanh[0]))
            {
                int soLan = so;
                // ItemGroup la struct, khong so duoc voi null. Mon chua chon thi
                // id van la 0 vi struct mac dinh moi truong bang 0.
                if (soLan <= 0 || nhomNhanh.id <= 0)
                {
                    Utils.resetTF();
                    return;
                }
                idNhanh = nhomNhanh.id;
                Item mon = timTrongTui(idNhanh);
                iconNhanh = (mon == null || mon.template == null)
                        ? (short)-1 : (short)mon.template.iconID;
                // Khong cho go nhieu hon so dang co: go 999 khi chi co 5 thi
                // vong lap phai cho tan het roi moi tu dung, nhin nhu treo.
                if (mon != null && soLan > mon.quantity)
                {
                    soLan = mon.quantity;
                }
                soLanConLai = soLan;
                soLuongTruoc = -1;
                soLanKhongGiam = 0;
                lucDungCuoi = 0L;
                GameScr.info1.addInfo("Dùng nhanh " + soLan + " món", 0);
                Utils.resetTF();
                return;
            }
            else if (chatTextField.strChat.Equals(inputBuys[0]))
            {
                int num = so;
                quantity = num;
                GameScr.info1.addInfo($"Số Lượng: {num}", 0);
                itemBuys.Add(listBuys);
                Utils.resetTF();
                return;
            }
        }
        public void Update()
        {
            autoBuy();
            autoUse();
            dungNhanh();
        }
        public void perform(int idAction, object p)
        {
            switch (idAction)
            {
                case 1:
                    addGroupItemToListUse((ItemGroup)p);
                    break;
                case 2:
                    GameScr.info1.addInfo("Đã xóa ra khỏi danh sách", 0);
                    short id = (short)p;
                    removeItem(id);
                    break;
                case 3:
                    addGroupItemToListBuy((ItemGroup)p);
                    break;
            }
        }
        public void onCancelChat() => Utils.resetTF();
    }
}
