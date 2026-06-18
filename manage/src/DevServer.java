import manager.proxy_manager;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import proxy.proxy_server;

import java.io.File;

/**
 * 开发模式启动类：只启动 Tomcat + 代理，不弹 JavaFX 窗口，不设置系统代理。
 * 用浏览器访问 http://localhost:8081 进行开发和测试。
 */
public class DevServer {

    private static Tomcat tomcat;

    private static String getAppDir() {
        try {
            String jarPath = DevServer.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            return (jarFile.isFile() ? jarFile.getParent() : jarPath);
        } catch (Exception e) {
            return new File(".").getAbsolutePath();
        }
    }

    private static File findWebDir() {
        File dir = new File(getAppDir(), "web");
        if (dir.isDirectory()) return dir;
        dir = new File("web");
        if (dir.isDirectory()) return dir;
        throw new RuntimeException("找不到 web 资源目录");
    }

    public static void main(String[] args) throws Exception {
//         启动代理服务器（不修改系统代理）
//        new Thread(() -> {
//            try {
//                proxy_server.start(8080);
//                System.out.println("[代理] 代理服务器已启动，端口 8080（系统代理未修改）");
//            } catch (Exception e) {
//                System.err.println("[代理] 启动失败: " + e.getMessage());
//            }
//        }).start();
//        try {
//            Thread.sleep(500);
//            proxy_manager.set_proxy_on("127.0.0.1", 8080);
//        } catch (Exception e) {
//            System.err.println("[错误] 设置系统代理失败: " + e.getMessage());
//        }

        // 启动嵌入式 Tomcat
        int port = 8081;
        tomcat = new Tomcat();
        tomcat.setBaseDir(new File(System.getProperty("java.io.tmpdir"), "WebTrackDB").getAbsolutePath());

        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        File webDir = findWebDir();
        System.out.println("[信息] web目录: " + webDir.getAbsolutePath());

        Context ctx = tomcat.addWebapp("", webDir.getAbsolutePath());
        ctx.addWelcomeFile("login.html");
        tomcat.start();

        System.out.println("========================================");
        System.out.println("  WebTrackDB 开发模式启动成功");
        System.out.println("  后端 API: http://localhost:" + port);
        System.out.println("  代理端口: 8080（系统代理未修改）");
        System.out.println("  用浏览器打开 http://localhost:" + port + " 即可测试");
        System.out.println("========================================");

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                proxy_server.stop();
//                tomcat.stop();
//                System.out.println("[关闭] 服务已停止");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }));

        System.out.println("按回车键停止服务...");
        System.in.read();
        System.exit(0);
    }
}