package com.toannguyen.authify.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.toannguyen.authify.service.impl.AppUserDetailService;
import com.toannguyen.authify.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtRequestFilter extends OncePerRequestFilter {

    AppUserDetailService appUserDetailService;
    JwtUtil jwtUtil;

    static final List<String> PUBLIC_URLS = List.of("/login", "/register", "/send-reset-opt", "/reset-password",
            "/logout");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        // logRequest(request);
        if (PUBLIC_URLS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        // log.info("hello {}", path);
        String jwt = null;
        String email = null;

        // 1. Check the authorization
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.info("check {}", jwt);
        }

        // 2. If not found in header, check cookie
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            log.info("Cookies: {}", cookies == null ? "null" : Arrays.toString(cookies));

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        log.info("Cookie - jwt {}", jwt);
                        break;
                    }
                }
            }
        }

        // 3. Validate the token and set security context
        if (jwt != null) {
            email = jwtUtil.extractEmail(jwt);
            log.info("Email: " + email);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = appUserDetailService.loadUserByUsername(email);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void logRequest(HttpServletRequest request) {
        log.info("=== Incoming Request ===");
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("ServletPath: {}", request.getServletPath());

        // Log headers
        request.getHeaderNames().asIterator()
                .forEachRemaining(header -> log.info("Header {} = {}", header, request.getHeader(header)));

        // Log cookies
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.info("Cookies: NONE");
        } else {
            for (Cookie cookie : cookies) {
                log.info("Cookie {} = {}", cookie.getName(), cookie.getValue());
            }
        }
        log.info("========================");
    }

}
