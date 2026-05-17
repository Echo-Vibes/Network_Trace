import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import manager.proxy_manager;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import proxy.proxy_server;

import java.io.File;
import java.net.URISyntaxException;

public class Main extends Application {

    private static Tomcat tomcat;

    /** 获取应用安装目录（JAR 所在目录，打包模式） */
    private static String getAppDir() {
        try {
            String jarPath = Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            return (jarFile.isFile() ? jarFile.getParent() : jarPath);
        } catch (URISyntaxException e) {
            return new File(".").getAbsolutePath();
        }
    }

    /** 自动查找 web 资源目录（兼容 IDE 和打包两种模式） */
    private static File findWebDir() {
        // 1. 打包模式：JAR 旁边的 web/
        File dir = new File(getAppDir(), "web");
        if (dir.isDirectory()) return dir;
        // 2. IDE 模式：当前工作目录下的 web/
        dir = new File("web");
        if (dir.isDirectory()) return dir;
        throw new RuntimeException("找不到 web 资源目录，请确认 web/ 文件夹位置正确");
    }

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

    @Override
    public void start(Stage stage) {
        try {
            startBackend();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("启动失败");
            alert.setHeaderText("后台服务启动失败");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load("http://localhost:8081");

        Scene scene = new Scene(webView, 1100, 750);

        stage.setOnCloseRequest(e -> {
            stopBackend();
            Platform.exit();
            System.exit(0);
        });

        stage.setTitle("WebTrackDB 网络监控系统");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::stopBackend));
        launch(args);
    }
}