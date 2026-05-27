package com.github.paulinagazwa.oss.coupon_service.exception;

import com.github.paulinagazwa.oss.coupon_service.api.model.FieldError;
import com.github.paulinagazwa.oss.coupon_service.api.model.Problem;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String DETAIL_VALIDATION = "Request validation failed";

	@ExceptionHandler(InvalidDiscountException.class)
	public ResponseEntity<Problem> handleInvalidDiscount(InvalidDiscountException ex, HttpServletRequest request) {
		return buildProblem(HttpStatus.BAD_REQUEST, ProblemTitles.VALIDATION_FAILED, ex.getMessage(), request.getRequestURI());
	}

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(CouponNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, ProblemTitles.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CouponAlreadyExistsException.class)
    public ResponseEntity<Problem> handleConflict(CouponAlreadyExistsException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemTitles.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(CouponAlreadyRedeemedException.class)
    public ResponseEntity<Problem> handleAlreadyRedeemed(CouponAlreadyRedeemedException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.CONFLICT, ProblemTitles.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

	@ExceptionHandler(CouponAlreadyInUseException.class)
	public ResponseEntity<Problem> handleAlreadyLock(CouponAlreadyInUseException ex, HttpServletRequest request) {
		return buildProblem(HttpStatus.CONFLICT, ProblemTitles.CONFLICT, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(CouponCountryMismatchException.class)
	public ResponseEntity<Problem> handleCountryMismatch(CouponCountryMismatchException ex, HttpServletRequest request) {
		return buildProblem(HttpStatus.FORBIDDEN, ProblemTitles.FORBIDDEN, ex.getMessage(), request.getRequestURI());
	}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError()
                        .field(error.getField())
                        .message(error.getDefaultMessage()))
                .toList();

        Problem problem = new Problem()
                .title(ProblemTitles.VALIDATION_FAILED)
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(DETAIL_VALIDATION)
                .instance(request.getRequestURI())
                .errors(fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Problem> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTitles.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<Problem> buildProblem(HttpStatus status, String title, String detail, String instance) {
        Problem problem = new Problem()
                .title(title)
                .status(status.value())
                .detail(detail)
                .instance(instance);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
