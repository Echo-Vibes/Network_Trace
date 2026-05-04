import manager.proxy_manager;
import proxy.proxy_server;
import config.ConfigLoader;

/**
 * 代理服务器测试类（自动修改代理）
 *
 * 测试功能：
 * 1. 自动开启系统代理
 * 2. 启动代理服务器
 * 3. 访问网站测试记录和拦截
 * 4. 退出时自动恢复原代理设置
 */
public class
MainTest {

    public static void main(String[] args) {
        String proxyHost = ConfigLoader.get("proxy.host", "127.0.0.1");
        int proxyPort = ConfigLoader.getInt("proxy.port", 8080);

        System.out.println("========================================");
        System.out.println("   代理服务器测试（自动修改代理）");
        System.out.println("========================================\n");

        // 1. 开启系统代理
        System.out.println("[测试] 正在开启系统代理...");
        proxy_manager.set_proxy_on(proxyHost, proxyPort);

        // 2. 启动代理服务器
        System.out.println("[测试] 正在启动代理服务器...");
        new Thread(() -> {
            try {
                proxy_server.start(proxyPort);
            } catch (Exception e) {
                System.err.println("[测试] 代理服务器启动失败: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        // 等待代理服务器启动
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n========================================");
        System.out.println("  代理服务器已启动，监听端口: " + proxyPort);
        System.out.println("  系统代理已自动设置为 " + proxyHost + ":" + proxyPort);
        System.out.println("========================================");
        System.out.println("\n[测试内容]");
        System.out.println("  1. 访问正常网站 → 应正常打开，记录到数据库");
        System.out.println("  2. 访问黑名单网站 → 应显示拦截页面");
        System.out.println("\n[查看记录]");
        System.out.println("  在 VS Code 中查询: SELECT * FROM history ORDER BY visit_time DESC");
        System.out.println("\n========================================\n");

        // 注册关闭钩子（确保退出时恢复代理）
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[测试] 正在停止...");
            proxy_server.stop();
            proxy_manager.restore_proxy();
            System.out.println("[测试] 代理已恢复，测试结束");
        }));

        // 等待用户按回车退出
        System.out.println("按回车键停止代理服务器并退出测试...");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 正常退出（关闭钩子会自动执行）
        System.out.println("\n[测试] 正在退出...");
        System.exit(0);
    }
}