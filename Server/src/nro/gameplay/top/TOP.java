package nro.gameplay.top;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TOP {

    private String name;
    private byte gender;
    private short head;
    private short body;
    private short leg;
    private long power;
    private long power_pet;
    private long ki;
    private long hp;
    private long sd;
    private long ki_pet;
    private long hp_pet;
    private long sd_pet;
    private int vnd;
    private int coin;
    private int danap;
    private int thoivang;
    private int hongngoc;
    private byte nv;
    private byte subnv;
    private int time;
    private int top;
    /** Đòn đơn mạnh nhất từng đấm vào máy đo sức mạnh. */
    private long donManhNhat;
    /** Tổng sát thương gom được trong một lượt 30 giây, lấy lượt cao nhất. */
    private long dame30s;
    //
    private int sk;
    private int pvp;
    private int nhs;
    private int dicanh;
    private int divdst;
    private int juventus;
    private long LastTimeLogin;
    
    private int PointNamekWar;
    //EVENT NEW YEAR
    private int TangLixi;
    private int MoLixi;
    private int BanPhaoHoa;
    private int BanPhaoHoaVIP;
    //EVENT CHRIST MAS
    private int TrangtricayNoel;
    private int Chetaonguoituyet;
    private int Chetaonguoituyetbanggia;
    private int Dotdiem;
    //EVENT VULAN
    private int BanPhaoHoaVuLan;
    private int HoaDang;
    private int HoaDangLoiChuc;
    //EVENT HALLOWEEN
    private int MoHopMaQuy;
    private int ThiepHalloween;
    //EVENT INTERNATION_WOMENS_DAY
    private int DuaTopMoThiep_83;
    private int DuaTopTangBongHoaHong;
    //EVENT TRUNG THU
    private int DuaTopLamBanhTrungThu;
    private int DuaTopMoBanhTrungThuDacBiet;
    //EVENT HUNG VUONG
    private int DuaTopMoTrungRongVang;
    private int DuaTopMoHopQuaGioTo;
    private int DuaTopDangBanhHungVuong;
    private int DuaTopDoiDuaHau;
    //BLACKFRIDAY
    private int DuaTopHopQuaBlackFriday;
    private int DuaTopMuaSam;
}





