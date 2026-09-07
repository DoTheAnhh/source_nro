# -*- coding: utf-8 -*-
"""
Dung lai file SQL chuan cho project hien tai.

CACH LAM
--------
1. Liet ke moi bang trong CSDL.
2. Doi chieu voi cac cau SQL THAT trong src/ (FROM/JOIN/INTO/UPDATE <ten bang>)
   — khong dem theo ten tran, vi "player"/"clan"/"event" cung la ten bien Java.
3. Bang khong xuat hien trong cau SQL nao -> KHONG dua vao file chuan, va ghi
   ro ra o dau file de nguoi doc tu quyet dinh.
4. Bang du lieu do NGUOI CHOI sinh ra -> chi lay cau truc, khong lay dong.
   File chuan la de dung server moi, khong phai ban sao luu.
"""
import os
import re
import subprocess
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

MYSQL = "C:/xampp/mysql/bin/mysql.exe"
DUMP = "C:/xampp/mysql/bin/mysqldump.exe"
DB = "awnv3"
SRC = "src"
RA = "sql/awnv3-chuan.sql"

# Bang do nguoi choi sinh ra: lay cau truc, bo dong.
CHI_CAU_TRUC = {
    "account", "player", "clan",
    "giftcode_save", "shop_ky_gui",
    "history_bank", "history_gold", "history_receive_goldbar",
    "history_transaction",
}


def chay(args):
    r = subprocess.run(args, capture_output=True)
    if r.returncode != 0:
        raise SystemExit("Loi chay %s:\n%s" % (args[0], r.stderr.decode('utf-8', 'replace')))
    return r.stdout.decode('utf-8', 'replace')


def cac_bang():
    out = chay([MYSQL, "-u", "root", DB, "-N", "-e",
                "SELECT table_name FROM information_schema.tables "
                "WHERE table_schema='%s' ORDER BY table_name" % DB])
    return [t.strip() for t in out.splitlines() if t.strip()]


def nguon_java():
    noi_dung = []
    for goc, _, tep in os.walk(SRC):
        for f in tep:
            if f.endswith(".java"):
                with open(os.path.join(goc, f), encoding='utf-8', errors='replace') as fh:
                    noi_dung.append(fh.read())
    return "\n".join(noi_dung)


def main():
    bang = cac_bang()
    ma = nguon_java()
    dung, bo = [], []
    for t in bang:
        pat = re.compile(r"(?i)\b(from|join|into|update|table)\s+`?%s`?\b" % re.escape(t))
        (dung if pat.search(ma) else bo).append(t)

    print("Tong %d bang: %d dung, %d khong dung" % (len(bang), len(dung), len(bo)))
    print("Khong dung:", ", ".join(bo))

    dau = [
        "-- =====================================================================",
        "--  awnv3 — lược đồ chuẩn, dựng lại từ CSDL đang chạy",
        "--",
        "--  CÁCH DỰNG FILE NÀY",
        "--  Đối chiếu từng bảng trong CSDL với các câu SQL thật trong src/ —",
        "--  khớp theo FROM/JOIN/INTO/UPDATE <tên bảng>, không đếm theo tên trần,",
        "--  vì \"player\", \"clan\", \"event\" cũng là tên biến trong mã Java.",
        "--",
        "--  GIỮ %d BẢNG — là những bảng máy chủ thật sự đọc hoặc ghi." % len(dung),
        "--",
        "--  BỎ %d BẢNG dưới đây: không câu SQL nào trong src/ chạm tới." % len(bo),
        "--  Chúng KHÔNG bị xoá khỏi CSDL của bạn — chỉ là không có trong file này.",
        "--  Nếu bạn chạy một trang web trên cùng CSDL (các bảng cvh_*, posts,",
        "--  payments, settings… trông đúng như vậy) thì trang web vẫn cần chúng,",
        "--  đừng xoá.",
        "--",
    ]
    for t in bo:
        dau.append("--    - %s" % t)
    dau += [
        "--",
        "--  DỮ LIỆU NGƯỜI CHƠI: các bảng dưới đây chỉ lấy CẤU TRÚC, không lấy",
        "--  dòng nào — file này để dựng máy chủ mới, không phải bản sao lưu.",
    ]
    for t in sorted(CHI_CAU_TRUC & set(dung)):
        dau.append("--    - %s" % t)
    dau += [
        "--",
        "--  Muốn sao lưu đủ cả dữ liệu người chơi thì dùng mysqldump trực tiếp.",
        "-- =====================================================================",
        "",
        "SET NAMES utf8mb4;",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "",
    ]

    co_du_lieu = [t for t in dung if t not in CHI_CAU_TRUC]
    chi_cau_truc = [t for t in dung if t in CHI_CAU_TRUC]

    than = []
    if chi_cau_truc:
        than.append("\n-- --------------------------------------------------------")
        than.append("--  Bảng dữ liệu người chơi — chỉ cấu trúc")
        than.append("-- --------------------------------------------------------\n")
        than.append(chay([DUMP, "-u", "root", "--no-data", "--skip-comments",
                          "--default-character-set=utf8mb4",
                          "--add-drop-table", DB] + chi_cau_truc))
    if co_du_lieu:
        than.append("\n-- --------------------------------------------------------")
        than.append("--  Bảng dữ liệu gốc của game — cấu trúc kèm dữ liệu")
        than.append("-- --------------------------------------------------------\n")
        than.append(chay([DUMP, "-u", "root", "--skip-comments",
                          "--default-character-set=utf8mb4",
                          "--add-drop-table", "--complete-insert",
                          "--extended-insert", DB] + co_du_lieu))

    cuoi = "\nSET FOREIGN_KEY_CHECKS = 1;\n"

    os.makedirs(os.path.dirname(RA), exist_ok=True)
    with open(RA, "w", encoding="utf-8", newline="\n") as f:
        f.write("\n".join(dau))
        f.write("\n".join(than))
        f.write(cuoi)
    print("Da ghi %s (%.1f MB)" % (RA, os.path.getsize(RA) / 1048576.0))


main()
