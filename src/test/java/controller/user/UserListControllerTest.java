package controller.user;

import controller.Controller;
import db.DataBase;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.http.request.Request;
import webserver.http.request.RequestBody;
import webserver.http.request.RequestHeader;
import webserver.http.request.RequestLine;
import webserver.http.response.Response;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserListControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(UserListControllerTest.class);

    private Controller controller;

    @BeforeEach
    void controllerSetup() {
        DataBase.addUser(new User("aaaa", "aaaa", "aaaa", "aaaa%40aaaa.com"));
        controller = new UserListController();
    }

    @Test
    @DisplayName("유저목록 확인 - 로그인한 상태")
    void testUserList_WithSuccess() throws Exception {
        RequestLine requestLine = new RequestLine("GET /user/list?userId=aaaa&password=aaaa&name=aaaa HTTP/1.1");
        RequestHeader requestHeader = new RequestHeader(Map.of("Host", "localhost:8080", "Connection", "keep-alive", "Cookie", "logined=true", "Accept", "*/*"));
        RequestBody requestBody = new RequestBody();

        Request request = new Request(requestLine, requestHeader, requestBody);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Response response = new Response(out);

        controller.service(request, response);

        assertThat(out.toString()).contains("HTTP/1.1 200 OK");
        assertThat(out.toString()).contains("Content-Type: text/html");
    }

    @Test
    @DisplayName("유저목록 확인 - 로그인 안한 상태")
    void testUserList_WithFail() throws Exception {
        RequestLine requestLine = new RequestLine("GET /user/list?userId=aaaa&password=aaaa&name=aaaa HTTP/1.1");
        RequestHeader requestHeader = new RequestHeader(Map.of("Host", "localhost:8080", "Connection", "keep-alive", "Accept", "*/*"));
        RequestBody requestBody = new RequestBody();

        Request request = new Request(requestLine, requestHeader, requestBody);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Response response = new Response(out);

        controller.service(request, response);

        assertThat(out.toString()).contains("HTTP/1.1 302 Found");
        assertThat(out.toString()).contains("Location: /");
    }

}