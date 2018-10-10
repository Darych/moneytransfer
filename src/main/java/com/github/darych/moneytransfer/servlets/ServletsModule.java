package com.github.darych.moneytransfer.servlets;

import com.google.inject.servlet.ServletModule;

public class ServletsModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(AccountServlet.class);
        bind(TransactionServlet.class);

        serve("/account", "/account/*").with(AccountServlet.class);
        serve("/transaction").with(TransactionServlet.class);
    }
}
