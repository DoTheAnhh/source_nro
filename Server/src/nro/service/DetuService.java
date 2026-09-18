package nro.service;

import nro.core.log.Logger;
import nro.service.inventory.InventoryService;
import nro.service.item.ItemService;
import nro.core.consts.ConstPlayer;
import nro.entity.player.PetFollow;
import nro.entity.player.Detu;
import nro.entity.player.Player;
import nro.service.fun.ChangeMapService;
import nro.core.util.SkillUtil;
import nro.core.util.Util;
import nro.core.consts.ConstDetu;
import nro.entity.player.DuongTang;

public class DetuService {

    private static DetuService i;

    public static DetuService gI() {
        if (i == null) {
            i = new DetuService();
        }
        return i;
    }
//-------------------------------BOTTTTTTTTTTTTTTTTTTTTTTTTTT-------------------
    // =====================================================================
    //  Chỉ số gốc của đệ tử
    // =====================================================================
    //
    // Không còn bảng số nào ở đây. Trần chỉ số, bậc thang theo bản đồ và hệ số
    // từng loại đệ đều nằm trong CSDL, sửa ở tab "Đệ tử" trên panel.
    //
    // DeTuDAO.damBaoBang() điền sẵn bảng lúc tạo, và ba hàm đọc của nó không bao
    // giờ trả null — nên chỗ này không cần bản dự phòng. Trước đây cùng một con
    // số nằm ở hai chỗ, và chỉ cần sửa một bên là sửa trên panel không thấy đổi
    // gì mà không ai lần ra vì sao.


    private void CheckPlayer(Player player) {
        if (player == null || player.Detu == null || player.Detu.nPoint == null) {
            return;
        }
        byte loai = player.Detu.typeDeTu;
        nro.repository.dao.DeTuDAO.ChiSo cs = nro.repository.dao.DeTuDAO.chiSo();
        nro.repository.dao.DeTuDAO.Loai lo = nro.repository.dao.DeTuDAO.loai(loai);
        double heSo = lo.heSo / 100d;

        // Day la chi so DE SO SINH — de moi ra thi yeu, nguoi choi nang len bang
        // tiem nang, va tran nang toi dau thi bang de_tu_chi_so quyet dinh.
        //
        // HP va KI boc RIENG nhau chu khong dung cung mot so: hai chi so giong
        // het nhau tung con nhin ra ngay la may moc.
        int hp = nhan(boc(cs.ssHpMin, cs.ssHpMax), heSo, cs.maxHp);
        int ki = nhan(boc(cs.ssHpMin, cs.ssHpMax), heSo, cs.maxHp);
        int dame = nhan(boc(cs.ssDameMin, cs.ssDameMax), heSo, cs.maxDame);
        int giap = nhan(boc(cs.ssGiapMin, cs.ssGiapMax), heSo, cs.maxGiap);
        int crit = nhan(boc(cs.ssCritMin, cs.ssCritMax), heSo, cs.maxCrit);

        player.Detu.nPoint.hpg = hp;
        player.Detu.nPoint.hp = hp;
        player.Detu.nPoint.hpMax = hp;
        player.Detu.nPoint.mpg = ki;
        player.Detu.nPoint.mp = ki;
        player.Detu.nPoint.mpMax = ki;
        player.Detu.nPoint.dameg = dame;
        player.Detu.nPoint.dame = dame;
        player.Detu.nPoint.defg = giap;
        player.Detu.nPoint.def = giap;
        player.Detu.nPoint.critg = crit;
        player.Detu.nPoint.crit = crit;
        player.Detu.nPoint.stamina = 1000;
        player.Detu.nPoint.maxStamina = 1000;

        // Loai tu trung co suc manh khoi diem rieng.
        if (lo.tuTrung && lo.sucManhDau > 0) {
            player.Detu.nPoint.power = lo.sucManhDau;
        } else if (cs.ssSmMax > cs.ssSmMin) {
            player.Detu.nPoint.power = Util.nextLong(cs.ssSmMin, cs.ssSmMax);
        } else {
            player.Detu.nPoint.power = Math.max(1L, cs.ssSmMin);
        }
    }

    /** Ba loại nhận từ trứng — có sức mạnh khởi điểm riêng. */
    public static boolean laDeTuTrung(byte loai) {
        return loai == ConstDetu.MABU || loai == ConstDetu.CELL
                || loai == ConstDetu.BILL;
    }

    /**
     * Bốc một số trong khoảng, chịu được khoảng ngược hoặc rỗng.
     *
     * <p>{@code max <= min} thì trả về {@code min} thay vì nổ: bảng do người gõ,
     * và một dòng gõ ngược không được làm sập việc tạo đệ.</p>
     */
    private static int boc(int min, int max) {
        if (min < 0) {
            min = 0;
        }
        if (max <= min) {
            return min;
        }
        return Util.nextInt(min, max);
    }

    /**
     * Nhân hệ số loại đệ, chặn ở trần <b>đã nhân theo loại</b>.
     *
     * <h3>Trần là của đệ thường</h3>
     *
     * <p>Bốn con số trần là trần của <b>đệ thường</b>. Mabư, Cell, Bill được vượt
     * đúng bằng hệ số của loại: Bill lên tới 600.000 × 1,157625 ≈ 694.575 HP. Nếu
     * chặn ở trần chung thì con Bill bốc đẹp bị cắt về đúng bằng con thường bốc
     * đẹp, và cả hệ thống loại đệ thành vô nghĩa ở mức cao nhất — chính chỗ mà
     * người chơi để ý nhất.</p>
     */
    private static int nhan(int v, double heSo, int tran) {
        long ra = Math.round(v * heSo);
        long tranTheoLoai = Math.round(tran * heSo);
        if (ra > tranTheoLoai) {
            ra = tranTheoLoai;
        }
        return (int) ra;
    }

    /**
     * Tạo đệ tử từ một quả trứng.
     *
     * <p>Ba loại {@code MABU}, {@code CELL}, {@code BILL} đều đi qua đây; loại nào
     * cũng khởi điểm 1,5 triệu sức mạnh và chỉ số bốc theo bản đồ, chỉ khác hệ số
     * nhân của loại.</p>
     *
     * <p>Chạy trong luồng riêng và ngủ một giây trước khi chào, y như hai hàm cũ:
     * gói thông tin đệ phải kịp tới client, không thì câu chào hiện trước cả con
     * đệ.</p>
     */
    public void createBotTuTrung(Player player, byte loai, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, loai);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu,
                        loai == ConstDetu.BILL ? "Ta là Bill, ngươi gọi ta?"
                        : (loai == ConstDetu.CELL ? "Hử... ta đã tỉnh."
                                : "Oa oa oa..."));
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    public void createBotTuTrungByGender(Player player, byte loai, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, loai, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu,
                        loai == ConstDetu.BILL ? "Ta là Bill, ngươi gọi ta?"
                        : (loai == ConstDetu.CELL ? "Hử... ta đã tỉnh."
                                : "Oa oa oa..."));
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    public void createNormalBot(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Xin hãy thu nhận con làm đệ tử");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }
    /**
     * Giu lai cho tuong thich: cac cho goi cu van dung ham nay cho de Mabu.
     */
    public void createMabuBot(Player player, byte... limitPower) {
        createBotTuTrung(player, ConstDetu.MABU, limitPower);
    }
    private void createNewBot(Player player, boolean isMabu, byte... gender) {
        createNewBot(player, (byte) (isMabu ? ConstDetu.MABU : ConstDetu.NORMAL),
                gender);
    }

    /**
     * Dựng một đệ tử mới thuộc {@code loai}, <b>thay hẳn</b> đệ đang có.
     *
     * <h3>Dùng trứng là reset</h3>
     *
     * <p>Gán {@code player.Detu = pet} ở cuối là con đệ cũ mất luôn — đúng thiết
     * kế: mỗi quả trứng cho một con đệ mới, không cộng dồn vào con cũ. Không có
     * bước hỏi lại ở đây; chỗ gọi (vật phẩm trứng) phải tự hỏi trước khi gọi.</p>
     */
    private void createNewBot(Player player, byte loai, byte... gender) {
        boolean isMabu = DetuService.laDeTuTrung(loai);
        int[] data = isMabu ?  getDataPetMabu(): getDataPetNormal();
        Detu pet = new Detu(player);
        pet.name = "$" + ConstDetu.tenLoai(loai);
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 1_000_000;
        pet.nPoint.power = isMabu ? ConstDetu.SUC_MANH_TRUNG : 2000;
        pet.typeDeTu = loai;
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int j = 0; j < ConstPlayer.QTY_MAX_ITEM_BODY_PET ; j++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ConstPlayer.QTY_MAX_SKILL_PET; j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        thayDe(player, pet);
    }
    // =====================================================================
    //  De tu no ra tu trung — danh cho NGUOI CHOI
    // =====================================================================
    /**
     * Nở một quả trứng thành đệ tử thuộc {@code loai}.
     *
     * <h3>Khác hàm dành cho bot ở chỗ nào</h3>
     *
     * <p>{@code createBotTuTrung} dựng đệ cho <b>bot</b>: id âm, không có ô
     * trang bị dành cho người chơi. Đệ của người chơi phải đi qua
     * {@code Player.setIdForPet} và có đủ ô đồ, không thì mặc đồ cho đệ là
     * tràn mảng.</p>
     *
     * <h3>Chỉ số vẫn là chỉ số trên panel</h3>
     *
     * <p>Dựng xong thì gọi {@code CheckPlayer} — đúng bước mà đệ bot đi — nên
     * HP, sức đánh, giáp, chí mạng và sức mạnh khởi điểm đều bốc từ
     * {@code de_tu_chi_so} rồi nhân hệ số của dòng loại trong
     * {@code de_tu_loai}. Ở đây không có con số nào viết cứng.</p>
     *
     * <p>Chạy trong luồng riêng và ngủ một giây trước khi chào: gói thông tin
     * đệ phải kịp tới client, không thì câu chào hiện trước cả con đệ.</p>
     */
    public void taoDeTuTuTrung(Player player, byte loai, byte hanhTinh) {
        new Thread(() -> {
            try {
                dungDeTuChoNguoiChoi(player, loai, hanhTinh);
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu,
                        loai == ConstDetu.BILL ? "Ta là Bill, ngươi gọi ta?"
                        : (loai == ConstDetu.CELL ? "Hử... ta đã tỉnh."
                                : "Oa oa oa..."));
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    /**
     * Dựng con đệ mới cho người chơi, <b>thay hẳn</b> con đang có.
     *
     * <p>Không hỏi lại ở đây; chỗ gọi (vật phẩm trứng) đã hỏi rồi.</p>
     */
    private void dungDeTuChoNguoiChoi(Player player, byte loai, byte hanhTinh) {
        Detu pet = new Detu(player);
        pet.name = "$" + ConstDetu.tenLoai(loai);
        // Hanh tinh do NGUOI CHOI chon luc no trung; ngoai khoang 0..2 thi boc.
        pet.gender = (hanhTinh >= 0 && hanhTinh <= 2)
                ? hanhTinh : (byte) Util.nextInt(0, 2);
        pet.id = Player.setIdForPet(pet, player.id);
        pet.typeDeTu = loai;
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        for (int j = 0; j < ConstPlayer.QTY_MAX_ITEM_BODY_PET; j++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ConstPlayer.QTY_MAX_SKILL_PET; j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        thayDe(player, pet);
    }

    /**
     * Đặt con đệ mới thay con đang có, và bắt client vẽ lại <b>ngay</b>.
     *
     * <h3>Vì sao trước đây phải tách/hợp thể mới thấy đệ mới</h3>
     *
     * <p>Đệ mới mang <b>cùng id</b> với đệ cũ, mà client khi nhận gói thêm nhân
     * vật (-5) thì bỏ qua nếu trên map đã có nhân vật trùng id. Nở trứng không
     * hề gỡ đệ cũ khỏi map, nên con cũ nằm đó và gói của con mới bị bỏ. Hợp thể
     * thì gỡ đệ (-6), tách ra thì thêm lại — lúc ấy mới ra hình mới.</p>
     *
     * <h3>Thứ tự</h3>
     *
     * <ol>
     *   <li>Đang hợp thể với con cũ thì tách, cho sư phụ về lại hình mình.</li>
     *   <li>Gỡ con cũ khỏi map và huỷ nó.</li>
     *   <li>Gửi -6 cho id đệ xuống cả khu — kể cả khi máy chủ tưởng con cũ đã
     *       đi rồi (đường Satan gỡ trước, nhưng một lượt update dở dang có thể
     *       đã đưa nó quay lại).</li>
     *   <li><b>Rồi mới</b> gán đệ mới: vòng update của sư phụ thấy đệ chưa có
     *       zone sẽ tự {@code joinMapMaster()}, gói -5 tới sau gói -6 nên client
     *       dựng nhân vật mới với hình mới.</li>
     * </ol>
     */
    private void thayDe(Player player, Detu moi) {
        Detu cu = player.Detu;
        if (cu != null && cu != moi) {
            try {
                if (player.fusion != null
                        && player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                    cu.unFusion();
                }
                ChangeMapService.gI().exitMap(cu);
                cu.dispose();
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }
        xoaDeTrenClient(player, moi.id);
        player.Detu = moi;
        // Bang de tu / khung de tren client giu hinh va chi so cua con cu cho
        // toi lan hoi sau — gui luon ban cua con moi.
        Service.gI().showInfoPet(player);
    }

    /** Gửi gói xoá nhân vật (-6) cho id đệ tới mọi người trong khu của sư phụ. */
    private static void xoaDeTrenClient(Player player, long idDe) {
        if (player.zone == null) {
            return;
        }
        nro.net.io.Message msg = null;
        try {
            msg = new nro.net.io.Message(-6);
            msg.writer().writeInt((int) idDe);
            Service.gI().sendMessAllPlayerInMap(player.zone, msg);
        } catch (Exception e) {
            Logger.logException(DetuService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

//-------------------------------CREATE DETU------------------------------------
    public void createNormalPetByGender(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false,false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Xin hãy thu nhận con làm đệ tử");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    public void createNormalPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Xin hãy thu nhận con làm đệ tử");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    public void createMabuPet(Player player, byte... limitPower) {
        createBotTuTrung(player, ConstDetu.MABU, limitPower);
    }

    public void createMabuPetByGender(Player player, int gender, byte... limitPower) {
        createBotTuTrungByGender(player, ConstDetu.MABU, gender, limitPower);
    }
    
    public void createUbuPet(Player player) {
        new Thread(() -> {
            try {
                createNewPet(player, false, true, false, false, false, (byte) 0);
                player.Detu.nPoint.limitPower = 13;
                player.Detu.nPoint.initPowerLimit();
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }
    
    public void createKidjirenPet(Player player) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, true, false,false, (byte) 1);
                player.Detu.nPoint.limitPower = 13;
                player.Detu.nPoint.initPowerLimit();
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }
    
    public void createKidbeerPet(Player player) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, true,false, (byte) 2);
                player.Detu.nPoint.limitPower = 13;
                player.Detu.nPoint.initPowerLimit();
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }
public void createBlackPet(Player player) {
    new Thread(() -> {
        try {
            createNewPet(player, false, false, false, false, true, (byte) player.gender);
            player.Detu.nPoint.limitPower = 13;
            player.Detu.nPoint.initPowerLimit();
            Thread.sleep(1000);
            Service.gI().chatJustForMe(player, player.Detu, "...");
        } catch (Exception e) {
            Logger.logException(DetuService.class, e);
        }
    }).start();
}

//-----------------------------------CHANGE DETU--------------------------------
    public void changeNormalPet(Player player, int gender) {
        byte limitPower = player.Detu.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu= null;
        createNormalPetByGender(player, gender, limitPower);
    }

    public void changeNormalPet(Player player) {
        byte limitPower = player.Detu.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu= null;
        createNormalPet(player, limitPower);
    }

    public void changeMabuPet(Player player) {
        byte limitPower = player.Detu.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createMabuPet(player, limitPower);
    }

    public void changeMabuPet(Player player, int gender) {
        byte limitPower = player.Detu.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createBotTuTrungByGender(player, ConstDetu.MABU, gender, limitPower);
    }
    
    public void changeUbuPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createUbuPet(player);
    }

    public void changeKidjirenPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createKidjirenPet(player);
    }

    public void changeKidbeerPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createKidbeerPet(player);
    }
    
     public void changeBlackPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createBlackPet(player);
    }
//---------------------------NAME-----------------------------------------------
    public void changeNamePet(Player player, String name) {
        try {
            if (!InventoryService.gI().isExistItemBag(player, 400)) {
                Service.gI().sendThongBao(player, "Bạn cần thẻ đặt tên đệ tử, mua tại Santa");
                return;
            } else if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBao(player, "Tên không được chứa ký tự đặc biệt");
                return;
            } else if (name.length() > 10) {
                Service.gI().sendThongBao(player, "Tên quá dài");
                return;
            }
            ChangeMapService.gI().exitMap(player.Detu);
            player.Detu.name = "$" + name.toLowerCase().trim();
            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 400), 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.gI().chatJustForMe(player, player.Detu, "Cảm ơn sư phụ đã đặt cho con tên " + name);
                } catch (Exception e) {
                    Logger.logException(DetuService.class, e);
                }
            }).start();
        } catch (Exception ex) {

        }
    }
//---------------------------DATA DETU------------------------------------------
    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(20, 45); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(20, 45); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }
     private int[] getDataPetBlackGoku() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(20, 45); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }
    
    private int[] getDataPetNew() {
        int[] petData = new int[5];
        petData[0] = 700000;
        petData[1] = 700000;
        petData[2] = 35000;
        petData[3] = 500;
        petData[4] = Util.nextInt(0, 2);
        for (int j = 0; j < 3; j++) {
            petData[j] += (petData[j] * 30 / 100);
        }
        return petData;
    }
    
    private void createNewPet(Player player, boolean isMabu, boolean isUbu, boolean isKidJiren, boolean isKidBill, boolean isBlackGoku, byte... gender) {
        int[] data = isMabu ? getDataPetMabu() : (isUbu || isKidJiren || isKidBill ) ? getDataPetNew(): isBlackGoku ? getDataPetBlackGoku() : getDataPetNormal();
        Detu pet = new Detu(player);
        pet.name = "$" + (isMabu ? "Mabư" : isUbu ? "Ubu" : isKidJiren ? "Kid jiren" : isKidBill ? "Kid beer" : isBlackGoku ? "Black Goku Rose" : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = Player.setIdForPet(pet, player.id);
        pet.nPoint.power = isMabu  ? 1500000 :( isUbu ||isKidJiren || isKidBill || isBlackGoku) ? 1500000L : 2000;
        pet.typeDeTu = (byte) (isMabu ? 1 : isUbu ? 2 : isKidJiren ? 3 : isKidBill ? 4 : isBlackGoku ? 5: 0);
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int j = 0; j < ((isUbu || isKidJiren || isKidBill || isBlackGoku) ? ConstPlayer.QTY_MAX_ITEM_BODY_PET  : ConstPlayer.QTY_MAX_ITEM_BODY_PET) ; j++) { //ConstPlayer.QTY_MAX_ITEM_BODY_PET +5
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ((isUbu || isKidJiren || isKidBill || isBlackGoku) ? ConstPlayer.QTY_MAX_SKILL_PET + 1 : ConstPlayer.QTY_MAX_SKILL_PET); j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        thayDe(player, pet);
        if (isUbu || isKidJiren || isKidBill) {
            player.pointfusion.setHpFusion(30);
            player.pointfusion.setMpFusion(30);
            player.pointfusion.setDameFusion(30);
        }
        if (isBlackGoku) {
            player.pointfusion.setHpFusion(40);
            player.pointfusion.setMpFusion(40);
            player.pointfusion.setDameFusion(40);
        }
    }
    
    public static void PetFollow(Player pl, int h, int b, int l) {
        if (pl.PetFollow != null) {
            pl.PetFollow.dispose();
        }
        pl.PetFollow = new PetFollow(pl, (short) h, (short) b, (short) l);
        pl.PetFollow.name = "$";
        pl.PetFollow.gender = pl.gender;
        pl.PetFollow.nPoint.tiemNang = 1;
        pl.PetFollow.nPoint.power = 1;
        pl.PetFollow.nPoint.limitPower = 1;
        pl.PetFollow.nPoint.hpg = 500000;
        pl.PetFollow.nPoint.mpg = 500000;
        pl.PetFollow.nPoint.hp = 500000;
        pl.PetFollow.nPoint.mp = 500000;
        pl.PetFollow.nPoint.dameg = 1;
        pl.PetFollow.nPoint.defg = 1;
        pl.PetFollow.nPoint.critg = 1;
        pl.PetFollow.nPoint.stamina = 1;
        pl.PetFollow.nPoint.setBasePoint();
        pl.PetFollow.nPoint.setFullHpMp();
    }
    
    public static void DuongTang(Player pl) {
        if (pl.Duongtang != null) {
            pl.Duongtang.dispose();
        }
        pl.Duongtang = new DuongTang(pl);
        // Ten mang theo nguoi dang ho tong.
        //
        // Ca may chu deu thay Duong Tang cua nguoi khac, nen chi mot chu
        // "Duong tang" thi giua cho dong nguoi khong ai biet ong nao cua ai —
        // ma biet la de tro thanh muc tieu, hoac de tranh ra.
        pl.Duongtang.name = "Đường tăng (" + pl.name + " hộ tống)";
        pl.Duongtang.gender = pl.gender;
        pl.Duongtang.nPoint.tiemNang = 1;
        pl.Duongtang.nPoint.power = 1;
        pl.Duongtang.nPoint.limitPower = 1;
        // Dung mot tram mau, va moi don chi an mot diem (xem
        // DuongTang.injured): ai danh ong ay cung phai bo ra dung mot tram
        // nhat, nen ke pha khong the mot phat ket lieu, ma nguoi ho tong cung
        // khong the lo la.
        pl.Duongtang.nPoint.hpg = 100;
        pl.Duongtang.nPoint.mpg = 100;
        pl.Duongtang.nPoint.hp = 100;
        pl.Duongtang.nPoint.mp = 100;
        pl.Duongtang.nPoint.dameg = 1;
        pl.Duongtang.nPoint.defg = 1;
        pl.Duongtang.nPoint.critg = 1;
        pl.Duongtang.nPoint.stamina = 1;
        pl.Duongtang.nPoint.setBasePoint();
        pl.Duongtang.nPoint.setFullHpMp();
    }
    
    public void deletePet(Player player) {
        Detu pet = player.Detu;
        if (pet != null) {
            if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
                pet.unFusion();
            }
            ChangeMapService.gI().exitMap(pet);
            pet.dispose();
            player.Detu = null;
        }
    }
}
