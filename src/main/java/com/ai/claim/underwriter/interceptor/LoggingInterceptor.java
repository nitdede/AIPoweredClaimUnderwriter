package com.ai.claim.underwriter.interceptor;

import com.ai.claim.underwriter.exception.InvalidClaimException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interceptor for logging HTTP requests and responses.
 * Captures request details, execution time, and response status.
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "startTime";

    /**
     * Pre-handle method called before the controller method is invoked.
     * Logs incoming request details and records the start time.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        if(request.getRequestURI().equalsIgnoreCase("/ingestion/saveDocument")){
            List<String> builder = new ArrayList<>();

            logger.info("Note: Document ingestion request - payload not logged for security reasons.");
            if(request.getParameter("policyId") == null) {
               builder.add("policyId");
            }
            if(request.getParameter("customerId") == null) {
                builder.add("customerId");
            }

            if(request.getParameter("policyNumber") == null) {
                builder.add("policyNumber");
            }

            if(!builder.isEmpty()){
                throw  new InvalidClaimException("Missing required parameters in request: " +  String.join(", ", builder));
                }

        }

        return true; // Continue with the request
    }

    /**
     * Post-handle method called after the controller method is invoked but before the view is rendered.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) {
        logger.debug("Post-handle processing for: {}", request.getRequestURI());
    }

    /**
     * After-completion method called after the complete request has finished.
     * Logs response status and execution time.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long executionTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        logger.info("========== Request Completed ==========");
        logger.info("Request URI: {}", request.getRequestURI());
        logger.info("HTTP Method: {}", request.getMethod());
        logger.info("Response Status: {}", response.getStatus());
        logger.info("Execution Time: {} ms", executionTime);
        
        if (ex != null) {
            logger.error("Exception occurred during request processing: ", ex);
        }
        
        logger.info("=======================================");
    }

    /**
     * Extracts the client's IP address from the request, considering proxy headers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Logs all request headers.
     */
    private void logRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            StringBuilder headers = new StringBuilder("Request Headers: ");
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.append(headerName).append("=").append(headerValue).append("; ");
            }
            logger.debug(headers.toString());
        }
    }
}
