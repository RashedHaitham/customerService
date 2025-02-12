package com.ABIC.CustomerRequest.util;

import org.springframework.stereotype.Component;

@Component
public class ResponseUtils {


    public static <T> Response<T> success(int statusCode, T data) {
        return new Response<>("success", statusCode, data);
    }


    public static <T> Response<T> error(int statusCode, T data) {
        return new Response<>("error", statusCode, data);
    }
}
