package nro.entity.boss.map.yardart;

/*
 * @Author: DoTheAnh
 */

import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.YARDART;
import nro.entity.boss.BossesData;

public class ChienBinh4 extends Yardart {

    public ChienBinh4() throws Exception {
        super(YARDART, BossID.CHIEN_BINH_4, BossesData.CHIEN_BINH_4);
    }

    @Override
    protected void init() {
        x = 993;
        x2 = 1063;
        y = 456;
        y2 = 456;
        range = 1000;
        range2 = 150;
        timeHoiHP = 20000;
        rewardRatio = 3;
    }

}






