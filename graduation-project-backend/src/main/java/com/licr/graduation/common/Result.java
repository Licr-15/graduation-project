package com.licr.graduation.common;

/** 统一接口响应对象，保证前端获得一致的数据结构。 */
public record Result<T>(boolean success, String message, T data) {
    public static <T> Result<T> ok(T data) { return new Result<>(true, "操作成功", data); }
    public static <T> Result<T> ok(String message, T data) { return new Result<>(true, message, data); }
    public static <T> Result<T> fail(String message) { return new Result<>(false, message, null); }
}
