package nro.entity.boss.map.yardart;

/*
 * @Author: DoTheAnh
 */

import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.YARDART;
import nro.entity.boss.BossesData;

public class TanBinh0 extends Yardart {

    public TanBinh0() throws Exception {
        super(YARDART, BossID.TAN_BINH_0, BossesData.TAN_BINH_0);
    }

    @Override
    protected void init() {
        x = 170;
        x2 = 240;
        y = 456;
        y2 = 456;
        range = 1000;
        range2 = 150;
        timeHoiHP = 25000;
        rewardRatio = 4;
    }
}






