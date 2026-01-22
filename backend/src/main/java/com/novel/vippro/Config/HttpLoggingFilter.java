package com.novel.vippro.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class HttpLoggingFilter implements jakarta.servlet.Filter {

	private static final Logger logger = LogManager.getLogger(HttpLoggingFilter.class);

	@Override
	public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		long start = System.currentTimeMillis();
		chain.doFilter(wrappedRequest, wrappedResponse);
		long duration = System.currentTimeMillis() - start;

		// String requestBody = getPayload(wrappedRequest.getContentAsByteArray(), request.getCharacterEncoding());

		String queryString = request.getQueryString();
		String fullUri = request.getRequestURI()
				+ (queryString != null && !queryString.isEmpty() ? "?" + queryString : "");
		if(fullUri.contains("/api/health") || fullUri.contains("/ws"))
		{
			return;
		}
		logger.info("{} {} {} {}ms",
				request.getMethod(),
				fullUri,
				wrappedResponse.getStatus(),
				duration);

		// if (requestBody != null && !requestBody.isBlank() ) {
		// 	ObjectMapper mapper = new ObjectMapper();
		// 	try {
		// 		Map<String, Object> bodyMap = mapper.readValue(requestBody, new TypeReference<>() {});
		// 		logger.info("Request Body: {}", bodyMap);
		// 	} catch (Exception e) {
		// 		logger.info("Request Body: {}", requestBody);
		// 	}
		// }
		wrappedResponse.copyBodyToResponse();
	}
}
