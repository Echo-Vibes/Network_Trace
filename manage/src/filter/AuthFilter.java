package filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path;
        if (contextPath != null && !contextPath.isEmpty()) {
            relativePath = path.substring(contextPath.length());
        }

        // 放行：登录页面、登录接口、静态资源（如果有的话）
        if (relativePath.equals("/login") || relativePath.equals("/login.html") || relativePath.equals("/") || relativePath.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("admin") != null) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect("login.html");
        }
    }

    @Override
    public void destroy() {
    }
}