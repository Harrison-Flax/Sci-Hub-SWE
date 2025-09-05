/**
 * declare regular expression pattern for field input
 */
const REGEX_PATTERN = {
    /*
    password
    1. At least one digit [0-9]
    2. At least one lowercase character [a-z]
    3. At least one uppercase character [A-Z]
    4. At least one special character [*.!@#$%^&(){}[]:;<>,.?/~_+-=|\]
    5. At least 8 characters in length, but no more than 32.
    */
    PASSWORD: /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}\[\]:;<>,.?\/~_+-=|]).{8,32}$/,
    URL: /https?:\/\/(www\.)?[\-a-zA-Z0-9@:%\._\+~#=]{1,256}\.[a-zA-Z0-9()\/]{1,6}\b([-a-zA-Z0-9()@:%_\+\.~#?&//=]*)/,
    PHONE_NUMBER: /^(\+\\d{1,2}\\s)?\(?\d{3}\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$/,
    EMAIL: /[a-z0-9!#$%&'*+/=?^_‘{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_‘{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/,
    SMUID: /^[0-9]{8}$/,
    GPA: /^([0-3](\.[0-9]{1,2})?)|(4(\.0)?)$/
}

/**
 * TODO: this function should be deprecated once parsely has been fully integrated
 * @param fieldId
 * @param fieldMessageId
 * @param maxLength
 * @param isRequired
 */
function validateField(fieldId, fieldMessageId, maxLength, isRequired) {
//    alert("fieldname: " + fieldName + "  maxLength: " + maxLength + " isRequired: " + isRequired);
    var maxlength = eval(maxLength);
    var pattern = "^\\s*?\\S.{0," +  (maxlength-1) +  "}?\\s*$";
    var msg = "";
    if (isRequired) {
        msg = "This is a required field and should be no longer than " + maxlength + " characters";
    } else {
        msg = "Field should be no longer than "+ maxlength +" characters";
    }
    document.getElementById(fieldMessageId).setAttribute('data-error', msg);
    document.getElementById(fieldId).pattern = pattern;
}

function validateRegex(fieldId, regex) {
    var element = document.getElementById(fieldId);
    const pattern = REGEX_PATTERN[regex] || regex;
    if (pattern.test(element.value))
        element.setCustomValidity("");
    else
        element.setCustomValidity(document.getElementById(fieldId + "Msg")["data-error"]);
}

/**
 * TODO: this function should be removed after parsely.js integration
 * @param urlId
 * @param urlMsgId
 * @param maxLengthString
 */
function validateRequiredUrl(urlId, urlMsgId, maxLengthString) {
    var maxLength = eval(maxLengthString);
    var msg = "This URL is a required field, must start with \"http://\" or \"https://\", and should not be longer than "+ maxLength +" characters"
    var pattern = "^\\s*?(http://)\\S.{0," + (maxLength-8) + "}?\\s*$|^\\s*?(https://)\\S.{0," + (maxLength-9) +"}?\\s*$";
    document.getElementById(urlMsgId).setAttribute('data-error', msg);
    document.getElementById(urlId).pattern = pattern;
}

function validateNoneRequiredFloat(floatId, floatMsgId, maxLengthFloat){
    var maxLength = eval(maxLengthFloat);
    var msg = "This field is a required field, must be a number, no longer than "+ maxLength + " numbers"
    var pattern = "^//d + (//.//d+)?$"
    document.getElementById(floatMsgId).setAttribute('data-error', msg);
    document.getElementById(floatId).pattern = pattern;
}

function validateNoneRequiredUrl(urlId, urlMsgId, maxLengthString) {
    var maxLength = eval(maxLengthString);
    var msg = "This URL must start with \"http://\" or \"https://\" and should not be longer than "+ maxLength +" characters"
    var pattern = "^\\s*$|^\\s*?(http://)\\S.{0," + (maxLength-8) + "}?\\s*$|^\\s*?(https://)\\S.{0," + (maxLength-9) +"}?\\s*$";
    document.getElementById(urlMsgId).setAttribute('data-error', msg);
    document.getElementById(urlId).pattern = pattern;
}

/**
 * set patter or class which mapped to database table
 * @param requiredFieldList
 * @param notRequiredFieldList
 * @param table
 * TODO: this function should be rewrite
 * list of
 */
function setPattern(requiredFieldList, notRequiredFieldList, table) {
    for(var i=0; i<requiredFieldList.length; i++){
        var field = requiredFieldList[i];
        console.log(field)
        var msgId = field + 'Msg';
        console.log(msgId)
        var maxlength = eval(table + upperCaseFirst(field) + "MaxLength");
        var pattern = "^\\s*?\\S.{0," +  (maxlength-1) +  "}?\\s*$";
        var msg = "This is a required field and should be no longer than "+ maxlength +" characters"
        document.getElementById(msgId).setAttribute('data-error', msg);
        document.getElementById(field).pattern = pattern;
    }

    for(var i=0; i<notRequiredFieldList.length; i++){
        var field = notRequiredFieldList[i];
        var msgId = field + 'Msg';
        console.log(9999)
        console.log(field)
        console.log(msgId)
        var maxlength = eval(table + upperCaseFirst(field) + "MaxLength");
        var pattern = "(^\\s*$|^\\s*?\\S.{0," +  (maxlength-1) +  "}?\\s*$)";
        var msg = "Field should be no longer than "+ maxlength +" characters";
        // if(field.toLowerCase().indexOf("number") != -1){
        //     pattern = "(^\\s*$|^\\s*?\\d{1," + (maxlength) + "}?\\s*$)";
        //     msg = "Field should only contain numbers and not longer than "+ maxlength +" characters";
        // }
        document.getElementById(msgId).setAttribute('data-error', msg);
        document.getElementById(field).pattern = pattern;
        console.log( document.getElementById(field).pattern)
    }
}





function validateTextarea(fieldId, tableName, buttonId) {
    var maxlength = eval(tableName + upperCaseFirst(fieldId) + "MaxLength");
    var msg = "Field should be no longer than "+ maxlength +" characters";
    var value = document.getElementById(fieldId).value.trim();
    if(value.length > maxlength){
        document.getElementById(fieldId+ "Msg").innerHTML = msg;
        // document.getElementById(buttonId).disabled = true;
    }else{
        document.getElementById(fieldId+ "Msg").innerHTML = "";
        // document.getElementById(buttonId).disabled = false;
    }
}

function validateTextInput(fieldId, tableName){
    var maxlength = eval(tableName + upperCaseFirst(fieldId) + "MaxLength");
    document.getElementById(fieldId).setAttribute('maxlength', maxlength+1);
    var msg = "Field should be no longer than "+ maxlength +" characters";
    // var value = document.getElementById(fieldId).value.trim();
    var value = document.getElementById(fieldId).value;
    if(value.length >= maxlength+1){
        document.getElementById(fieldId+ "Msg").innerHTML = msg;
    }else if (document.getElementById(fieldId).hasAttribute('required') && value.trim().length==0){
        document.getElementById(fieldId+ "Msg").innerHTML = "This is a required field, thus can not be empty";
    }else{
        document.getElementById(fieldId+ "Msg").innerHTML = "";
    }
}

function upperCaseFirst(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}


function validateUserName(nameFieldId, nameFieldMsgId, maxLengthString) {

}