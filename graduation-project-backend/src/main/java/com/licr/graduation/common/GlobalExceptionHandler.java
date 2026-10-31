package com.licr.graduation.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> business(BusinessException e) { return Result.fail(e.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> valid(MethodArgumentNotValidException e) { return Result.fail(e.getBindingResult().getFieldError().getDefaultMessage()); }
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> violation(ConstraintViolationException e) { return Result.fail(e.getMessage()); }
    @ExceptionHandler(Exception.class)
    public Result<Void> other(Exception e) { return Result.fail("服务器内部错误"); }
}
