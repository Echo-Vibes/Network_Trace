package controller;

import model.History;
import service.HistoryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
    private HistoryService service = new HistoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");

        String userName = req.getParameter("userName");
        String urlKeyword = req.getParameter("urlKeyword");
        List<History> list = service.getHistory(userName, urlKeyword);

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            History h = list.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(h.getId()).append(",");
            json.append("\"userName\":\"").append(escapeJson(h.getUserName())).append("\",");
            json.append("\"url\":\"").append(escapeJson(h.getUrl())).append("\",");
            json.append("\"visitTime\":\"").append(escapeJson(h.getVisitTime())).append("\"");
            json.append("}");
        }
        json.append("]");

        resp.getWriter().print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}