package com.secureauth.authserver.common.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@NoArgsConstructor
public class ApiResponse {

    private boolean success;
    private String message;
    private HttpStatusCode statusCode;

    public ApiResponse(boolean success, String message,
                       HttpStatusCode statusCode) {
        this.success = success;
        this.message = message;
        this.statusCode = statusCode;
    }
}
