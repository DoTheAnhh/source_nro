# -*- coding: utf-8 -*-
"""
Doi nhan chi so trong client (global-metadata.dat cua IL2CPP).

CACH LAM
--------
File metadata gom mot header la day cac cap (offset, size) tro toi cac vung
lien nhau. Vung [0] la BANG CHUOI (moi muc 8 byte: do dai + vi tri), vung [1]
la KHOI DU LIEU CHUOI.

Doi mot chuoi thanh chuoi DAI HON thi khoi du lieu phinh ra, moi vung phia sau
bi day lui. Bo va nay:
  1. Doc toan bo chuoi theo bang muc luc.
  2. Thay noi dung nhung chuoi can doi.
  3. Dung lai khoi du lieu, ghi lai vi tri moi cho TAT CA cac muc.
  4. Dich cac vung phia sau va sua lai header.

Lam duoc vi trong metadata moi tham chieu deu la CHI SO trong vung cua no,
khong phai vi tri tuyet doi trong file — chi header moi giu vi tri tuyet doi.
"""
import io
import os
import shutil
import struct
import sys

# Console Windows mac dinh la cp1252, in chu Viet ra la no UnicodeEncodeError
# TRUOC khi kip ghi file. Ep stdout ve UTF-8 ngay tu dau.
try:
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
except Exception:
    pass

DUONG_DAN = ("F:/AWN_Version/Mod_Local/Panel Game_Data/"
             "il2cpp_data/Metadata/global-metadata.dat")

# Chuoi cu -> chuoi moi. Khop CHINH XAC ca chuoi, khong phai chuoi con.
# Nhan cu -> nhan moi. Khop CHINH XAC ca chuoi.
#
# Nhan phai NGAN: khung thong tin nhan vat trong client rong co dinh, khong tu
# xuong dong. "Ty le chi mang" lam dong "Suc danh: 123.456.789, ..." tran ra
# ngoai khung va bi cat. "Chi mang" / "ST chi mang" van doc duoc ma vua khung.
DOI = {
    "DAME: ": "Sức đánh: ",
    "DEF: ": "Thủ: ",
    ", CRIT: ": ", Chí mạng: ",
    ", ST CRIT: ": ", ST chí mạng: ",
    # Vet cu tu lan va truoc — cho phep chay lai de rut ngan.
    ", Tỷ lệ chí mạng: ": ", Chí mạng: ",
    ", Sát thương chí mạng: ": ", ST chí mạng: ",
}

# ---------------------------------------------------------------------------
#  Cho go tieng Viet: noi dai bang ky tu duoc phep
# ---------------------------------------------------------------------------
#  Trong metadata co mot ten ham la "AcsChars" (accepted chars) va DUNG mot
#  chuoi hinh dang bang ky tu: tab/xuong dong/space, dau cau, so, A-Z, a-z —
#  KHONG co chu co dau. Go tieng Viet khong an la vi bo loc nay.
#
#  Chin bo font nhung trong resources.assets deu la UTM Nokia Standard va deu
#  PHU DU chu Viet ca thuong lan HOA (da kiem: khong thieu ky tu nao), nen
#  them vao bang ky tu la du — khong phai thay font.
#
#  Chuoi goc dang XEP TANG theo ma ky tu. Chu Viet co ma U+00C0 tro len, deu
#  lon hon 'z' (U+007A), nen noi vao cuoi theo thu tu tang van giu nguyen tinh
#  chat do — phong khi cho nao do dung tim nhi phan tren chuoi nay.
#
#  CHUA KIEM CHUNG DUOC bang cach chay thu. Neu go van khong an, hoac co gi la,
#  chep de ban .goc lai la xong.
BANG_KY_TU_CU = ("\t\n\r '(),-./0123456789:?"
                 "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz")

_VIET = ("àáảãạăằắẳẵặâầấẩẫậđèéẻẽẹêềếểễệìíỉĩị"
         "òóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵ")
_CHU_VIET = "".join(sorted(set(_VIET + _VIET.upper())))

DOI[BANG_KY_TU_CU] = BANG_KY_TU_CU + _CHU_VIET


def rut_gon(t, n=46):
    """In chuoi dai thanh dang ngan gon de dong log khong tran ca man hinh."""
    r = repr(t)
    return r if len(r) <= n else r[:n - 4] + "..." + r[-1]


def doc_header(d):
    """Doc cac cap (offset, size) cho toi khi het vung header."""
    cap = []
    i = 8
    dau_tien = None
    while True:
        off, size = struct.unpack_from('<ii', d, i)
        if dau_tien is None:
            dau_tien = off
        if i + 8 > dau_tien:
            break
        cap.append((off, size))
        i += 8
    return cap, dau_tien


def main():
    if not os.path.exists(DUONG_DAN):
        print("KHONG THAY file metadata:", DUONG_DAN)
        return 1
    d = bytearray(open(DUONG_DAN, 'rb').read())
    magic, ver = struct.unpack_from('<II', d, 0)
    print("magic=%08X version=%d kich thuoc=%d" % (magic, ver, len(d)))
    if magic != 0xFAB11BAF:
        print("KHONG PHAI file global-metadata hop le.")
        return 1

    cap, het_header = doc_header(d)
    print("header: %d vung, ket thuc o byte %d" % (len(cap), het_header))

    bang_off, bang_size = cap[0]
    blob_off, blob_size = cap[1]
    so_muc = bang_size // 8
    print("bang chuoi: %d muc | khoi du lieu: %d byte" % (so_muc, blob_size))

    # ---- doc tat ca chuoi ----
    muc = []
    for k in range(so_muc):
        do_dai, vi_tri = struct.unpack_from('<ii', d, bang_off + k * 8)
        raw = bytes(d[blob_off + vi_tri: blob_off + vi_tri + do_dai])
        muc.append(raw)

    # ---- thay the ----
    doi_bytes = {k.encode('utf-8'): v.encode('utf-8') for k, v in DOI.items()}
    da_doi = []
    for k in range(so_muc):
        if muc[k] in doi_bytes:
            cu = muc[k]
            muc[k] = doi_bytes[cu]
            da_doi.append((k, cu.decode('utf-8'), muc[k].decode('utf-8')))

    if not da_doi:
        print("Khong tim thay chuoi nao can doi — co the da va roi.")
        return 0
    print("\nSe doi %d chuoi:" % len(da_doi))
    for k, a, b in da_doi:
        print("  muc %5d: %-14s -> %s  (%d -> %d byte)"
              % (k, rut_gon(a), rut_gon(b),
                 len(a.encode('utf-8')), len(b.encode('utf-8'))))

    # ---- dung lai khoi du lieu ----
    blob_moi = bytearray()
    bang_moi = bytearray()
    for raw in muc:
        bang_moi += struct.pack('<ii', len(raw), len(blob_moi))
        blob_moi += raw
    # Can bien 4 byte cho giong ban goc
    while len(blob_moi) % 4:
        blob_moi.append(0)

    chenh = len(blob_moi) - blob_size
    print("\nkhoi du lieu: %d -> %d byte (chenh %+d)" % (blob_size, len(blob_moi), chenh))

    # ---- dung file moi ----
    ra = bytearray(d[:het_header])
    # vung [0] bang chuoi (kich thuoc khong doi)
    assert len(bang_moi) == bang_size, "bang chuoi doi kich thuoc — khong duoc phep"

    vung_moi = []
    con_tro = het_header
    for i, (off, size) in enumerate(cap):
        if i == 0:
            noi_dung = bytes(bang_moi)
        elif i == 1:
            noi_dung = bytes(blob_moi)
        else:
            noi_dung = bytes(d[off:off + size])
        vung_moi.append((con_tro, len(noi_dung), noi_dung))
        con_tro += len(noi_dung)

    # ghi lai header
    for i, (off, size, _) in enumerate(vung_moi):
        struct.pack_into('<ii', ra, 8 + i * 8, off, size)
    for off, size, noi_dung in vung_moi:
        assert len(ra) == off, "lech vi tri vung: %d != %d" % (len(ra), off)
        ra += noi_dung

    # ---- sao luu roi ghi ----
    bak = DUONG_DAN + ".goc"
    if not os.path.exists(bak):
        shutil.copy2(DUONG_DAN, bak)
        print("\nDa sao luu ban goc: %s" % bak)
    else:
        print("\n(ban sao luu da co san: %s)" % bak)
    # Ghi ra file phu truoc: neu game dang chay thi file goc bi khoa, ghi thang
    # se hong nua chung. Ghi xong moi thu thay vao cho.
    tam = DUONG_DAN + ".moi"
    open(tam, 'wb').write(bytes(ra))
    print("Da dung xong ban moi: %s (%d byte, cu %d)" % (tam, len(ra), len(d)))

    d2 = open(tam, 'rb').read()
    cap2, _ = doc_header(bytearray(d2))
    b_off, b_size = cap2[0]
    l_off, l_size = cap2[1]
    loi = 0
    for k in range(b_size // 8):
        do_dai, vi_tri = struct.unpack_from('<ii', d2, b_off + k * 8)
        if vi_tri < 0 or vi_tri + do_dai > l_size:
            loi += 1
    print("\nKiem lai: %d muc chuoi, %d muc tro ra ngoai vung" % (b_size // 8, loi))
    for k, a, b in da_doi:
        do_dai, vi_tri = struct.unpack_from('<ii', d2, b_off + k * 8)
        doc = d2[l_off + vi_tri: l_off + vi_tri + do_dai].decode('utf-8')
        print("  muc %5d doc lai: %r  %s" % (k, doc, "OK" if doc == b else "LECH"))
    # cac vung phai lien nhau va phu het file
    lien = all(cap2[i][0] + cap2[i][1] == cap2[i + 1][0] for i in range(len(cap2) - 1))
    het = cap2[-1][0] + cap2[-1][1] == len(d2)
    print("  cac vung lien nhau: %s | phu het file: %s" % (lien, het))
    if loi or not lien or not het:
        print("\nBAN MOI CO VAN DE — khong thay vao cho. File goc giu nguyen.")
        return 1

    try:
        os.replace(tam, DUONG_DAN)
        print("\nDa thay vao cho. Mo lai game de thay nhan moi.")
        print("Muon tra lai nhu cu: chep de %s len %s" % (bak, DUONG_DAN))
    except OSError as ex:
        print("\nCHUA thay duoc vao cho: %s" % ex)
        print("Game dang chay nen file bi khoa. Hay DONG GAME roi chay lai lenh nay,")
        print("hoac tu chep de: %s  ->  %s" % (tam, DUONG_DAN))
        return 2
    return 0


if __name__ == '__main__':
    sys.exit(main())
