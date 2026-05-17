package controller;

import service.MonitorService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/monitor")
public class MonitorServlet extends HttpServlet {

    private final MonitorService service = new MonitorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        req.setCharacterEncoding("GBK");
        resp.setContentType("application/json; charset=GBK");
        resp.getWriter().print(service.getMonitorDataJson());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }
}