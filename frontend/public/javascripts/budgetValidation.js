$(document).ready(function () {
    $("#minBudget, #maxBudget").on("input", function () {
        const minBudget = parseFloat($("#minBudget").val());
        const maxBudget = parseFloat($("#maxBudget").val());

        $("#minBudgetMsg").text("");
        $("#maxBudgetMsg").text("");

        let isValid = true;

        if (isNaN(minBudget) || minBudget <= 0) {
            $("#minBudgetMsg").text("Min Budget must be greater than 0.");
            isValid = false;
        }

        if (isNaN(maxBudget) || maxBudget <= 0) {
            $("#maxBudgetMsg").text("Max Budget must be greater than 0.");
            isValid = false;
        }

        if (!isNaN(minBudget) && !isNaN(maxBudget) && minBudget > maxBudget) {
            $("#minBudgetMsg").text("Min Budget must be less than Max Budget.");
            $("#maxBudgetMsg").text("Max Budget must be greater than Min Budget.");
            isValid = false;
        }

        $("#challengeRegister").prop("disabled", !isValid);
    });
});