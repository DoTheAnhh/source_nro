# Đưa máy chủ lên VPS — làm theo thứ tự

Ghi cho Ubuntu 22.04 / 24.04. Mỗi bước có cách **kiểm tra** ngay sau đó; đừng
sang bước tiếp khi bước hiện tại chưa kiểm được.

Quy ước trong tài liệu: thư mục đích trên VPS là `/opt/nro/Server`. Đổi chỗ khác
cũng được, chỉ cần sửa đồng bộ ở mọi bước.

---

## 0. Chuẩn bị ở máy nhà

Tạo bản sao lưu CSDL mới nhất rồi đóng gói. Thư mục `Server/` khoảng **3,3 GB**,
riêng `data/` đã **1,6 GB** (`data/icon` 827 MB) — nén lại rồi hãy tải lên.

```powershell
cd F:\NgocRong
tar -czf nro-server.tar.gz Server
```

Bản sao lưu CSDL nằm sẵn ở `Server/backupsql/awnv3_backup_*.sql` (máy chủ tự tạo
mỗi lần khởi động). Lấy file **mới nhất**.

---

## 1. Cấu hình VPS tối thiểu

| Mục | Mức | Vì sao |
|---|---|---|
| RAM | 4 GB | máy chủ chạy `-Xmx3g` |
| Ổ cứng | 40 GB | dự án 3,3 GB + CSDL + chỗ cho sao lưu |
| CPU | 2 nhân | |

---

## 2. Cài Java 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
```

**Kiểm tra:**

```bash
java -version     # phải ra 21.x
javac -version    # phải có, vì build.bat/biên dịch lại cần javac
```

Bắt buộc đúng **21**: các file `.class` trong `out/` có major version 65, JRE
thấp hơn sẽ ném `UnsupportedClassVersionError` ngay khi khởi động.

---

## 3. Cài MariaDB

```bash
sudo apt install -y mariadb-server mariadb-client
sudo systemctl enable --now mariadb
```

**Dùng MariaDB, đừng dùng MySQL 8.** Driver JDBC gói sẵn trong
`lib/NgocRongOnline.jar` là Connector/J 5.x (gói `com.mysql.jdbc`, không có
`com.mysql.cj`), không hỗ trợ `caching_sha2_password` — cơ chế xác thực mặc định
của MySQL 8. Nếu buộc phải dùng MySQL 8 thì tài khoản phải đặt kiểu cũ:

```sql
ALTER USER 'nro'@'localhost' IDENTIFIED WITH mysql_native_password BY '<mật khẩu>';
```

`mariadb-client` cũng là thứ mang theo `mysqldump` mà máy chủ cần cho việc tự
sao lưu.

---

## 4. Tạo CSDL và tài khoản riêng

Đừng dùng `root` mật khẩu rỗng như bản chạy ở nhà.

```bash
sudo mariadb
```

```sql
CREATE DATABASE awnv3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'nro'@'localhost' IDENTIFIED BY '<mật khẩu mạnh>';
GRANT ALL PRIVILEGES ON awnv3.* TO 'nro'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

---

## 5. Tải mã nguồn lên và giải nén

Từ máy nhà:

```powershell
scp F:\NgocRong\nro-server.tar.gz user@<ip-vps>:/tmp/
```

Trên VPS:

```bash
sudo mkdir -p /opt/nro
sudo tar -xzf /tmp/nro-server.tar.gz -C /opt/nro
sudo chown -R $USER:$USER /opt/nro
```

**Kiểm tra:**

```bash
ls /opt/nro/Server/data/config/data_base.properties
du -sh /opt/nro/Server/data      # khoảng 1,6 GB
```

---

## 6. Nạp CSDL

```bash
cd /opt/nro/Server
mariadb -u nro -p awnv3 < backupsql/<file sao lưu mới nhất>.sql
```

**Kiểm tra:**

```bash
mariadb -u nro -p -e "SELECT COUNT(*) FROM awnv3.player;"
```

---

## 7. Sửa `data/config/data_base.properties`

```bash
nano /opt/nro/Server/data/config/data_base.properties
```

| Khoá | Đặt thành | Ghi chú |
|---|---|---|
| `server.ip` | **IP công cộng của VPS** | đừng để `auto` — trên VPS dùng NAT nó dò ra IP nội bộ |
| `database.user` | `nro` | |
| `database.pass` | mật khẩu vừa tạo | |
| `database.max` | `10` | đang là `1`; hồ một kết nối từng làm máy chủ tự treo 30 giây khi hai chỗ cùng xin |
| `server.port` | `14445` | giữ nguyên nếu không có lý do đổi |

---

## 8. Biên dịch lại trên VPS

Không bắt buộc nếu `out/` đã có sẵn và cùng Java 21, nhưng biên dịch lại thì
chắc chắn hơn.

```bash
cd /opt/nro/Server
rm -rf out && mkdir out
find src -name '*.java' > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d out @sources.txt
```

**Kiểm tra:** không có dòng `error:` nào.

---

## 9. Chạy thử bằng tay

```bash
cd /opt/nro/Server
java -Xms128m -Xmx3g -Djava.awt.headless=true \
     -cp "out:lib/*" nro.server.ServerManager --headless
```

Ba chỗ dễ sai:

- Dấu phân cách classpath trên Linux là **`:`**, không phải `;`.
- Phải **`cd` vào thư mục `Server` trước**. Mã nguồn đọc đường dẫn tương đối
  (`data/config/...`, `data/effect/...`) theo thư mục làm việc **thật**;
  `-Duser.dir` không đổi được thứ đó.
- **Bắt buộc có `--headless`**, không thì máy chủ cố mở giao diện Swing.

**Kiểm tra:** log phải có `Server đang chạy tại port 14445` và không có dòng
`EXCEPTION` nào. Bấm `Ctrl+C` để dừng.

---

## 10. Mở cổng

Hai lớp, thiếu lớp nào cũng không vào được:

```bash
sudo ufw allow 14445/tcp
sudo ufw status
```

Rồi mở tiếp trong **bảng bảo mật của nhà cung cấp** (Security Group / Firewall
trên trang quản trị VPS).

**Chỉ mở đúng cổng 14445.** Cổng 3306 (CSDL) và cổng SSH thì giới hạn theo IP.

---

## 11. Cho chạy nền bằng systemd

```bash
sudo nano /etc/systemd/system/nro.service
```

```ini
[Unit]
Description=NRO Server
After=network.target mariadb.service

[Service]
Type=simple
User=nro
WorkingDirectory=/opt/nro/Server
ExecStart=/usr/bin/java -Xms128m -Xmx3g -Djava.awt.headless=true -cp "out:lib/*" nro.server.ServerManager --headless
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

`WorkingDirectory` là dòng quan trọng nhất — thiếu nó thì máy chủ không tìm thấy
`data/`.

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now nro
sudo systemctl status nro
journalctl -u nro -f
```

---

## 12. Trỏ client sang VPS

Ở máy nhà, sửa **`Mod/Assets/Resources/server_ip.txt`** thành IP công cộng của
VPS (hoặc tên miền), rồi Build lại APK từ Unity.

File đó chỉ chứa một dòng địa chỉ; dòng bắt đầu bằng `#` là chú thích.

Có tên miền thì nên gõ tên miền: sau này đổi VPS chỉ cần trỏ lại DNS, không phải
build APK mới.

---

## Những thứ **không** cần cài

- **libpcap** — `lib/pcap4j-core-1.7.4.jar` nằm trong thư mục nhưng không chỗ
  nào dùng; `Antiddos` chỉ đếm request bằng `HashMap`.
- **Máy chủ X / môi trường đồ hoạ** — chạy `--headless` là đủ. Mã có dùng
  `java.awt.image.BufferedImage`, thứ này chạy headless bình thường.

---

## Hạn chế phải biết trước

**Panel quản trị không mở được trên VPS Linux không màn hình.** Nó là giao diện
Swing, `--headless` bỏ qua hẳn. Ba đường đi:

1. SSH tunnel + X11 forwarding (`ssh -X`), chạy không kèm `--headless`.
2. Giữ panel chạy ở máy nhà, trỏ `database.host` sang IP VPS — khi đó phải mở
   cổng 3306 **chỉ cho riêng IP nhà bạn**, đừng mở ra internet.
3. Dùng VPS Windows + Remote Desktop (đắt hơn, tốn RAM hơn).

Cách 2 sửa được dữ liệu nhưng **không** thấy được trạng thái đang chạy (boss
đang sống, người online), vì những thứ đó nằm trong bộ nhớ tiến trình máy chủ.

---

## 13. Cập nhật về sau — chỉ `git pull`, không gì khác

Đây là việc làm hằng ngày, khác hẳn mười hai bước ở trên (chỉ làm một lần).

```bash
cd <thư mục dự án>
git pull
```

rồi khởi động lại máy chủ:

| Máy chạy | Lệnh |
|---|---|
| Windows | đóng cửa sổ đang chạy, bấm **`chay.bat`** |
| Linux (systemd) | `sudo systemctl restart nro` |

Không cần `javac`. Thư mục `out/` nằm trong git và luôn được biên dịch lại ở máy
nhà **trước** mỗi lần đẩy lên, nên `git pull` là đã có `.class` mới.

### Cài một lần: Git LFS

```bash
git lfs install
git lfs pull
```

**Bắt buộc.** Ba file `.rar` trong `data/` đi qua Git LFS, trong đó
`item_bg_temp.rar` nặng 245 MB. Máy không cài git-lfs thì `git pull` vẫn báo
thành công nhưng cái nó để lại là **file con trỏ 130 byte**, không phải kho lưu
trữ thật — hỏng lặng lẽ, không một dòng báo lỗi. Cài rồi thì mọi lần kéo sau tự
lấy đủ.

Kiểm tra:

```bash
ls -l data/item_bg_temp.rar     # phải ~245 MB, không phải ~130 byte
```

### Dùng `chay.bat`, đừng dùng `run.bat`

`run.bat` **xoá sạch `out/` rồi biên dịch lại**. Làm thế ở máy chạy là hỏng lần
`git pull` kế tiếp: `out/*.class` bị sửa ở cả hai đầu và git từ chối kéo về

```
error: Your local changes to the following files would be overwritten by merge
```

`chay.bat` chỉ gọi `java` trên `out/` có sẵn, không đụng vào file nào git đang
theo dõi.

### Nếu vẫn kẹt

Máy chạy không bao giờ soạn mã, nên cứ soi gương theo kho là xong:

```bash
git fetch origin
git reset --hard origin/main
```

Lệnh này **xoá mọi thay đổi cục bộ** trên máy đó. Ở máy chạy thì không mất gì —
nhưng đừng gõ nó ở máy làm việc.

Cần dùng tới nó là dấu hiệu có file nào đó vẫn bị ghi ở cả hai đầu; báo lại để
thêm vào `Server/.gitignore`, đừng biến `reset --hard` thành thói quen.

### Vì sao bây giờ mới hết kẹt

Ngày 08/09/2026, `git pull` ở máy chạy hỏng với đúng bảy file, kéo hai lần đều
`Aborting`. Không file nào trong bảy file đó do người viết ra — máy chủ tự ghi
chúng mỗi lần khởi động:

| File | Ai ghi |
|---|---|
| `log-run.txt`, `server-console.log`, `srv.log`, `srv.err`, `_run.log` | log mỗi lần chạy |
| `sources.txt` | `run.bat` sinh lại bằng `dir /s /b src\*.java` |
| `out/**/*.class` | `run.bat` biên dịch lại |
| `data/update_data/part` | `Manager.loadDatabase()` đọc bảng `part` rồi ghi đè, mỗi lần khởi động |
| `hs_err_pid*.log`, `replay_pid*.log` | máy ảo Java đổ ra khi sập |

Tất cả (trừ `out/`) đã được bỏ theo dõi và chặn trong `Server/.gitignore`.
Riêng `out/` phải giữ lại — đó chính là thứ giúp máy chạy khỏi cần `javac` — nên
nó được bảo vệ bằng cách khác: dùng `chay.bat`, thứ không biên dịch.
