package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class proxy_server {
    private static ServerSocket server_socket; // 服务器监听套接字
    private static ExecutorService thread_pool; // 线程池管理客户端连接
    private static volatile boolean running = false; // 服务器运行状态，volatile保证多线程环境下的可见性

    // 启动代理服务器
    public static void start(int port) throws IOException {
        server_socket = new ServerSocket(port);
        thread_pool = Executors.newCachedThreadPool(); // 使用可缓存线程池，根据需要创建新线程
        running = true;

        System.out.println("[代理服务器]启动成功，监听端口: " + port);

        while (running) {
            try {
                Socket client_socket = server_socket.accept();// 无连接时阻塞，浏览器发起连接时返回socket对象
                thread_pool.submit(new proxy_handler(client_socket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[代理服务器]接受连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    // 停止代理服务器
    public static void stop() {
        running = false;
        try {
            if (server_socket != null && !server_socket.isClosed()) {
                server_socket.close();
            }
            if (thread_pool != null) {
                thread_pool.shutdownNow();
            }
            System.out.println("[代理服务器]已停止");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
