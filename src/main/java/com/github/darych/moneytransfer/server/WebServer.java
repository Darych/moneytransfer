package com.github.darych.moneytransfer.server;

import com.github.darych.moneytransfer.Application;
import com.github.darych.moneytransfer.model.StorageModule;
import com.github.darych.moneytransfer.servlets.ServletsModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private Server server;

    private WebServer(Server server) {
        this.server = server;
    }

    public static WebServer create(String host, int port, ThreadPool threadPool, List<AbstractModule> modules) {
        Server server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(host);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        Injector injector = Guice.createInjector(modules);

        ServletContextHandler servletContext = new ServletContextHandler(server, "/");
        servletContext.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContext.addServlet(DefaultServlet.class, "/");

        return new WebServer(server);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            logger.error("Error occured while stopping web server.", e);
        } finally {
            server.destroy();
        }
    }
}
