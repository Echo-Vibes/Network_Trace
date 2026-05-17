package controller;

import model.Admin;
import service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private AdminService service = new AdminService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Admin admin = service.login(username, password);
        if (admin != null) {
            HttpSession session = req.getSession();
            session.setAttribute("admin", admin);
            resp.getWriter().print("{\"success\": true, \"msg\": \"µ«¬º≥…π¶\"}");
        } else {
            resp.getWriter().print("{\"success\": false, \"msg\": \"’À∫≈ªÚ√‹¬Î¥ÌŒÛ\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET /login?action=logout
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        resp.sendRedirect("login.html");
    }
}