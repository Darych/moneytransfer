package com.github.darych.moneytransfer;


import com.github.darych.moneytransfer.model.StorageModule;
import com.github.darych.moneytransfer.server.WebServer;
import com.github.darych.moneytransfer.servlets.ServletsModule;
import com.google.inject.AbstractModule;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Main application class.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        int minThreads = 10;
        int maxThreads = 100;
        int idleTimeout = 100;
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);

        List<AbstractModule> modules = new LinkedList<>();
        modules.add(new StorageModule());
        modules.add(new ServletsModule());

        WebServer server = WebServer.create("localhost", 8080, threadPool, modules);
        try {
            server.start();
        } catch (Exception ex) {
            logger.error("Error occurred while running web server.", ex);
            server.stop();
            System.exit(1);
        }
    }
}
