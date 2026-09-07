package nro.services;

import Utils.Logger;
import nro.inventory.InventoryService;
import models.Item.ItemService;
import consts.ConstPlayer;
import nro.player.PetFollow;
import nro.player.Detu;
import nro.player.Player;
import nro.services.Fun.ChangeMapService;
import Utils.SkillUtil;
import Utils.Util;
import consts.ConstDetu;
import nro.player.DuongTang;

public class DetuService {

    private static DetuService i;

    public static DetuService gI() {
        if (i == null) {
            i = new DetuService();
        }
        return i;
    }

//-------------------------------BOT--------------------------------------------
    //-------------------------------BOTTTTTTTTTTTTTTTTTTTTTTTTTT-------------------
    private void CheckPlayer(Player player) {
        player.Detu.nPoint.defg = Util.nextInt(1, 200);
        player.Detu.nPoint.def = player.Detu.nPoint.defg;
        player.Detu.nPoint.critg = Util.nextInt(0, 5);
        player.Detu.nPoint.crit = player.Detu.nPoint.critg;
        player.Detu.nPoint.stamina = 1000;
        player.Detu.nPoint.maxStamina = 1000;
        if (player.zone != null) {
            int MapId = player.zone.map.mapId;
            switch (MapId) {
                default:
                    player.Detu.nPoint.power = Util.nextInt(2_000, 100_000);
                    player.Detu.nPoint.hpg = Util.nextInt(1_000, 3_000)
                            + ((player.Detu.typeDeTu == ConstDetu.MABU || player.Detu.typeDeTu == 5)
                                    ? (player.Detu.nPoint.hpg * 10 / 100) : 1);
                    player.Detu.nPoint.hp = player.Detu.nPoint.hpg;
                    player.Detu.nPoint.hpMax = player.Detu.nPoint.hpg;
                    player.Detu.nPoint.mpg = Util.nextInt(1_000, 3_000)
                            + ((player.Detu.typeDeTu == ConstDetu.MABU || player.Detu.typeDeTu == 5)
                                    ? (player.Detu.nPoint.hpg * 10 / 100) : 1);
                    player.Detu.nPoint.mp = player.Detu.nPoint.mpg;
                    player.Detu.nPoint.mpMax = player.Detu.nPoint.mpg;
                    player.Detu.nPoint.dameg = Util.nextInt(29, 100)
                            + ((player.Detu.typeDeTu == ConstDetu.MABU || player.Detu.typeDeTu == 5)
                                    ? (player.Detu.nPoint.dameg * 10 / 100) : 1);
                    player.Detu.nPoint.dame = player.Detu.nPoint.dameg;
                    break;
            }
        }
    }

    public void createNormalBot(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, false, false);
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

    public void createMabuBot(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewBot(player, true, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.Detu.nPoint.limitPower = limitPower[0];
                    player.Detu.nPoint.initPowerLimit();
                }
                CheckPlayer(player);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Oa oa oa...");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

//-------------------Hàm gốc tạo thực thể đệ BOT-------------------
    private void createNewBot(Player player, boolean isMabu, boolean isBlack, byte... gender) {
        int[] data = (isMabu || isBlack) ? getDataPetMabu() : getDataPetNormal();
        Detu pet = new Detu(player);
        pet.name = "$" + (isMabu ? "Mabư" : isBlack ? "Black" : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 1_000_000;

        // ✅ 1tr5 SM cho Mabu và Black
        pet.nPoint.power = (isMabu || isBlack) ? 1_500_000 : 2000;
        pet.typeDeTu = (byte) (isMabu ? 1 : isBlack ? 5 : 0);
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int j = 0; j < ConstPlayer.QTY_MAX_ITEM_BODY_PET; j++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ConstPlayer.QTY_MAX_SKILL_PET; j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.Detu = pet;

        // ✅ Reset limit khi tạo
        player.Detu.nPoint.limitPower = 0;
        player.Detu.nPoint.initPowerLimit();
    }

//-------------------------------CREATE DETU------------------------------------
    private void createNewPet(Player player, boolean isMabu, boolean isUbu, boolean isKidJiren, boolean isKidBill, boolean isBlack, byte... gender) {
        // ✅ BU & Black dùng cùng data với Mabu
        int[] data = (isMabu || isUbu || isKidJiren || isKidBill || isBlack) ? getDataPetMabu() : getDataPetNormal();

        Detu pet = new Detu(player);
        pet.name = "$" + (isMabu ? "Mabư"
                : isUbu ? "Ubu"
                        : isKidJiren ? "Kid Jiren"
                                : isKidBill ? "Kid Beer"
                                        : isBlack ? "Black"
                                                : "Đệ tử");
        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);
        pet.id = Player.setIdForPet(pet, player.id);

        // ✅ Power 1tr5 cho Mabu và BU
        pet.nPoint.power = (isMabu || isUbu || isKidJiren || isKidBill || isBlack) ? 1_500_000 : 2000;

        pet.typeDeTu = (byte) (isMabu ? 1
                : isUbu ? 2
                        : isKidJiren ? 3
                                : isKidBill ? 4
                                        : isBlack ? 5 : 0);
        pet.thuctinh = (byte) 0;
        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];
        for (int j = 0; j < ConstPlayer.QTY_MAX_ITEM_BODY_PET; j++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }
        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int j = 0; j < ConstPlayer.QTY_MAX_SKILL_PET; j++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }
        pet.nPoint.setFullHpMp();
        player.Detu = pet;

        // ✅ Reset limit khi tạo
        player.Detu.nPoint.limitPower = 0;
        player.Detu.nPoint.initPowerLimit();
    }

    public void createNormalPetByGender(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false, false, (byte) gender);
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
                createNewPet(player, true, false, false, false, false);
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
                createNewPet(player, true, false, false, false, false, (byte) gender);
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
                createNewPet(player, false, false, true, false, false, (byte) 1);
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
                createNewPet(player, false, false, false, true, false, (byte) 2);
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
                createNewPet(player, false, false, false, false, true, (byte) 1);
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.Detu, "Ta là Black - sức mạnh bóng tối trỗi dậy!");
            } catch (Exception e) {
                Logger.logException(DetuService.class, e);
            }
        }).start();
    }

//-----------------------------------CHANGE DETU--------------------------------
    public void changeNormalPet(Player player, int gender) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createNormalPetByGender(player, gender);
    }

    public void changeNormalPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createNormalPet(player);
    }

    public void changeMabuPet(Player player) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createMabuPet(player);
    }

    public void changeMabuPet(Player player, int gender) {
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.Detu.unFusion();
        }
        ChangeMapService.gI().exitMap(player.Detu);
        player.Detu.dispose();
        player.Detu = null;
        createMabuPetByGender(player, gender);
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
        return new int[]{800, 800, 35, 10, 1};
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; // hp
        petData[1] = Util.nextInt(40, 105) * 20; // mp
        petData[2] = Util.nextInt(20, 45);       // dame
        petData[3] = Util.nextInt(9, 50);        // def
        petData[4] = Util.nextInt(0, 2);         // crit
        return petData;
    }

//---------------------------PET FOLLOW / DUONG TANG----------------------------
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
