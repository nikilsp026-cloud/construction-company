package com.construction.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces Spring Boot's default BasicErrorController, which has two
 * competing response paths (an HTML view vs. a JSON/Map body) chosen by
 * content negotiation. For plain browser requests to a missing resource
 * (e.g. /favicon.ico), that negotiation sometimes picked the Map path while
 * the response's Content-Type was still text/html, throwing a secondary
 * HttpMessageNotWritableException on every such request. Always rendering
 * the same "error" view sidesteps that ambiguity entirely.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;

        model.addAttribute("status", status);
        model.addAttribute("error", switch (status) {
            case 404 -> "Not Found";
            case 403 -> "Forbidden";
            case 400 -> "Bad Request";
            default -> "Unexpected Error";
        });
        return "error";
    }
}
