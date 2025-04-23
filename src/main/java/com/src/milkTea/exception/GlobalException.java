package com.src.milkTea.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

    // Xử lí lỗi validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        errorResponse.put("message", "VALIDATION_ERROR");
        errorResponse.put("details", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Xử lí lỗi cú pháp JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    // Xử lí lỗi trùng lặp
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", ex.getMessage());
        msg.put("details", ex.getDetails());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
    }

    // Xử lí lỗi quan hệ dữ liệu
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleSQLIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
    }

    // Xử lí xác thực
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
    }

    // Xử lí lỗi liên quan đến page khi gọi trang
    @ExceptionHandler(PageException.class)
    public ResponseEntity<Map<String, Object>> handlePageException(PageException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    // Xử lí lỗi không tìm thấy
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(NotFoundException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
    }

    // Status exception custom
    @ExceptionHandler(StatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatusException(StatusException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    // Xử lí lỗi không tìm thấy thuộc tính của entity
    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReferenceException(PropertyReferenceException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
    }

    // Xử lí lỗi phân quyền
    @ExceptionHandler(AuthorizeException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizeException(AuthorizeException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
    }

    // Xử lí lỗi giao dịch
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionException(TransactionException ex) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("message", "error");
        msg.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(msg);
    }
}
