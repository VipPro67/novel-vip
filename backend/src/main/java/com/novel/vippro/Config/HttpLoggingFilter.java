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
import java.nio.charset.StandardCharsets;

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

    String requestBody = getPayload(wrappedRequest.getContentAsByteArray(), request.getCharacterEncoding());
    String responseBody = getPayload(wrappedResponse.getContentAsByteArray(), response.getCharacterEncoding());

    logger.info("HTTP {} {} from={} headers={} query={} payload={} | responseStatus={} duration={}ms",
        request.getMethod(),
        request.getRequestURI(),
        request.getRemoteAddr(),
        getHeaders(request),
        request.getQueryString(),
        requestBody,
        wrappedResponse.getStatus(),
        duration
    );
    wrappedResponse.copyBodyToResponse();
  }

  private String getPayload(byte[] buf, String encoding) {
    if (buf == null || buf.length == 0) return "";
    try {
      return new String(buf, encoding != null ? encoding : StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return "[unknown]";
    }
  }

  private String getHeaders(HttpServletRequest request) {
    var sb = new StringBuilder();
    request.getHeaderNames().asIterator().forEachRemaining(name -> {
      sb.append(name).append("=").append(request.getHeader(name)).append("; ");
    });
    return sb.toString();
  }
}
