package co.com.menor.commerce_core_bd.shared.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            int status = response.getStatus();
            if (status >= 500) {
                log.error("[HTTP] {} {} → {} ({}ms)", request.getMethod(), request.getRequestURI(), status, duracion);
            } else if (status >= 400) {
                log.warn("[HTTP] {} {} → {} ({}ms)", request.getMethod(), request.getRequestURI(), status, duracion);
            } else {
                log.info("[HTTP] {} {} → {} ({}ms)", request.getMethod(), request.getRequestURI(), status, duracion);
            }
        }
    }
}
