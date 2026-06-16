package app_programming_development.Class.security.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public XssHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String requestBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String contentType = request.getContentType();
        // JSON 바디는 sanitize하면 따옴표 등 구조 문자가 HTML 엔티티로 변환돼 파싱 불가 → 그대로 유지
        if (contentType != null && contentType.startsWith("application/json")) {
            this.body = requestBody.getBytes(StandardCharsets.UTF_8);
        } else {
            this.body = sanitize(requestBody).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override public int read() { return bais.read(); }
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener readListener) {}
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return value != null ? sanitize(value) : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) return null;
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = sanitize(values[i]);
        }
        return sanitized;
    }

    private String sanitize(String value) {
        if (value == null) return null;
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .replace("(", "&#40;")
                .replace(")", "&#41;")
                .replace("javascript:", "")
                .replace("vbscript:", "")
                .replace("onload=", "")
                .replace("onerror=", "");
    }
}
