#if UNITY_EDITOR_WIN
using System;
using System.Diagnostics;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;

/// <summary>
/// Trước mỗi lần build Android: dọn hai thứ làm Gradle đứng chờ mãi.
/// </summary>
/// <remarks>
/// <para><b>1. Tiến trình Gradle mồ côi.</b> Tắt Unity (hay nó văng) giữa lúc
/// build, hoặc bấm build lần nữa khi lần trước chưa xong, là để lại một tiến
/// trình <c>java … gradle-launcher</c> không ai quản, vẫn giữ khoá project.
/// "Mồ côi" là tiến trình mà tiến trình cha đã chết — Gradle của lần build đang
/// chạy có cha là chính Unity này nên không bị đụng tới.</para>
///
/// <para><b>2. Sổ đăng ký daemon cũ.</b> Gradle ghi các daemon đang sống vào
/// <c>~/.gradle/daemon/&lt;bản&gt;/registry.bin</c>, kèm cổng mạng của chúng. Daemon
/// chết mà sổ không được xoá thì lần build sau đọc sổ, nối vào cổng cũ — lúc ấy
/// cổng có thể đã thuộc chương trình khác, nhận kết nối mà không bao giờ trả
/// lời. Gradle chờ ở <c>DaemonClientConnection.receive</c> mãi mãi: đúng cảnh
/// "build APK mười mấy phút không xong" mà không có lỗi nào. Không còn daemon
/// nào sống thì sổ ấy chỉ còn là rác — xoá đi, Gradle tự mở daemon mới.</para>
///
/// <para>Dọn tay: menu <b>Tools → Dọn Gradle treo</b>.</para>
/// </remarks>
public class DonGradleTreo : IPreprocessBuildWithReport
{
    public int callbackOrder
    {
        get { return -1000; }
    }

    public void OnPreprocessBuild(BuildReport report)
    {
        if (report.summary.platform != BuildTarget.Android)
        {
            return;
        }
        DonDep();
    }

    [MenuItem("Tools/Dọn Gradle treo")]
    private static void DonTay()
    {
        int n = DonDep();
        EditorUtility.DisplayDialog("Dọn Gradle treo",
            n < 0 ? "Không chạy được lệnh dọn — xem Console."
                : "Đã tắt " + n + " tiến trình Gradle mồ côi, và dọn sổ daemon cũ nếu có.",
            "OK");
    }

    /// <returns>Số tiến trình mồ côi đã tắt; -1 nếu không chạy được lệnh dọn.</returns>
    private static int DonDep()
    {
        const string lenh =
            "$n = 0; "
            + "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | "
            + "Where-Object { $_.CommandLine -like '*gradle-launcher*' -and "
            + "-not (Get-Process -Id $_.ParentProcessId -ErrorAction SilentlyContinue) } | "
            + "ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue; $n++ }; "
            // Khong con daemon nao song -> so dang ky chi con dia chi chet: xoa.
            + "$daemon = Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | "
            + "Where-Object { $_.CommandLine -like '*GradleDaemon*' }; "
            + "if (-not $daemon) { Remove-Item \"$env:USERPROFILE\\.gradle\\daemon\\*\\registry.bin*\" "
            + "-Force -ErrorAction SilentlyContinue }; "
            + "Write-Output $n";
        try
        {
            // Gui lenh dang ma hoa de khong phai lo ngoac kep, dau $ qua cmd.
            string maHoa = Convert.ToBase64String(System.Text.Encoding.Unicode.GetBytes(lenh));
            ProcessStartInfo psi = new ProcessStartInfo("powershell.exe",
                "-NoProfile -NonInteractive -ExecutionPolicy Bypass -EncodedCommand " + maHoa);
            psi.UseShellExecute = false;
            psi.RedirectStandardOutput = true;
            psi.CreateNoWindow = true;
            using (Process p = Process.Start(psi))
            {
                string ra = p.StandardOutput.ReadToEnd().Trim();
                p.WaitForExit(20000);
                int n;
                if (!int.TryParse(ra, out n))
                {
                    n = 0;
                }
                if (n > 0)
                {
                    UnityEngine.Debug.Log("[DonGradleTreo] Đã tắt " + n + " tiến trình Gradle mồ côi trước khi build.");
                }
                return n;
            }
        }
        catch (Exception ex)
        {
            // Khong don duoc thi van build binh thuong — chi la mat buoc phong ngua.
            UnityEngine.Debug.LogWarning("[DonGradleTreo] Không dọn được Gradle treo: " + ex.Message);
            return -1;
        }
    }
}
#endif
