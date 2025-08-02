package com.secureauth.authserver.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiErrorResponse extends ApiResponse{
    private String errorCode;

    public ApiErrorResponse(String message, int statusCode,
                            String errorCode){
        super(false, message, statusCode);
        this.errorCode = errorCode;
    }
}
