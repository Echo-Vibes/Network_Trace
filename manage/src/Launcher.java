import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Main.startBackend();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("启动失败");
            alert.setHeaderText("后端服务启动失败");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load("http://localhost:8081");

        VBox root = new VBox(webView);
        Scene scene = new Scene(root, 1100, 750);

        // 点击右上角 X 时完整退出
        stage.setOnCloseRequest(e -> {
            Main.stopBackend();
            Platform.exit();
            System.exit(0);
        });

        stage.setTitle("WebTrackDB 网络监控面板");
        stage.setScene(scene);
        stage.show();
    }
}