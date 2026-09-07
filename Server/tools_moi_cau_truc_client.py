# -*- coding: utf-8 -*-
"""
Moi cau truc lop cua client Unity IL2CPP tu global-metadata.dat.

CACH LAM
--------
Metadata v31 la mot day cac vung (offset, size) lien nhau. Ba vung can dung:
  [2]  ten dinh danh, cac chuoi ket thuc bang byte 0
  [11] bang TRUONG:      moi muc 12 byte (nameIndex, typeIndex, token)
  [19] bang KIEU (lop):  moi muc mot struct co dinh
  [5]  bang PHUONG THUC: moi muc mot struct co dinh

Kich thuoc struct KHONG doc tu file duoc — no do phien ban IL2CPP quyet dinh.
Nen o day thu tung kich thuoc roi KIEM: moi muc phai co nameIndex tro dung vao
mot chuoi, va fieldStart/methodStart phai nam trong bang tuong ung. Kich thuoc
nao qua duoc het moi muc thi la dung.

GIOI HAN — DOC KY
-----------------
Chi lay duoc CHU KY: ten lop, ten truong, ten phuong thuc. IL2CPP bien dich than
ham thanh ma may nen THAN HAM KHONG CON TRONG FILE NAY. Ban dump nay la khung
xuong de doi chieu, khong phai ma nguon chay duoc.
"""
import io
import os
import struct
import sys

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

DUONG_DAN = ("F:/AWN_Version/Mod_Local/Panel Game_Data/"
             "il2cpp_data/Metadata/global-metadata.dat")
RA = "docs/client-cau-truc.txt"

d = open(DUONG_DAN, 'rb').read()


def doc_header():
    cap = []
    i = 8
    dau = None
    while True:
        off, size = struct.unpack_from('<ii', d, i)
        if dau is None:
            dau = off
        if i + 8 > dau:
            break
        cap.append((off, size))
        i += 8
    return cap


cap = doc_header()
S_OFF, S_LEN = cap[2]      # ten dinh danh
F_OFF, F_LEN = cap[11]     # truong
T_OFF, T_LEN = cap[19]     # kieu
M_OFF, M_LEN = cap[5]      # phuong thuc


def ten(idx):
    """Chuoi ket thuc bang 0 tai vi tri idx trong vung ten."""
    if idx < 0 or idx >= S_LEN:
        return None
    k = d.index(b'\x00', S_OFF + idx)
    return d[S_OFF + idx:k].decode('utf-8', 'replace')


def hop_le_ten(idx):
    if idx < 0 or idx >= S_LEN:
        return False
    t = ten(idx)
    return t is not None and len(t) < 512


# ---------------------------------------------------------------- truong: 12 byte
assert F_LEN % 12 == 0, "bang truong khong chia het 12"
SO_TRUONG = F_LEN // 12
truong = []
for i in range(SO_TRUONG):
    n, ty, tok = struct.unpack_from('<iiI', d, F_OFF + i * 12)
    truong.append(ten(n))

# ---------------------------------------------------------------- phuong thuc
def thu_method(sz):
    if M_LEN % sz:
        return None
    n = M_LEN // sz
    ra = []
    for i in range(n):
        ni = struct.unpack_from('<i', d, M_OFF + i * sz)[0]
        if not hop_le_ten(ni):
            return None
        ra.append(ten(ni))
    return ra


method = None
SZ_M = None
for sz in (36, 40, 44, 48, 52, 56):
    r = thu_method(sz)
    if r:
        method = r
        SZ_M = sz
        break
assert method, "khong doan duoc kich thuoc muc phuong thuc"

# ---------------------------------------------------------------- kieu (lop)
def thu_type(sz):
    if T_LEN % sz:
        return None
    n = T_LEN // sz
    ra = []
    for i in range(n):
        o = T_OFF + i * sz
        ni, nsi = struct.unpack_from('<ii', d, o)
        if not hop_le_ten(ni) or not hop_le_ten(nsi):
            return None
        # fieldStart / methodStart nam o cac o int32 tiep theo; kiem so 8..9
        fs, ms = struct.unpack_from('<ii', d, o + 32)
        if fs < -1 or ms < -1 or fs > SO_TRUONG or ms > len(method):
            return None
        mc, pc, fc = struct.unpack_from('<HHH', d, o + 64)
        if fc > 4000 or mc > 4000:
            return None
        ra.append((ten(nsi), ten(ni), fs, fc, ms, mc))
    return ra


kieu = None
SZ_T = None
for sz in (88, 92, 96, 100, 104, 84):
    r = thu_type(sz)
    if r:
        kieu = r
        SZ_T = sz
        break
assert kieu, "khong doan duoc kich thuoc muc kieu"

print("kich thuoc muc: kieu=%d  phuong thuc=%d" % (SZ_T, SZ_M))
print("so lop=%d  so truong=%d  so phuong thuc=%d"
      % (len(kieu), SO_TRUONG, len(method)))

# ---------------------------------------------------------------- ghi ra
BO_QUA = ("System", "Unity", "Mono", "Microsoft", "Internal", "TMPro",
          "JetBrains", "Newtonsoft", "Cinemachine", "Spine")


def cua_game(ns, n):
    if any(ns.startswith(b) for b in BO_QUA):
        return False
    if any(n.startswith(b) for b in ("<", "$", "__")):
        return False
    return True


lop_game = [k for k in kieu if cua_game(k[0] or "", k[1] or "")]
print("lop cua game (bo thu vien):", len(lop_game))

os.makedirs("docs", exist_ok=True)
with io.open(RA, "w", encoding="utf-8", newline="\n") as f:
    f.write("CAU TRUC LOP CUA CLIENT — moi tu global-metadata.dat\n")
    f.write("=" * 70 + "\n\n")
    f.write("Unity 2022.3.62f2, IL2CPP. Chi co CHU KY: ten lop, ten truong, ten\n")
    f.write("phuong thuc. IL2CPP bien dich than ham thanh ma may nen THAN HAM\n")
    f.write("KHONG CON trong tep nay — day la khung xuong de doi chieu, khong\n")
    f.write("phai ma nguon chay duoc.\n\n")
    f.write("Tong: %d lop trong metadata, %d lop la cua game.\n\n"
            % (len(kieu), len(lop_game)))
    f.write("=" * 70 + "\n\n")
    for ns, n, fs, fc, ms, mc in sorted(lop_game, key=lambda x: (x[0] or "", x[1])):
        f.write("class %s%s\n" % ((ns + ".") if ns else "", n))
        if fc:
            f.write("    // truong (%d)\n" % fc)
            for i in range(fs, min(fs + fc, SO_TRUONG)):
                f.write("    %s\n" % truong[i])
        if mc:
            f.write("    // phuong thuc (%d)\n" % mc)
            for i in range(ms, min(ms + mc, len(method))):
                f.write("    %s()\n" % method[i])
        f.write("\n")
print("da ghi", RA, "(%.0f KB)" % (os.path.getsize(RA) / 1024))
