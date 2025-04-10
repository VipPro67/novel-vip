package com.novel.vippro.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
        this.statusCode = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation successful", data, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, HttpStatus.BAD_REQUEST.value());
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(false, message, null, statusCode);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, HttpStatus.BAD_REQUEST.value());
    }

    public static <T> ApiResponse<T> error(String message, T data, int statusCode) {
        return new ApiResponse<>(false, message, data, statusCode);
    }
}