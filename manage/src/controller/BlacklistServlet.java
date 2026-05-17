package controller;

import model.Blacklist;
import service.BlacklistService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/blacklist")
public class BlacklistServlet extends HttpServlet {
    private BlacklistService service = new BlacklistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");

        String deleteId = req.getParameter("delete");
        String msg = "";
        if (deleteId != null) {
            try {
                service.delete(Integer.parseInt(deleteId));
                msg = "删除成功";
            } catch (NumberFormatException e) {
                msg = "无效ID";
            }
        }

        List<Blacklist> list = service.getAll();

        StringBuilder json = new StringBuilder();
        json.append("{\"msg\":\"").append(escapeJson(msg)).append("\",");
        json.append("\"list\":[");
        for (int i = 0; i < list.size(); i++) {
            Blacklist b = list.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(b.getId()).append(",");
            json.append("\"url\":\"").append(escapeJson(b.getUrl())).append("\",");
            json.append("\"addTime\":\"").append(escapeJson(b.getAddTime())).append("\"");
            json.append("}");
        }
        json.append("]}");

        resp.getWriter().print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");

        String url = req.getParameter("url");
        String msg;

        if (url != null && !url.trim().isEmpty()) {
            if (url.contains(".")) {
                service.add(url.trim());
                msg = "添加成功";
            } else {
                msg = "URL 格式无效（需包含 .）";
            }
        } else {
            msg = "URL 不能为空";
        }

        List<Blacklist> list = service.getAll();

        StringBuilder json = new StringBuilder();
        json.append("{\"msg\":\"").append(escapeJson(msg)).append("\",");
        json.append("\"list\":[");
        for (int i = 0; i < list.size(); i++) {
            Blacklist b = list.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(b.getId()).append(",");
            json.append("\"url\":\"").append(escapeJson(b.getUrl())).append("\",");
            json.append("\"addTime\":\"").append(escapeJson(b.getAddTime())).append("\"");
            json.append("}");
        }
        json.append("]}");

        resp.getWriter().print(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}