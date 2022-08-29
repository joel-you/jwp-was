package webserver.http.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.Cookie;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class RequestHeader {
    private static final Logger logger = LoggerFactory.getLogger(RequestHeader.class);

    private static final String HEADER_SEPARATOR = ": ";
    private static final String COOKIE = "Cookie";
    public static final String HEADER_TO_COOKIE_SEPARATOR = "; ";
    public final static String MAP_KEY_VALUE_SEPARATOR = "=";


    private final Map<String, Object> headers;
    private Cookie cookie = new Cookie();

    public RequestHeader(List<String> headers) {
        this.headers = headers.stream()
                .map(request -> request.split(HEADER_SEPARATOR))
                .collect(toMap(s -> s[0], s -> s[1]));
        parseCookies(String.valueOf(this.headers.getOrDefault(COOKIE, "")));
    }

    public RequestHeader(Map<String, Object> headerMap) {
        this.headers = headerMap;
        parseCookies(String.valueOf(this.headers.getOrDefault(COOKIE, "")));
    }

    private void parseCookies(String headerValue) {
        if (headerValue == null || headerValue.equals("")) {
            return;
        }

        logger.debug("headerValue : {}", headerValue);

        String[] cookies = headerValue.split(HEADER_TO_COOKIE_SEPARATOR);

        logger.debug("cookies : {}", cookies);

        Map<String, Object> map = Arrays.stream(cookies).
                map(cookie -> cookie.split(MAP_KEY_VALUE_SEPARATOR)).
                collect(Collectors.toMap(datas -> datas[0], datas -> datas[1], (a, b) -> b));

        map.entrySet().forEach(entry -> {
            cookie.setCookie(entry.getKey(), entry.getValue());
        });
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public int getContentLength() {
        return headers.get("Content-Length") == null ? 0 : Integer.parseInt(headers.get("Content-Length").toString());
    }

    public void setHeader(String key, Object value) {
        this.headers.put(key, value);
    }

    public String getSessionId() {
        return this.cookie.getSessionId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestHeader header1 = (RequestHeader) o;
        return Objects.equals(headers, header1.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers);
    }
}
