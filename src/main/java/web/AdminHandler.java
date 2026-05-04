package web;

import db.history_DAO;
import db.history_DAO.BlacklistEntry;
import db.history_DAO.HistoryEntry;
import db.history_DAO.HistoryPage;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AdminHandler {

    private static final history_DAO dao = new history_DAO();

    public static String handle(String method, String path, String body) {
        try {
            // GET /api/history?page=1&size=20&user=&keyword=
            if (path.startsWith("/api/history") && "GET".equalsIgnoreCase(method)) {
                return handleHistory(path);
            }
            // GET /api/blacklist
            if (path.equals("/api/blacklist") && "GET".equalsIgnoreCase(method)) {
                return handleBlacklistList();
            }
            // POST /api/blacklist
            if (path.equals("/api/blacklist") && "POST".equalsIgnoreCase(method)) {
                return handleBlacklistAdd(body);
            }
            // DELETE /api/blacklist/{id}
            if (path.startsWith("/api/blacklist/") && "DELETE".equalsIgnoreCase(method)) {
                int id = Integer.parseInt(path.substring("/api/blacklist/".length()));
                return handleBlacklistDelete(id);
            }
            return jsonError(404, "Not Found");
        } catch (Exception e) {
            return jsonError(500, e.getMessage());
        }
    }

    private static String handleHistory(String path) {
        Map<String, String> params = parseQuery(path);
        int page = Math.max(1, parseInt(params.get("page"), 1));
        int size = Math.min(100, Math.max(1, parseInt(params.get("size"), 20)));
        String user = params.get("user");
        String keyword = params.get("keyword");

        HistoryPage result = dao.search_history(page, size, user, keyword);

        List<String> items = new ArrayList<>();
        for (HistoryEntry e : result.data) {
            items.add(JsonUtil.obj(
                    "id", String.valueOf(e.id),
                    "userName", JsonUtil.esc(e.userName),
                    "url", JsonUtil.esc(e.url),
                    "visitTime", JsonUtil.date(e.visitTime)
            ));
        }

        String json = JsonUtil.obj(
                "total", String.valueOf(result.total),
                "data", JsonUtil.arr(items)
        );
        return jsonOk(json);
    }

    private static String handleBlacklistList() {
        List<BlacklistEntry> list = dao.blacklist_list();
        List<String> items = new ArrayList<>();
        for (BlacklistEntry e : list) {
            items.add(JsonUtil.obj(
                    "id", String.valueOf(e.id),
                    "url", JsonUtil.esc(e.url),
                    "addTime", JsonUtil.date(e.addTime)
            ));
        }
        return jsonOk(JsonUtil.arr(items));
    }

    private static String handleBlacklistAdd(String body) {
        String url = extractJsonString(body, "url");
        if (url == null || url.isEmpty()) {
            return jsonError(400, "url is required");
        }
        int id = dao.blacklist_add(url);
        if (id < 0) {
            return jsonError(500, "Failed to add blacklist");
        }
        return jsonOk(JsonUtil.obj("id", String.valueOf(id)));
    }

    private static String handleBlacklistDelete(int id) {
        boolean ok = dao.blacklist_delete(id);
        return jsonOk(JsonUtil.obj("ok", ok ? "true" : "false"));
    }

    // 简单 JSON body 解析，从 {"url": "xxx.com"} 中提取 url 值
    private static String extractJsonString(String json, String key) {
        if (json == null) return null;
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(':', keyIdx);
        if (colonIdx < 0) return null;
        int start = json.indexOf('"', colonIdx);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    // ---- HTTP 响应构建 ----

    public static void writeResponse(OutputStream out, String content) throws IOException {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        pw.print("HTTP/1.1 200 OK\r\n");
        pw.print("Content-Type: application/json; charset=UTF-8\r\n");
        pw.print("Access-Control-Allow-Origin: *\r\n");
        pw.print("Content-Length: " + body.length + "\r\n");
        pw.print("Connection: close\r\n");
        pw.print("\r\n");
        pw.flush();
        out.write(body);
        out.flush();
    }

    private static String jsonOk(String data) {
        return data;
    }

    private static String jsonError(int code, String msg) {
        return JsonUtil.obj("error", JsonUtil.esc(msg));
    }

    // 解析 URL 查询字符串
    private static Map<String, String> parseQuery(String path) {
        Map<String, String> map = new LinkedHashMap<>();
        int qi = path.indexOf('?');
        if (qi < 0) return map;
        String qs = path.substring(qi + 1);
        for (String pair : qs.split("&")) {
            int ei = pair.indexOf('=');
            if (ei >= 0) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, ei), "UTF-8");
                    String val = URLDecoder.decode(pair.substring(ei + 1), "UTF-8");
                    map.put(key, val);
                } catch (UnsupportedEncodingException ignored) {}
            }
        }
        return map;
    }

    private static int parseInt(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
