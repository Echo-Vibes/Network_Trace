import javafx.application.Application;
import manager.proxy_manager;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import proxy.proxy_server;

import java.io.File;

public class Main {

    private static Tomcat tomcat;

    public static void startBackend() throws Exception {
        // 启动代理服务器（后台线程）
        new Thread(() -> {
            try {
                proxy_server.start(8080);
            } catch (Exception e) {
                System.err.println("[错误] 代理服务器启动失败: " + e.getMessage());
            }
        }).start();

        // 等待代理启动并设置系统代理
        try {
            Thread.sleep(500);
            proxy_manager.set_proxy_on("127.0.0.1", 8080);
        } catch (Exception e) {
            System.err.println("[错误] 设置系统代理失败: " + e.getMessage());
        }

        int port = 8081;
        tomcat = new Tomcat();
        tomcat.setBaseDir(new File(".").getAbsolutePath());

        Connector connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        String webDir = new File("web").getAbsolutePath();
        System.out.println("[信息] web目录: " + webDir);

        Context ctx = tomcat.addWebapp("", webDir);
        ctx.addWelcomeFile("login.html");

        tomcat.start();

        System.out.println("========================================");
        System.out.println("  WebTrackDB 管理控制台启动成功");
        System.out.println("  地址: http://localhost:" + port);
        System.out.println("  代理端口: 8080");
        System.out.println("========================================");
    }

    public static void stopBackend() {
        try {
            proxy_server.stop();
            proxy_manager.restore_proxy();
            if (tomcat != null) {
                tomcat.stop();
            }
            System.out.println("[关闭] 已停止");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::stopBackend));
        Application.launch(Launcher.class, args);
    }
}