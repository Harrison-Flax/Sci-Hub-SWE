import { startRecognition, getRecognitionInstance, startSpeaking, stopRecognition } from "./speechRecognition.js";

var recognition = null;
var micState = null;
var candidateTags = null;


document.querySelector("#linkInput").onkeyup  = function () {
    textSearchToggle();
};

document.querySelector("#headSpeak").onclick = async function () {
    await startVoiceSearch();
};

async function startVoiceSearch() {
    micState = sessionStorage.getItem('micState') == null ? 0 : sessionStorage.getItem('micState');
    console.log(micState);
    if (micState == 1 && recognition != undefined) {
        await stopRecognition(recognition);
    }
    else{
        recognition = getRecognitionInstance();
        await startRecognition(recognition, voiceSearchToggle);
    }
}

// if no candidate (e.g., service), no display dropped down list.
function generalSearchToggle(value) {

    if (value == "") {
        $("#myDiv").css("display", "none");

        $("#myDiv").children().css("display", "none");
    } else {
        var all_hide = 1;
        $("#myDiv *").filter(function () {
            if($(this).text().toLowerCase().indexOf(value) > -1) {
                all_hide = 0;
                $("#myDiv").css("display", "block");
            }
            $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
        if(all_hide == 1){
            $("#myDiv").css("display", "none");
        }
    }
    var candidateTagArray = [];
    $("#myDiv > p:visible").filter(function () {
        candidateTagArray.push($(this).text().toLowerCase().trim());
    });
    candidateTags = candidateTagArray
}

function textSearchToggle() {
    var value = $("#linkInput").val().toLowerCase();
    generalSearchToggle(value);
    sessionStorage.setItem("candidateTags", candidateTags.join(','));
}

// match voice with items in the dropped down list
function voiceSearchToggle(voice) {
    $("#linkInput").val(voice); //variable to store the menu item mentioned in voice
    candidateTags = sessionStorage.getItem('candidateTags') == null ? [] : sessionStorage.getItem('candidateTags').split(",");
    var value = $("#linkInput").val().toLowerCase();
    console.log(candidateTags, value);

    if (candidateTags.includes(value)) {
        directJump(value);
        sessionStorage.setItem("candidateTags", "");
    } else {
        generalSearchToggle(value);
        sessionStorage.setItem("candidateTags", candidateTags.join(','));
    }
}

// jump to the page with value (identified from voice), e.g., API list. Go to site navigation structure, use the value as key to find its corresponding URL and jump.
function directJump(value) {
    $.ajax({
        url: "/adminCenter/allSiteNavigationStructrueAsJson",
        headers: {
            'Content-Type': 'application/json'
        },
        type: "GET"
    }).done(function(data){
        console.log("this:" + JSON.stringify(data));
        data.forEach(function(item, index){

            var obj = {
                fromURL:"/dataset/DiRA",
                toURL:item['url'].substring(item['url'].lastIndexOf(":") + 5)
            }
            console.log("itemhere:" + item);
            if(item["name"].toLowerCase() == value.toLowerCase()){
                $.ajax({
                    url:"/siteNavigationEvent/registerSiteNavigationEvent",
                    data: JSON.stringify(obj),
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    type: "POST"
                }).done(function(data){
                    window.location.href = item['url'];
                })

            }
        })
    })

}