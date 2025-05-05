package com.novel.vippro.Payload.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControllerResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private int statusCode;

    public ControllerResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
        this.statusCode = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }

    public ControllerResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = success ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }

    public static <T> ControllerResponse<T> success(T data) {
        return new ControllerResponse<>(true, "Operation successful", data, HttpStatus.OK.value());
    }

    public static <T> ControllerResponse<T> success(String message, T data) {
        return new ControllerResponse<>(true, message, data, HttpStatus.OK.value());
    }

    public static <T> ControllerResponse<T> error(String message) {
        return new ControllerResponse<>(false, message, null, HttpStatus.BAD_REQUEST.value());
    }

    public static <T> ControllerResponse<T> error(String message, int statusCode) {
        return new ControllerResponse<>(false, message, null, statusCode);
    }

    public static <T> ControllerResponse<T> error(String message, T data) {
        return new ControllerResponse<>(false, message, data, HttpStatus.BAD_REQUEST.value());
    }

    public static <T> ControllerResponse<T> error(String message, T data, int statusCode) {
        return new ControllerResponse<>(false, message, data, statusCode);
    }
}