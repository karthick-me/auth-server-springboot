package com.secureauth.authserver.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiSuccessResponse extends ApiResponse{
    private Object data;

    public ApiSuccessResponse(String message, Object data, int statusCode){
        super(true, message, statusCode);
        this.data = data;
    }
}
