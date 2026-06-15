package app_programming_development.Class.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 클릭재킹 방지
        response.setHeader("X-Frame-Options", "DENY");
        // MIME 타입 스니핑 방지
        response.setHeader("X-Content-Type-Options", "nosniff");
        // XSS 보호 (구형 브라우저)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        // HTTPS 강제 (1년)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // Referrer 정책
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 권한 정책
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        filterChain.doFilter(request, response);
    }
}
