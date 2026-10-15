package com.apimonix.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException{

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        try{
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            if(duration>500){
                log.warn("SLOW REQUEST: {} {} -> {} [{}ms]",
                        req.getMethod(),
                        req.getRequestURI(),
                        res.getStatus(),
                        duration);
            } else{
                log.info("{} {} -> {} [{}ms]",
                        req.getMethod(),
                        req.getRequestURI(),
                        res.getStatus(),
                        duration);
            }
        }
    }
}
