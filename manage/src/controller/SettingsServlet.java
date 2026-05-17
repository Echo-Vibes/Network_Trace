package controller;

import service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/settings")
public class SettingsServlet extends HttpServlet {
    private AdminService service = new AdminService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");

        String type = req.getParameter("type");
        String result;

        if ("username".equals(type)) {
            String newUsername = req.getParameter("newUsername");
            if (newUsername == null || newUsername.trim().isEmpty()) {
                result = "{\"msg\": \"用户名不能为空\"}";
            } else {
                service.updateUsername(newUsername.trim());
                // 更新 session 中的 admin 信息
                HttpSession session = req.getSession(false);
                if (session != null) {
                    model.Admin admin = service.getAdmin();
                    if (admin != null) {
                        session.setAttribute("admin", admin);
                    }
                }
                result = "{\"msg\": \"用户名修改成功\"}";
            }
        } else if ("password".equals(type)) {
            String oldPassword = req.getParameter("oldPassword");
            String newPassword = req.getParameter("newPassword");
            String msg = service.updatePassword(oldPassword, newPassword);
            result = "{\"msg\": \"" + escapeJson(msg) + "\"}";
        } else {
            result = "{\"msg\": \"无效请求\"}";
        }

        resp.getWriter().print(result);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}