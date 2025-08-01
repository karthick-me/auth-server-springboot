package com.secureauth.authserver.common.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatusCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class ApiSuccessResponse extends ApiResponse{
    private Object data;

    public ApiSuccessResponse(String message, Object data, HttpStatusCode statusCode){
        super(true, message, statusCode);
        this.data = data;
    }
}
