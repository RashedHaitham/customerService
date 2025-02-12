package com.ABIC.CustomerRequest.config;

import com.ABIC.CustomerRequest.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.startsWith("/api/auth/") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }


        Optional<String> tokenOptional = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> "jwtToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();

        if (tokenOptional.isPresent()) {
            String token = tokenOptional.get();
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractEmail(token);
                List<String> roles = jwtUtil.extractRoles(token);

                SecurityContext context = new SecurityContextImpl(jwtUtil.getAuthentication(username, roles));
                SecurityContextHolder.setContext(context);

                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write("Invalid JWT Token");
            }
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("JWT Token is missing");
        }
    }

}
