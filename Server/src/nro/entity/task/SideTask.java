package nro.entity.task;

import nro.core.util.Util;
import nro.core.consts.ConstTask;

public class SideTask {

    public SideTaskTemplate template;

    public int count;

    public int maxCount;

    public int level;

    public int leftTask;

    public long receivedTime;

    public boolean notify0;
    public boolean notify10;
    public boolean notify20;
    public boolean notify30;
    public boolean notify40;
    public boolean notify50;
    public boolean notify60;
    public boolean notify70;
    public boolean notify80;
    public boolean notify90;

    public void reset() {
        this.template = null;
        this.count = 0;
        this.level = 0;
        this.notify0 = false;
        this.notify10 = false;
        this.notify20 = false;
        this.notify30 = false;
        this.notify40 = false;
        this.notify50 = false;
        this.notify60 = false;
        this.notify70 = false;
        this.notify80 = false;
        this.notify90 = false;
    }

    public SideTask() {
        this.leftTask = soMoiNgay();
    }

    /**
     * Số nhiệm vụ nhận được mỗi ngày — đọc từ cấu hình NRO Pass
     * ({@code so_nv_bo_mong_ngay}) chứ không còn viết cứng: nhiệm vụ Bò Mộng
     * là nguồn điểm pass nên số lượt mỗi ngày quyết định nhịp lên cấp.
     */
    public static int soMoiNgay() {
        return nro.repository.dao.NroPassDAO.soNhiemVuMoiNgay();
    }

    public void renew() {
        if (Util.isAfterMidnight(receivedTime)) {
            this.leftTask = soMoiNgay();
            this.receivedTime = System.currentTimeMillis();
        }
        // Ha tran giua ngay (vd 20 -> 10) thi nguoi dang con nhieu luot cung ve
        // dung tran moi, khong doi toi nua dem.
        if (this.leftTask > soMoiNgay()) {
            this.leftTask = soMoiNgay();
        }
    }

    public boolean isDone() {
        return this.count >= this.maxCount;
    }

    public String getName() {
        if (this.template != null) {
            return this.template.name.replaceAll("%1", String.valueOf(maxCount));
        }
        return "Hiện tại không có nhiệm vụ nào";
    }

    public String getLevel() {
        switch (this.level) {
            case ConstTask.EASY:
                return "dễ";
            case ConstTask.NORMAL:
                return "bình thường";
            case ConstTask.HARD:
                return "khó";
            case ConstTask.VERY_HARD:
                return "rất khó";
            case ConstTask.HELL:
                return "địa ngục";
            default:
                return "DoTheAnh";
        }
    }

    public int getPercentProcess() {
        if (this.count >= this.maxCount) {
            return 100;
        }
        return (int) ((long) count * 100 / maxCount);
    }
}