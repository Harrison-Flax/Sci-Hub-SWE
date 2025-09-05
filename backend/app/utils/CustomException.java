package utils;

import lombok.Getter;
import lombok.Setter;

/**
 * @author: Grayson Wu
 * @description: CustomException
 * @create: 06/28/2019 15:52
 **/
@Getter
@Setter
public class CustomException extends Exception {

    private int code;
    private String msg;

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
