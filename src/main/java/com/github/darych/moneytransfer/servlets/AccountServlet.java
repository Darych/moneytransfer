package com.github.darych.moneytransfer.servlets;

import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.services.AccountService;
import com.google.common.net.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Singleton
public class AccountServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(AccountServlet.class);

    @Inject
    private AccountService accountService;

    /**
     * Get account by its internal id.
     *
     * GET localhost:8080/account/{id}
     * @param request
     * @param response
     * @return 200 if account exist. Example of json:
     * {
     *     "name": "user1",
     *     "balance": 100,
     *     "id": 1
     * }
     * @return 400 if id could not be parsed.
     * @return 500 if there was some internal server error.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        int id = -1;
        try {
            id = Integer.parseInt(request.getPathInfo().substring(1));
        } catch (NumberFormatException e) {
            String errMsg = "Account id must be an integer.";
            logger.error(errMsg, e);
            setErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, errMsg);
            return;
        }

        try {
            Account account = accountService.getById(id);
            if (account == null) {
                setErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, String.format("Account with id %d could not be found.", id));
            } else {
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().print(objectMapper.writeValueAsString(account));
                response.setContentType(MediaType.JSON_UTF_8.toString());
            }
        } catch (IOException e) {
            String errMsg = String.format("Could not get Account for id %d.", id);
            logger.error(errMsg, e);
            setErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errMsg);
        }
    }

    /**
     * Create account in system.
     *
     * POST localhost:8080/account
     * Example of input body JSON:
     * {
     * "name": "user1",
     * "balance": 100
     * }
     * @param request
     * @param response
     * @return 201 if account created successfully. Example of JSON:
     * {"name":"user1","balance":100.0,"id":1}
     * @return 400 if input JSON could not be parsed.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        BufferedReader reader = readRequestBody(request, response);
        if (reader == null) {
            return;
        }

        try {
            Account acc = objectMapper.readValue(reader, Account.class);
            acc = accountService.save(acc);
            response.getWriter().print(objectMapper.writeValueAsString(acc));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (IOException e) {
            String errMsg = "Failed to parse account JSON.";
            logger.error(errMsg);
            setErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, errMsg);
        }
    }
}
