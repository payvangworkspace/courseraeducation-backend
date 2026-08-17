package com.pv.couseae.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class HitCounterFilter extends OncePerRequestFilter {

    private final AtomicLong totalHits = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> urlHits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, AtomicLong> statusHits = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        totalHits.incrementAndGet();

        String path = request.getRequestURI();
        urlHits.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();

        filterChain.doFilter(request, response);

        int status = response.getStatus();
        statusHits.computeIfAbsent(status, k -> new AtomicLong(0)).incrementAndGet();
    }

    public long getTotalHits() {
        return totalHits.get();
    }

    public ConcurrentHashMap<String, AtomicLong> getUrlHits() {
        return urlHits;
    }

    public ConcurrentHashMap<Integer, AtomicLong> getStatusHits() {
        return statusHits;
    }
}
