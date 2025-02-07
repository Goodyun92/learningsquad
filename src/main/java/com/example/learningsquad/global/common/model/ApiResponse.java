package com.example.learningsquad.global.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ApiResponse<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer totalItemCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer pageNumber;
    @Enumerated(EnumType.STRING)
    private ResultCode resultCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resultMessage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // Response
    public ApiResponse(ResultCode resultCode, String resultMessage, T data) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.data = data;
    }

    // Response with pagination
    public ApiResponse(ResultCode resultCode, String resultMessage, T data, Integer totalItemCount, Integer pageNumber) {
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.data = data;
        this.totalItemCount = totalItemCount;
        this.pageNumber = pageNumber;
    }

}
