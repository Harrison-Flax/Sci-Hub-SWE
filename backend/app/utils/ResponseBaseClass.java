package utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author: Grayson Wu
 * @description: response base class
 * @create: 06/28/2019 14:16
 **/
@Getter
@Setter
public class ResponseBaseClass<T> {
    private int code;
    private String message;
    private T data;

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"message\":\"" + message + '"' +
                ", \"data\":" + data +
                '}';
    }

    /**
     * return success response with data
     */
    public static <T> ResponseBaseClass<T> successResponse(T data){

        ResponseBaseClass<T> generalResponseClass = new ResponseBaseClass<>();

        generalResponseClass.setCode(ResponseStatus.SUCCESS.getCode());
        generalResponseClass.setMessage(ResponseStatus.SUCCESS.getMsg());
        generalResponseClass.setData(data);

        return generalResponseClass;
    }

    /**
     * return fail response with data
     */
    public static ResponseBaseClass<String> exceptionResponse(CustomException customException){

        ResponseBaseClass<String> generalResponseClass = new ResponseBaseClass<>();

        generalResponseClass.setCode(customException.getCode());
        generalResponseClass.setMessage(customException.getMsg());
        generalResponseClass.setData(null);

        return generalResponseClass;
    }

}
