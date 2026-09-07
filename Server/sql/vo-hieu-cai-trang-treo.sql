-- =====================================================================
--  Vô hiệu 15 cải trang trỏ vào part chưa bao giờ tồn tại (1953–1998)
--
--  VÌ SAO PHẢI LÀM:
--  Id part bắt buộc liên tục, nên mỗi cải trang mới thêm vào sẽ chiếm đúng
--  các id 1953, 1954, 1955, … Mười lăm món dưới đây trỏ sẵn vào khoảng đó
--  từ lâu (bảng `part` cao nhất mới có 1952 trước hôm nay), nên món nào
--  chưa có res sẽ **cướp hình của cải trang mới**.
--
--  Đã xảy ra thật khi thêm Akatsuki/Kamin/Oren:
--      2193 Super Broly Z         1953/1954/1955  ->  hiện ra Akatsuki
--      2194 Super Broly Z Hắc Hoá 1956/1957/1958  ->  hiện ra Kamin
--      2195 Super Broly Z White   1959/1960/1961  ->  hiện ra Oren
--  Ba món còn lại trong khoảng 1962–1998 sẽ dính đúng như vậy ở những cải
--  trang thêm sau, nên vô hiệu cả mười lăm ngay bây giờ.
--
--  Đặt head/body/leg = -1: vật phẩm vẫn còn trong CSDL, chỉ là không trỏ
--  vào part nào. Giá trị cũ ghi lại ngay dưới đây để khôi phục khi có res.
--
--  KHÔI PHỤC (chỉ làm khi đã có đủ res cho part đó, và phải đánh lại id cho
--  liên tục sau id lớn nhất hiện có — KHÔNG dùng lại số cũ):
--      1777 Gas Heeters            1984 / 1985 / 1986
--      1778 Oil Heeters            1987 / 1988 / 1989
--      1779 Macki Heeters          1990 / 1991 / 1992
--      1780 Đội Trưởng Elec        1993 / 1994 / 1995
--      1781 Granolah               1996 / 1997 / 1998
--      2193 Super Broly Z          1953 / 1954 / 1955
--      2194 Super Broly Z Hắc Hoá  1956 / 1957 / 1958
--      2195 Super Broly Z White    1959 / 1960 / 1961
--      2196 Chi Chi Z              1962 / 1963 / 1964
--      2197 Thiên Xin Hăng Z       1965 / 1966 / 1967
--      2198 Erasa Z                1968 / 1969 / 1970
--      2199 Gogeta Z               1971 / 1972 / 1973
--      2200 Picolo Daima Z         1975 / 1976 / 1977
--      2201 Gohan Z                1978 / 1979 / 1980
--      2202 Goku Blue Z            1981 / 1982 / 1983
-- =====================================================================

UPDATE `item_template`
   SET `head` = -1, `body` = -1, `leg` = -1
 WHERE `id` IN (1777, 1778, 1779, 1780, 1781,
                2193, 2194, 2195, 2196, 2197,
                2198, 2199, 2200, 2201, 2202);
