package manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class proxy_manager {
    private static boolean origin_proxy_enabled = false; // 原代理是否启用
    private static String original_proxy = ""; // 原代理地址

    // 执行系统命令，无返回值
    private static void execute_cmd(String cmd) {
        try {
            // Runtime.getRuntime().exec(cmd);

            // 使用ProcessBuilder不解析特殊字符，避免命令注入的风险
            new ProcessBuilder("cmd.exe", "/c", cmd).start();
        } catch (Exception e) {
            System.err.println("[代理管理]执行命令失败: " + e.getMessage());
        }
    }

    // 读取注册表，检查代理是否开启
    private static boolean is_proxy_enabled() {
        try {
            // Process p = Runtime.getRuntime().exec(
            // "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet
            // Settings\" /v ProxyEnable");

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
                    "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable");
            Process p = pb.start();

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ProxyEnable")) {
                    String[] parts = line.trim().split("\\s+");
                    String value = parts[parts.length - 1];
                    return "0x1".equals(value); // 0x1表示启用代理
                }
            }
        } catch (Exception e) {
            System.err.println("[代理管理]检查代理状态失败 " + e.getMessage());
        }
        return false;
    }

    // 获取代理地址
    private static String get_proxy_address() {
        try {
            // Process p = Runtime.getRuntime().exec(
            // "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet
            // Settings\" /v ProxyServer");

            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c",
                    "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer");
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("ProxyServer")) {
                    String[] parts = line.trim().split("\\s+");
                    return parts[parts.length - 1];
                }
            }
        } catch (Exception e) {
            System.err.println("[代理管理]获取代理地址失败 " + e.getMessage());
        }
        return "";
    }

    // 开/关代理
    private static void set_proxy(boolean enabled) {
        String value = enabled ? "1" : "0";
        String cmd = String.format(
                "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d "
                        + value + " /f");
        execute_cmd(cmd);
    }

    // 设置代理地址
    private static void set_proxy_address(String address) {
        String cmd = "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d \""
                + address + "\" /f";
        execute_cmd(cmd);
    }

    // 开启代理（地址 + 端口）
    public static void set_proxy_on(String proxy_address, int proxy_port) {
        // 备份原始代理
        origin_proxy_enabled = is_proxy_enabled();
        original_proxy = get_proxy_address();

        System.out.println("[代理管理]原始状态: " + origin_proxy_enabled);
        System.out.println("[代理管理]原始地址: " + original_proxy);

        set_proxy(true);
        set_proxy_address(proxy_address + ":" + proxy_port);

        System.out.println("[代理管理]已设置代理： " + proxy_address + ":" + proxy_port);
    }

    // 恢复原始状态
    public static void restore_proxy() {
        if (origin_proxy_enabled) {
            set_proxy(true);
            set_proxy_address(original_proxy);
            System.out.println("[代理管理]已恢复原始代理: " + original_proxy);
        } else {
            set_proxy(false);
            System.out.println("[代理管理]已关闭代理");
        }
    }
}