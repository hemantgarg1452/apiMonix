package com.apimonix.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException{
        String key = resolveKey(request);

        Bucket bucket = buckets.computeIfAbsent(key, this::createBucket);

        if(bucket.tryConsume(1)){
            response.addHeader(
                    "X-Rate-Limit-Remaining",
                    String.valueOf(bucket.getAvailableTokens())
            );
            filterChain.doFilter(request, response);
        }else {
            log.warn("Rate limit exceeded for key: [{}] path: [{}]",
                    key, request.getRequestURI());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {
                        "status": 429,
                        "error": "Too many requests. Please slow down bruh.",
                        "timestamp": "%s"
                    }
                    """.formatted(java.time.Instant.now()));
        }

    }

    private String resolveKey(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");

        if(authHeader!= null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            return "user:" + token.substring(Math.max(0, token.length() - 8));
        }

        String ip = request.getHeader("X-Forward-For");
        if(ip==null) ip = request.getRemoteAddr();
        return "ip:" + ip;
    }


    private Bucket createBucket(String key) {
        int capacity = key.startsWith("ip:") ? 20 : 60;

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return request.getRequestURI().equals("/actuators/health");
    }
}
