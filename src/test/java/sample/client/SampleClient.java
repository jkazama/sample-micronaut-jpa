package sample.client;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

import org.junit.Test;

import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.http.*;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.reactivex.Flowable;
import sample.util.*;

/**
 * Check api via the simple HTTP.
 * <p>you really login and handle it at the time of "micronaut.security.enabled: true".
 * <p>you want to check with administrator authority, hange "extension.auth.admin: true".
 */
public class SampleClient {
    private static final String ROOT_PATH = "http://localhost:8080";

    // for 「extention.auth.admin: false」
    @Test
    public void usecaseCustomer() {
        try (SimpleTestAgent agent = new SimpleTestAgent()) {
            agent.login("sample", "sample");
            agent.post("withdraw", "/api/asset/cio/withdraw", MapBuilder
                    .of("accountId", "sample")
                    .put("currency", "JPY")
                    .put("absAmount", "200")
                    .build());
            agent.get("unprocessedOut", "/api/asset/cio/unprocessedOut");
        }
    }

    // for 「extention.auth.admin: true」
    @Test
    public void usecaseInternal() {
        String day = DateUtils.dayFormat(TimePoint.now().day());
        try (SimpleTestAgent agent = new SimpleTestAgent()) {
            agent.login("admin", "admin");
            agent.get("findCashInOut", "/api/admin/asset/cio?updFromDay=" + day + "&updToDay=" + day);
        }
    }

    @Test
    public void usecaseBatch() {
        String fromDay = DateUtils.dayFormat(TimePoint.now().day().minusDays(1));
        String toDay = DateUtils.dayFormat(TimePoint.now().day().plusDays(3));
        try (SimpleTestAgent agent = new SimpleTestAgent()) {
            agent.post("processDay", "/api/system/job/daily/processDay");
            agent.post("closingCashOut", "/api/system/job/daily/closingCashOut");
            agent.post("realizeCashflow", "/api/system/job/daily/realizeCashflow");
            agent.get("findAuditEvent", "/api/system/job/audit/event?fromDay=" + fromDay + "&toDay=" + toDay);
        }
    }

    private class SimpleTestAgent implements Closeable {
        private final HttpClient client;
        private Optional<String> sessionId = Optional.empty();

        public SimpleTestAgent() {
            try {
                this.client = HttpClient.create(new URL(ROOT_PATH)).start();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public void close() {
            this.client.close();
        }

        public SimpleTestAgent login(String loginId, String password) {
            Map<String, Object> p = new HashMap<>();
            p.put("loginId", loginId);
            p.put("password", password);
            try {
                @SuppressWarnings("rawtypes")
                HttpResponse<ByteBuffer> res = post("Login", "/api/login", MapBuilder
                        .of("loginId", loginId)
                        .put("password", password)
                        .build());
                if (res.getStatus() == HttpStatus.OK) {
                    String cookieStr = res.getHeaders().get(HttpHeaders.SET_COOKIE).trim();
                    sessionId = Optional.of(cookieStr.substring(0, cookieStr.indexOf(';')));
                }
                return this;
            } catch (HttpClientResponseException e) {
                System.out.println(e.getMessage());
                return this;
            }
        }

        @SuppressWarnings("rawtypes")
        private HttpResponse<ByteBuffer> get(String title, String path) {
            title(title);
            MutableHttpRequest<?> req = HttpRequest.GET(path);
            req.setAttribute("fromDay", "2019-01-16");
            req.setAttribute("toDay", "2019-01-16");
            sessionId.ifPresent((jsessionId) -> req.getHeaders().add(HttpHeaders.COOKIE, jsessionId));
            HttpResponse<ByteBuffer> res = Flowable.fromPublisher(client.exchange(req)).blockingSingle();
            dump(res);
            return res;
        }

        @SuppressWarnings("rawtypes")
        private HttpResponse<ByteBuffer> post(String title, String path) {
            return post(title, path, new HashMap<>());
        }

        @SuppressWarnings("rawtypes")
        private HttpResponse<ByteBuffer> post(String title, String path, Map<Object, Object> body) {
            title(title);
            MutableHttpRequest<?> req = HttpRequest.POST(path, body);
            sessionId.ifPresent((jsessionId) -> req.getHeaders().add(HttpHeaders.COOKIE, jsessionId));
            HttpResponse<ByteBuffer> res = Flowable.fromPublisher(client.exchange(req)).blockingSingle();
            dump(res);
            return res;
        }

        public void title(String title) {
            System.out.println("------- " + title + "------- ");
        }

        @SuppressWarnings("rawtypes")
        public void dump(HttpResponse<ByteBuffer> res) {
            System.out.println(
                    String.format("status: %d, text: %s", res.getStatus().getCode(), res.getStatus().getReason()));
            System.out.println(res.body().toString(StandardCharsets.UTF_8));
        }

    }

}
