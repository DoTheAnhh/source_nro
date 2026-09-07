package nro.entity.boss.map.yardart;

/*
 * @Author: DoTheAnh
 */

import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.YARDART;
import nro.entity.boss.BossesData;

public class TapSu1 extends Yardart {

    public TapSu1() throws Exception {
        super(YARDART, BossID.TAP_SU_1, BossesData.TAP_SU_1);
    }

    @Override
    protected void init() {
        x = 376;
        x2 = 446;
        y = 456;
        y2 = 432;
        range = 1000;
        range2 = 150;
        timeHoiHP = 30000;
        rewardRatio = 5;
    }
}






