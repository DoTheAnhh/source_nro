package nro.entity.boss.map.yardart;

/*
 * @Author: DoTheAnh
 */

import nro.entity.boss.BossID;
import static nro.entity.boss.BossType.YARDART;
import nro.entity.boss.BossesData;

public class TanBinh2 extends Yardart {

    public TanBinh2() throws Exception {
        super(YARDART, BossID.TAN_BINH_2, BossesData.TAN_BINH_2);
    }

    @Override
    protected void init() {
        x = 582;
        x2 = 652;
        y = 432;
        y2 = 432;
        range = 1000;
        range2 = 150;
        timeHoiHP = 25000;
        rewardRatio = 4;
    }
}






