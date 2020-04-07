package com.covid19.controllers;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;

public class Covid19ErrorController implements ErrorController  {
    private static final Logger LOGGER = LoggerFactory.getLogger(Covid19ErrorController.class);
    private static final String ERROR = "error";

    @RequestMapping("/"+ERROR)
    public String handleError() {
        LOGGER.error("Some error occurred, propably wrong URL.");
        return ERROR;
    }

    @RequestMapping("/"+ERROR)
    public String handleError(final HttpServletRequest request) {
        final Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            final Integer statusCode = Integer.parseInt(status.toString());
            if(statusCode == HttpStatus.NOT_FOUND.value())
                return ERROR+"-404";
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value())
                return ERROR+"-500";
        }
        return ERROR;
    }

    @Override
    public String getErrorPath() {
        return "/"+ERROR;
    }
}
