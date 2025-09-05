package utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author: Grayson Wu
 * @description: definition of response status
 * @create: 06/28/2019 16:22
 **/
@Setter
@Getter
@AllArgsConstructor
@ToString
public class ResponseStatus {

    private int code;
    private String msg;

    /**
     * SUCCESS
     */
    public static final ResponseStatus SUCCESS = new ResponseStatus(10000, "Success");

    /**
     *  10001-10100 FOR SYSTEM EXCEPTION
     */
    public static final ResponseStatus UNTITLED = new ResponseStatus(10001, "Unexpected exception");
    public static final ResponseStatus DB_OPERATION_FAIL = new ResponseStatus(10002,
            "Database operation fail");

    /**
     *  10101-10200 FOR GENERAL EXCEPTION
     */
    public static final ResponseStatus PARAMETER_INVALID = new ResponseStatus(10101, "Parameter invalid");
    public static final ResponseStatus PERMISSION_DENY = new ResponseStatus(10102, "Permission deny");
    public static final ResponseStatus USER_ID_INVALID = new ResponseStatus(10103, "User id invalid");
    public static final ResponseStatus JSON_FORMAT_INVALID = new ResponseStatus(10104,
            "Json format invalid");

    /**
     *  10201-10300 FOR MATA RELATED
     */
    public static final ResponseStatus DICT_ENTITY_NOT_MATCH = new ResponseStatus(10201,
            "Each MATA sample sentence should contain at lease one entity from your dictionary");
    public static final ResponseStatus APP_ID_INVALID = new ResponseStatus(10202, "App id invalid");

}
