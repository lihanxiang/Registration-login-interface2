package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getSession().
                getAttribute("username") == null){
            ((HttpServletResponse)servletResponse).sendRedirect("preLogin.action");
        } else {
            filterChain.doFilter(servletRequest,
                    servletResponse);
        }
    }
}
