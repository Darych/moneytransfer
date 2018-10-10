package com.github.darych.moneytransfer.servlets;

import com.github.darych.moneytransfer.model.Transaction;
import com.github.darych.moneytransfer.server.WebServer;
import com.github.darych.moneytransfer.services.TransactionService;
import com.github.darych.moneytransfer.services.TransactionServiceException;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Providers;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("Transaction servlet tests")
class TransactionServletTest {
    private static WebServer server;
    private static TransactionService txService = mock(TransactionService.class);
    private CloseableHttpClient client = HttpClients.createDefault();
    private String url = "http://localhost:8080/transaction";
    private HttpPost request = new HttpPost(url);

    @BeforeAll
    static void setup() throws Exception {
        List<AbstractModule> modules = new LinkedList<>();
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(TransactionService.class).toProvider(Providers.of(txService));
            }
        });
        modules.add(new ServletModule() {
            @Override
            public void configureServlets() {
                bind(TransactionServlet.class);
                serve("/transaction").with(TransactionServlet.class);
            }
        });
        server = WebServer.create("localhost", 8080, new QueuedThreadPool(4, 1), modules);
        server.start();
    }

    @BeforeEach
    void setupTest() {
        reset(txService);
    }

    @AfterEach
    void cleanupTest() throws IOException {
        client.close();
        verifyNoMoreInteractions(txService);
    }

    @Test
    @DisplayName("Return 200 on successful money transfer between accounts")
    void return200OnSuccessfulTransfer() throws IOException {
        Transaction tx = new Transaction(1, 2, 100);
        doNothing().when(txService).processTx(tx);
        StringEntity json = new StringEntity("{\n" +
                "\"from\":1,\n" +
                "\"to\":2,\n" +
                "\"amount\":100\n" +
                "}");
        request.setEntity(json);

        HttpResponse response = client.execute(request);

        assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
        verify(txService).processTx(tx);
    }

    @Nested
    class Negative {
        @Test
        @DisplayName("Return 406 if transaction failed")
        void return406IfTxFailed() throws IOException {
            doThrow(new TransactionServiceException("")).when(txService).processTx(any());

            StringEntity json = new StringEntity("{\n" +
                    "\"from\":1,\n" +
                    "\"to\":2,\n" +
                    "\"amount\":100\n" +
                    "}");
            request.setEntity(json);

            HttpResponse response = client.execute(request);

            assertEquals(HttpServletResponse.SC_NOT_ACCEPTABLE, response.getStatusLine().getStatusCode());
            verify(txService).processTx(any());
        }

        @Test
        @DisplayName("Return 400 if JSON invalid")
        void return400IfJsonInvalid() throws IOException {
            StringEntity json = new StringEntity("{\n" +
                    "\"from\":1,\n" +
                    "\"amount\":100\n" +
                    "}");
            request.setEntity(json);

            HttpResponse response = client.execute(request);

            assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        }
    }
}