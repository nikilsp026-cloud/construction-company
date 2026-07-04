package com.construction.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Central exception handling for the whole application.
 * <p>
 * Before this class existed, ANY unhandled exception thrown from a controller
 * (a failed file write, a database constraint violation, a null pointer, ...)
 * propagated all the way up to Spring Boot's default error mechanism, which -
 * with no custom {@code /error} mapping or view - rendered the generic
 * "Whitelabel Error Page". That is what admins were seeing when adding
 * images or deleting records: the underlying cause differs per action, but
 * the symptom (an uncaught exception with nowhere to go) was the same.
 * <p>
 * This handler:
 * 1. Logs the real error (with stack trace) server-side, so it's actually
 *    diagnosable.
 * 2. Sends the user back to where they came from with a clear, human
 *    readable flash message instead of a stack trace.
 * 3. Never leaks internal details (SQL, file paths, stack traces) to the browser.
 * <p>
 * As a last line of defence, {@code templates/error.html} additionally
 * replaces the default Whitelabel page for any error that reaches Spring
 * Boot's {@code /error} endpoint directly (e.g. 404s, or errors raised
 * outside controller methods).
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Where to send the user back to after a recoverable error. Prefers the
     * page they submitted the form from; falls back to a sensible default
     * depending on whether they were in the admin panel or the public site.
     */
    private String safeRedirectTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        String path = request.getRequestURI();
        return (path != null && path.startsWith("/admin")) ? "redirect:/admin/dashboard" : "redirect:/";
    }

    // -------------------------------------------------------------------
    // Not found (bad id in a URL, deleted-in-the-meantime record, etc.)
    // -------------------------------------------------------------------
    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public String handleNotFound(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        log.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        ra.addFlashAttribute("errorMessage", "The item you were looking for could not be found. It may have already been deleted.");
        return safeRedirectTarget(request);
    }

    // -------------------------------------------------------------------
    // Uploaded file too large
    // -------------------------------------------------------------------
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request, RedirectAttributes ra) {
        log.warn("Upload rejected (too large) on {}: {}", request.getRequestURI(), ex.getMessage());
        ra.addFlashAttribute("errorMessage", "That file is too large. Please upload an image smaller than 10 MB.");
        return safeRedirectTarget(request);
    }

    // -------------------------------------------------------------------
    // Any other problem reading/writing an uploaded file
    // -------------------------------------------------------------------
    @ExceptionHandler({IOException.class, MultipartException.class})
    public String handleFileError(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        log.error("File handling error on {}", request.getRequestURI(), ex);
        ra.addFlashAttribute("errorMessage", "We couldn't process the uploaded file. Please check the file and try again.");
        return safeRedirectTarget(request);
    }

    // -------------------------------------------------------------------
    // Database constraint violations - e.g. deleting a record that is still
    // referenced elsewhere, or saving with a missing required value.
    // -------------------------------------------------------------------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request, RedirectAttributes ra) {
        log.error("Data integrity violation on {}", request.getRequestURI(), ex);
        ra.addFlashAttribute("errorMessage", "This action couldn't be completed because the record is still linked to other data, or a required field was missing.");
        return safeRedirectTarget(request);
    }

    // -------------------------------------------------------------------
    // Bad input the user (or a malformed request) supplied
    // -------------------------------------------------------------------
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleBadInput(RuntimeException ex, HttpServletRequest request, RedirectAttributes ra) {
        log.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "The request could not be processed.");
        return safeRedirectTarget(request);
    }

    // -------------------------------------------------------------------
    // Access denied (authenticated but not authorized)
    // -------------------------------------------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletRequest request, Model model) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        model.addAttribute("statusCode", 403);
        model.addAttribute("errorMessage", "You don't have permission to access that page.");
        return "error";
    }

    // -------------------------------------------------------------------
    // Catch-all - guarantees the Whitelabel Error Page is never shown for
    // any exception raised inside a controller.
    // -------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, HttpServletRequest request, RedirectAttributes ra) {
        log.error("Unexpected error handling {} {}", request.getMethod(), request.getRequestURI(), ex);
        ra.addFlashAttribute("errorMessage", "Something went wrong on our end. Please try again, and contact support if the problem continues.");
        return safeRedirectTarget(request);
    }
}
