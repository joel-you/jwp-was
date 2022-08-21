package webserver.http.request;

import com.github.jknack.handlebars.internal.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.ContentType;
import webserver.http.HttpMethod;
import webserver.http.session.HttpSession;
import webserver.http.session.SessionManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static model.Constant.JSESSIONID;
import static utils.IOUtils.readData;
import static utils.IOUtils.readLines;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public static final String ROOT_PATH = "/";
    public static final String ROOT_FILE = "/index.html";
    public final static String EXTENSION_SEPARATOR = ".";
    private static final String COOKIE = "Cookie";

    private final RequestLine requestLine;
    private final RequestHeader requestHeader;
    private final RequestBody requestBody;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        List<String> requests = readLines(br);
        this.requestLine = new RequestLine(requests.get(0));
        requests.remove(0);
        this.requestHeader = new RequestHeader(requests);
        this.requestBody = new RequestBody(readData(br, this.requestHeader.getContentLength()));
        initializedSession();
    }

    private void initializedSession() {
        String extension = getRequestPath().substring(getRequestPath().lastIndexOf(EXTENSION_SEPARATOR) + 1);
        if (this.requestHeader.getCookieValue(JSESSIONID).isEmpty() && !ContentType.isStaticExtension(extension)) {
            HttpSession httpSession = SessionManagement.createSession();
            this.requestHeader.setCookie(JSESSIONID, httpSession.getId());
            this.requestHeader.addHeader(COOKIE, String.format(JSESSIONID, "%s; Path=/", httpSession.getId()));
        }
    }

    public HttpRequest(RequestLine requestLine, RequestHeader header, RequestBody requestBody) {
        this.requestLine = requestLine;
        this.requestHeader = header;
        this.requestBody = requestBody;
    }

    public String getRequestPath() {
        return StringUtils.equals(requestLine.getPathWithoutQueryString(), ROOT_PATH) ? getRedirectUrl() : requestLine.getPathWithoutQueryString();
    }

    public String getMethod() {
        return requestLine.getHttpMethod().name();
    }

    public String getPath() {
        return requestLine.getPath().getPath();
    }

    private String getRedirectUrl() {
        return ROOT_FILE;
    }

    public RequestHeader getHeaders() {
        return requestHeader;
    }

    public String getHeader(String key) {
        return (String) requestHeader.getHeaders().get(key);
    }

    public HttpMethod getHttpMethod() {
        return requestLine.getHttpMethod();
    }

    public boolean isPost() {
        return getHttpMethod() == HttpMethod.POST;
    }

    public boolean isGet() {
        return getHttpMethod() == HttpMethod.GET;
    }

    public HttpSession getHttpSession() {
        return SessionManagement.getSession(this.requestHeader.getCookieValue(JSESSIONID));
    }

    public boolean isLogin() {
        return Optional.ofNullable(getHttpSession().getAttribute("user"))
                .isPresent();
    }

    public String getParameter(String key) {
        String value = requestLine.getQueryStringWithoutPathFromPath().get(key);
        if (value == null) {
            value = requestBody.getRequestBodyMap().get(key);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest httpRequest = (HttpRequest) o;
        return Objects.equals(requestLine, httpRequest.requestLine) && Objects.equals(requestHeader, httpRequest.requestHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestLine, requestHeader);
    }
}
