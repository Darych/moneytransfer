package com.github.darych.moneytransfer.servlets;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.server.WebServer;
import com.github.darych.moneytransfer.services.AccountService;
import com.github.darych.moneytransfer.services.AccountServiceException;
import com.google.common.net.MediaType;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Providers;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Account servlet tests")
class AccountServletTest {
    private static WebServer server;
    private static AccountService accService = mock(AccountService.class);
    private CloseableHttpClient client = HttpClients.createDefault();

    @BeforeAll
    static void setup() throws Exception {
        List<AbstractModule> modules = new LinkedList<>();
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AccountService.class).toProvider(Providers.of(accService));
            }
        });
        modules.add(new ServletModule() {
            @Override
            public void configureServlets() {
                bind(AccountServlet.class);
                serve("/account", "/account/*").with(AccountServlet.class);
            }
        });
        server = WebServer.create("localhost", 8080, new QueuedThreadPool(4, 1), modules);
        server.start();
    }

    @AfterAll
    static void cleanup() {
        server.stop();
    }

    @BeforeEach
    void setupTest() {
        reset(accService);
    }

    @AfterEach
    void cleanupTest() throws IOException {
        client.close();
        verifyNoMoreInteractions(accService);
    }

    @Nested
    @DisplayName("Create account tests")
    class CreateAccount {

        private String url = "http://localhost:8080/account";
        private HttpPost request = new HttpPost(url);

        CreateAccount() {
            request.setHeader("Content-type", "application/json");
        }

        @Test
        @DisplayName("On success returns 201 Created")
        void onSuccessReturnsCreated() throws Exception {
            StringEntity json = new StringEntity("{\n" +
                    "\"name\":\"user1\",\n" +
                    "\"balance\":100.1\n" +
                    "}");
            request.setEntity(json);
            Account acc = new Account("user1", 100.1);
            when(accService.save(any())).thenReturn(acc);

            HttpResponse response = client.execute(request);

            assertEquals(HttpServletResponse.SC_CREATED, response.getStatusLine().getStatusCode());
            verify(accService).save(acc);
        }

        @Nested
        class Negative {
            @Test
            @DisplayName("Return 400 when JSON is incorrect")
            void onIncorrectJsonReturns400() throws Exception {
                StringEntity json = new StringEntity("{\n" +
                        "\"something\":\"wrong\"\n" +
                        "}");
                request.setEntity(json);

                HttpResponse response = client.execute(request);

                assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            }

            @Test
            @DisplayName("Return 400 when JSON is partially correct")
            void onPartiallyCorrectJsonReturns400() throws Exception {
                StringEntity json = new StringEntity("{\n" +
                        "\"name\":\"user1\"\n" +
                        "}");
                request.setEntity(json);

                HttpResponse response = client.execute(request);

                assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            }

            @Test
            @DisplayName("Return 500 on creation exception")
            void return500OnCreationException() throws Exception {
                when(accService.save(any())).thenThrow(new RuntimeException());

                HttpResponse response = client.execute(request);

                assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            }
        }
    }

    @Nested
    @DisplayName("Get account tests")
    class GetAccount {
        private String url = "http://localhost:8080/account/1";
        private HttpGet request = new HttpGet(url);
        private Account account = new Account(1, "user1", 10.125);

        @Test
        @DisplayName("Get already created account")
        void getAlreadyCreatedAccount() throws Exception {
            when(accService.getById(1)).thenReturn(account);

            HttpResponse response = client.execute(request);

            assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
            assertEquals(MediaType.JSON_UTF_8.toString().replace(" ", ""), response.getEntity().getContentType().getValue());
            verify(accService).getById(1);
        }

        @Test
        @DisplayName("Return 404 on getting absent account")
        void return404OnGettingAbsentAccount() throws Exception {
            when(accService.getById(1)).thenReturn(null);

            HttpResponse response = client.execute(request);

            assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
            verify(accService).getById(1);
        }

        @Nested
        class Negative {
            @Test
            @DisplayName("Return 400 on non-numeric id")
            void return400OnNonNumericId() throws Exception {
                request.setURI(URI.create("http://localhost:8080/account/wrong"));

                HttpResponse response = client.execute(request);

                assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            }

            @Test
            @DisplayName("Return 500 on account retrieve exception")
            void return500OnAccountRetrieveException() throws IOException {
                when(accService.getById(1)).thenThrow(new AccountServiceException(""));

                HttpResponse response = client.execute(request);

                assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatusLine().getStatusCode());
                verify(accService).getById(1);
            }
        }
    }
}
