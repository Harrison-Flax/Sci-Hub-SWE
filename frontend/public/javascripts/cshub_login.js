var isEmailAlreadyRegistered = false;
var isEmailEmpty = true;
var isEmailInvalid = true;

var emailEmptyError = "Field 'Email' must be filled in.<br/>";
var emailInvalidError = "Your email address must be valid.<br/>";
var emailAlreadyRegisteredError = "Your email address has already been registered.<br/>";

/**
 * Validation before form submission
 */
function checkFormValid() {
    if (!isEmailAlreadyRegistered) {
        document.getElementById("warningMessage").innerText = "No user matched to given email.";
        $("#warningModal").modal();
        $("#warningModal").modal('open');
        return false;
    }
    return true;
}

/**
 * Validate the currently entered email
 * and send an AJAX request to the backend to check if the email already exists
 */
function checkValidEmail() {
    var email = document.getElementById("email").value;
    var obj = {
        email: email
    };
    if (!email) {
        isEmailEmpty = true;
        document.getElementById("emailMsg").innerHTML = emailEmptyError;
    } else {
        isEmailEmpty = false;
        var re = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,6}$/;
        isEmailInvalid = !re.test(email);

        if (isEmailInvalid) {
            document.getElementById("emailMsg").innerHTML = emailInvalidError;
        } else {
            $.ajax({
                url: "/user/isEmailExisted",
                data: JSON.stringify(obj),
                headers: {
                    'Content-Type': 'application/json'
                },
                type: "POST",
                success: function (data) {
                    console.log("AJAX response:", data);
                    if (false || "error" in data) {
                        isEmailAlreadyRegistered = true;
                        document.getElementById("emailMsg").innerHTML = "";
                    } else {
                        isEmailAlreadyRegistered = false;
                        document.getElementById("emailMsg").innerHTML = "No user matched.";
                    }
                }
            });
        }
    }
}
