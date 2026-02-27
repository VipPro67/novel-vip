package com.novel.vippro.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class HttpLoggingFilter implements jakarta.servlet.Filter {

	private static final Logger logger = LogManager.getLogger(HttpLoggingFilter.class);
	@Value("${server.environment:production}")
    private String env;
	@Override
	public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String uri = request.getRequestURI();
		if (uri.contains("/actuator")) {
			chain.doFilter(req, res);
			return; 
		}
		logger.info(">>> START: {} {}", request.getMethod(), uri);

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		long start = System.currentTimeMillis();

		try {
			chain.doFilter(wrappedRequest, wrappedResponse); 
		} finally {
			long duration = System.currentTimeMillis() - start;
			logger.info("<<< END: {} {} - Status: {} - Time: {}ms", 
					request.getMethod(), uri, wrappedResponse.getStatus(), duration);
			if("development".equalsIgnoreCase(env)) {
				if(wrappedRequest.getContentAsByteArray().length > 0) {
					logger.info("Request Body: {}", new String(wrappedRequest.getContentAsByteArray()));
				}
				logger.info("Response Body: {}", new String(wrappedResponse.getContentAsByteArray()));
			}
			wrappedResponse.copyBodyToResponse();
		}
	}
}
