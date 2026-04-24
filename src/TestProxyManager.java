import manager.proxy_manager;
import java.util.Scanner;

/**
 * ProxyManager 测试类
 * 
 * 测试内容：
 * 1. 备份当前代理设置
 * 2. 设置代理为 127.0.0.1:8080
 * 3. 等待用户确认（此时可以去浏览器检查代理是否生效）
 * 4. 恢复原始代理设置
 */
public class TestProxyManager {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("   ProxyManager 测试程序");
        System.out.println("========================================\n");

        // 1. 显示当前代理设置
        System.out.println("【步骤1】查看当前代理设置...");
        System.out.println("请手动检查：");
        System.out.println("  Windows设置 → 网络和Internet → 代理");
        System.out.println("  或 控制面板 → Internet选项 → 连接 → 局域网设置");
        System.out.println("按回车继续...");
        scanner.nextLine();

        // 2. 开启代理
        System.out.println("\n【步骤2】开启代理 (127.0.0.1:8080)...");
        proxy_manager.set_proxy_on("127.0.0.1", 8080);

        System.out.println("\n【验证】请再次检查系统代理设置：");
        System.out.println("  代理开关应该已打开");
        System.out.println("  代理地址应该是 127.0.0.1:8080");
        System.out.println("\n按回车恢复代理...");
        scanner.nextLine();

        // 3. 恢复代理
        System.out.println("\n【步骤3】恢复原始代理设置...");
        proxy_manager.restore_proxy();

        System.out.println("\n【验证】请再次检查系统代理设置：");
        System.out.println("  应该恢复到测试前的状态");

        System.out.println("\n测试完成！");
        scanner.close();
    }
}