package nro.entity.player;

import nro.server.Manager;
import nro.service.MapService;
import nro.service.PlayerService;
import nro.service.Service;
import nro.core.consts.ConstPlayer;
import nro.entity.map.Map;
import nro.entity.map.Zone;

/**
 * @author DoTheAnh
 */

public class TestDame extends Player {

    public void initTestDame() {
        init();
    }

    @Override
    public short getHead() {
        return 83;
    }

    @Override
    public short getBody() {
        return 84;
    }

    @Override
    public short getLeg() {
        return 85;
    }

    public void joinMap(Zone z, Player player) {
        MapService.gI().goToMap(player, z);
        z.load_Me_To_Another(player);
    }
    /**
     * Báo lên đầu POPO thời gian nó vừa bị choáng.
     *
     * <p>Sát thương thì client tự hiện số bay lên, nhưng thời gian choáng thì
     * không hiện ở đâu cả — mà đó lại đúng là thứ cần đo khi chỉnh set kéo dài
     * choáng. Không có dòng này thì chỉ còn cách bấm đồng hồ bằng mắt.</p>
     *
     * <p>In cả số giây lẻ: hiệu ứng set cộng theo phần trăm nên hay ra số như
     * {@code 6.4s}, làm tròn về giây thì không phân biệt được 100% với 110%.</p>
     */
    public void baoChoang(long mili, String chieu) {
        Service.gI().chat(this, "Choáng " + (mili / 1000.0) + "s — " + chieu);
    }

    public void changeToTypePK() {
        PlayerService.gI().changeAndSendTypePK(this, ConstPlayer.PK_ALL);
    }
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
    }
    
    @Override
    public void update() {
        super.update();
        active();
        if (this.isDie()) {
            Service.getInstance().sendMoney(this);
            PlayerService.gI().hoiSinh(this);
            PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax / 100, 1);
            Service.getInstance().hsChar(this, this.nPoint.hpMax, this.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMp(this);
        }
    }
   
    private void init() {
        for (Map m : Manager.MAPS) {
            switch (m.mapId) {
                case 169:
                    for (Zone z : m.zones) {
                        TestDame pl = new TestDame();
                        pl.name = "POPO";
                        pl.gender = 0;
                        pl.id = -15122007;
                        pl.nPoint.hpMax = 2_000_000_000;
                        pl.nPoint.hpg = 2_000_000_000;
                        pl.nPoint.hp = 2_000_000_000;
                        pl.nPoint.setFullHpMp();
                        pl.location.x = 500;
                        pl.location.y = 384;
                        joinMap(z, pl);
                        z.setTestDame(pl);
                    }   break;
                case 170:
                    for (Zone z : m.zones) {
                        TestDame pl = new TestDame();
                        pl.name = "POPO";
                        pl.gender = 1;
                        pl.id = -15082007;
                        pl.nPoint.hpMax = 2_000_000_000;
                        pl.nPoint.hpg = 2_000_000_000;
                        pl.nPoint.hp = 2_000_000_000;
                        pl.nPoint.setFullHpMp();
                        pl.location.x = 500;
                        pl.location.y = 384;
                        joinMap(z, pl);
                        z.setTestDame(pl);
                    }   break;
                case 171:
                    for (Zone z : m.zones) {
                        TestDame pl = new TestDame();
                        pl.name = "POPO";
                        pl.gender = 2;
                        pl.id = -15122008;
                        pl.nPoint.hpMax = 2_000_000_000;
                        pl.nPoint.hpg = 2_000_000_000;
                        pl.nPoint.hp = 2_000_000_000;
                        pl.nPoint.setFullHpMp();
                        pl.location.x = 500;
                        pl.location.y = 384;
                        joinMap(z, pl);
                        z.setTestDame(pl);
                    }   break;
                default:
                    break;
            }
        }
    }
}
