package proxy;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import config.ConfigLoader;
import db.history_DAO;

// implements Runnable接口，使得每个连接都在独立线程中处理
public class proxy_handler implements Runnable {
    private Socket client_socket; // 浏览器与代理间的连接
    private history_DAO dao; // 数据库访问对象，用于记录访问历史

    public proxy_handler(Socket socket) {
        this.client_socket = socket;
        this.dao = new history_DAO();
    }

    // 处理请求
    @Override
    public void run() {
        try {
            // 读取浏览器请求：字节流按行读取为字符流
            BufferedReader reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));

            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                client_socket.close();
                return;
            }

            System.out.println("[代理处理]接收到请求: " + line);

            // 解析请求行，格式：请求方法 url http版本
            String parts[] = line.split(" ");
            if (parts.length < 2) {
                client_socket.close();
                return;
            }

            String method = parts[0];
            String url = parts[1];

            // // 提取域名
            // String domain = extract_domain(url, method);

            // // 黑名单检测
            // if (dao.is_blacklisted(domain)) {
            //     System.out.println("[代理处理]URL被黑名单拦截: " + url);
            //     send_block(client_socket);
            //     client_socket.close();
            //     return;
            // }

            
            // 提取域名
            String domain = extract_domain(url, method);
            System.out.println("[代理处理] 原始URL: " + url);
            System.out.println("[代理处理] 提取域名: " + domain);
            System.out.println("[代理处理] 请求方法: " + method);

            // 黑名单检测
            boolean isBlocked = dao.is_blacklisted(domain);
            System.out.println("[代理处理] 黑名单检查结果: " + isBlocked);

            if (isBlocked) {
                System.out.println("[代理处理] URL被黑名单拦截: " + url);
                send_block(client_socket);
                client_socket.close();
                return;
            }




            // 记录访问历史
            String user_name = System.getProperty("user.name");
            Date visit_time = new Date();
            dao.history_add(user_name, url, visit_time);

            // 转发请求
            forward_request(client_socket, method, url, reader);

        } catch (Exception e) {
            System.err.println("[代理处理]处理请求时发生错误: " + e.getMessage());
        } finally {
            try {
                client_socket.close();
            } catch (IOException e) {
                System.err.println("[代理处理]关闭连接时发生错误: " + e.getMessage());
            }
        }
    }

    // 从 URL 中提取域名
    private String extract_domain(String url, String method) {
        // HTTPS的CONNECT请求，去除端口号
        if (method.equalsIgnoreCase("CONNECT")) {
            int index1 = url.indexOf(':');
            if(index1 > 0){
                url = url.substring(0, index1);
            }   
            return url;
        } 
        
        // HTTP请求，去除协议、路径、端口号
        String domain = url;
        if (domain.startsWith("http://")) {
            domain = domain.substring(7);
        }

        int index2 = domain.indexOf('/');
        if (index2 > 0) {
            domain = domain.substring(0, index2);
        }

        int index3 = domain.indexOf(':');
        if (index3 > 0) {
            domain = domain.substring(0, index3);
        }

        return domain;
    }

    // 发送拦截界面到浏览器
    private void send_block(Socket socket) throws IOException {
        String title = ConfigLoader.get("block.page.title", "Access Blocked");
        String message = ConfigLoader.get("block.page.message", "This website has been blocked by administrator.");
        String block_page = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<title>" + title + "</title></head>" +
                "<body style='text-align:center;margin-top:100px'>" +
                "<h1 style='color:red'>" + title + "</h1>" +
                "<p>" + message + "</p>" +
                "</body></html>";

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html; charset=UTF-8");
        out.println("Content-Length: " + block_page.getBytes("UTF-8").length);
        out.println("Connection: close");
        out.println();
        out.println(block_page);
        out.flush();
    }

    // 转发请求到目标服务器，并将响应返回给浏览器
    private void forward_request(Socket client_socket, String method, String url, BufferedReader client_reader)
            throws Exception {

        // HTTPS
        if (method.equalsIgnoreCase("CONNECT")) {
            // url 格式: www.google.com:443
            String[] parts = url.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            System.out.println("[代理处理] HTTPS 隧道: " + host + ":" + port);

            // 返回 200 Connection Established
            PrintWriter out = new PrintWriter(client_socket.getOutputStream());
            out.println("HTTP/1.1 200 Connection Established");
            out.println("Proxy-agent: ProxyServer/1.0");
            out.println();
            out.flush();

            // 建立双向隧道
            Socket target_socket = new Socket(host, port);

            // 两个线程：浏览器 → 目标网站，目标网站 → 浏览器
            Thread t1 = new Thread(() -> {
                try {
                    byte[] buffer = new byte[8192];
                    int len;
                    InputStream in = client_socket.getInputStream();
                    OutputStream out2 = target_socket.getOutputStream();
                    while ((len = in.read(buffer)) != -1) {
                        out2.write(buffer, 0, len);
                        out2.flush();
                    }
                } catch (IOException e) {
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    byte[] buffer = new byte[8192];
                    int len;
                    InputStream in = target_socket.getInputStream();
                    OutputStream out2 = client_socket.getOutputStream();
                    while ((len = in.read(buffer)) != -1) {
                        out2.write(buffer, 0, len);
                        out2.flush();
                    }
                } catch (IOException e) {
                }
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            target_socket.close();
            return;
        }

        // HTTP
        // 提取主机和路径
        String host;
        String path;
        int port = 80; // HTTP 默认端口

        String tmp = url;
        if (tmp.startsWith("http://")) {
            tmp = tmp.substring(7);
        }
        
        /*else if (tmp.startsWith("https://")) {
            tmp = tmp.substring(8);
            port = 443; // HTTPS 端口
        }*/

        // 分离主机和路径
        int slash_index = tmp.indexOf('/');
        if (slash_index != -1) {
            host = tmp.substring(0, slash_index);
            path = tmp.substring(slash_index);
        } else {
            host = tmp;
            path = "/";
        }

        // 去掉 host 中可能带的端口号
        if (host.contains(":")) {
            String[] host_parts = host.split(":");
            host = host_parts[0];
            // 如果 URL 中指定了端口，使用那个端口
            if (host_parts.length > 1) {
                port = Integer.parseInt(host_parts[1]);
            }
        }

        System.out.println("[代理处理] HTTP 转发: " + host + ":" + port + path);

        // 连接目标服务器
        Socket target_socket = new Socket(host, port);
        PrintWriter target_out = new PrintWriter(target_socket.getOutputStream());

        // 发送请求行
        target_out.println(method + " " + path + " HTTP/1.1");
        target_out.println("Host: " + host);

        // 转发剩余请求头
        String line;
        while ((line = client_reader.readLine()) != null && !line.isEmpty()) {
            if (line.toLowerCase().startsWith("proxy-")) {
                continue;
            }
            target_out.println(line);
        }
        target_out.println();
        target_out.flush();

        // 读取响应并转发给浏览器
        InputStream target_in = target_socket.getInputStream();
        OutputStream client_out = client_socket.getOutputStream();

        byte[] buffer = new byte[8192];
        int len;
        while ((len = target_in.read(buffer)) != -1) {
            client_out.write(buffer, 0, len);
        }

        target_in.close();
        target_out.close();
        target_socket.close();
    }
}
