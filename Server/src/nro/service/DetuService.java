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
        player.Detu = pet;
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
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false, false );
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Oa oa oa...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

    public void createMabuPetByGender(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false, false ,(byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Oa oa oa...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
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
        byte limitPower = 0;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createMabuPetByGender(player, gender, limitPower);
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
        player.Detu = pet;
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
        pl.Duongtang.name = "Đường tăng";
        pl.Duongtang.gender = pl.gender;
        pl.Duongtang.nPoint.tiemNang = 1;
        pl.Duongtang.nPoint.power = 1;
        pl.Duongtang.nPoint.limitPower = 1;
        pl.Duongtang.nPoint.hpg = 1_000_000;
        pl.Duongtang.nPoint.mpg = 1_000_000;
        pl.Duongtang.nPoint.hp = 1_000_000;
        pl.Duongtang.nPoint.mp = 1_000_000;
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
