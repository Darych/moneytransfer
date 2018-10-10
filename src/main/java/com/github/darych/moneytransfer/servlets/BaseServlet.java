package com.github.darych.moneytransfer.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

abstract class BaseServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected void setErrorResponse(HttpServletResponse response, int code, String errMsg) {
        response.setStatus(code);
        try {
            response.getWriter().print(errMsg);
        } catch (IOException writerException) {
            logger.error("Failed to write response body.", writerException);
        }
    }

    protected BufferedReader readRequestBody(HttpServletRequest request, HttpServletResponse response) {
        BufferedReader reader;
        try {
            reader = request.getReader();
        } catch (IOException e) {
            String errMsg = "Failed to read request body.";
            String additionalMsg = String.format("\nRequest: %s", request.toString());
            logger.error(errMsg.concat(additionalMsg), e);
            setErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, errMsg);
            reader = null;
        }
        return reader;
    }

}
