package app_programming_development.Class.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String contentType = request.getContentType();

        // JSON/form 요청에만 XSS 래핑 적용 (multipart 제외)
        if (contentType != null && !contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            filterChain.doFilter(new XssHttpServletRequestWrapper(request), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
