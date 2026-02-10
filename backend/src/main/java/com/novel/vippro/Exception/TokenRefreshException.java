package com.novel.vippro.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@Getter
public class TokenRefreshException extends RuntimeException {
    private final String token;

    public TokenRefreshException(String token, String message) {
        super(message);
        this.token = token;
    }

    public TokenRefreshException(String message) {
        super(message);
        this.token = null;
    }
}