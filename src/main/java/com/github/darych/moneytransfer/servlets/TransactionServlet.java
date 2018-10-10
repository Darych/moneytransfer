package com.github.darych.moneytransfer.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.darych.moneytransfer.model.Account;
import com.github.darych.moneytransfer.model.Storage;
import com.github.darych.moneytransfer.model.StorageException;
import com.github.darych.moneytransfer.model.Transaction;
import com.github.darych.moneytransfer.services.AccountService;
import com.github.darych.moneytransfer.services.TransactionService;
import com.github.darych.moneytransfer.services.TransactionServiceException;
import com.google.common.net.MediaType;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Singleton
public class TransactionServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServlet.class);

    @Inject
    private TransactionService txService;

    /**
     * Transfer money from one account to another.
     * Example of request body JSON:
     * {
     * "from": 1,
     * "to": 2,
     * "amount": 25
     * }
     * @param request
     * @param response
     * @return 200 if money was transferred successfully.
     * @return 400 if could not parse transaction JSON.
     * @return 406 if transaction processing failed.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        BufferedReader reader = readRequestBody(request, response);
        if (reader == null) {
            return;
        }
        try {
            Transaction tx = objectMapper.readValue(reader, Transaction.class);
            txService.processTx(tx);
        } catch (TransactionServiceException e) {
            String errMsg = "Failed to process transaction.";
            logger.error(errMsg);
            setErrorResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, errMsg);
        } catch (IOException e) {
            String errMsg = "Failed to parse transaction JSON.";
            logger.error(errMsg);
            setErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, errMsg);
        }
    }
}
