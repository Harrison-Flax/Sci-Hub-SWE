import { startRecognition, getRecognitionInstance, startSpeaking, stopRecognition } from "./speechRecognition.js";
import {compareTwoStrings, findBestMatch} from "./string_similarity.js";

var recognition = null;
var candidates = new Map();
var candidatesKeys = null;
var replyField = null;
var requestField = null;
var noMatchReply = null;
var speakFromFilterFlag = false;
var threshold = 0.5; // constant to decide the voice recognized string similarity with candidates

$("#speak-from-filter").click(function changeFlag() {
    speakFromFilterFlag = !speakFromFilterFlag;
});

$("#speak-nasa").click(function speakInList() {
    console.log("mic clicked");
    updateCandidates("#nasaText");
    var micState = sessionStorage.getItem('micState') == null ? 0 : sessionStorage.getItem('micState');
    if (micState == 0 || speakFromFilterFlag == false || recognition == undefined){
        voiceJump( "Incorrect Name");
    }
});

$("#speak-community").click(function speakInList() {
    console.log("mic clicked");
    updateCandidates("#communityText");

    var micState = sessionStorage.getItem('micState') == null ? 0 : sessionStorage.getItem('micState');
    if (micState == 0 || speakFromFilterFlag == false || recognition == undefined){
        voiceJump( "Incorrect Name");
    }
});

$("#speak-oneway").click(function speakInList() {
    // Candidate is what you may select from the page from your speech
    updateCandidates("#directText");
    var micState = sessionStorage.getItem('micState') == null ? 0 : sessionStorage.getItem('micState');
    voiceJump( "Incorrect Name");

});

// Candidate is what you may select from the page from your speech
function updateCandidates(fieldName){
    var getCandidates = $(fieldName).text();
    console.log("sup music" + fieldName + "----" + getCandidates);
    candidates.clear();
    candidatesKeys = [];
    getCandidates = JSON.parse(getCandidates);
    for(var i = 0; i < getCandidates.length; i++){
        candidatesKeys.push(getCandidates[i].name.toLowerCase());
        candidates.set(getCandidates[i].name.toLowerCase(), getCandidates[i].url);
    }
    console.log("candidates:" + candidates);
}

// if jumped from other pages to do some initial setup
// mic status is stored in session
async function voiceJump(getNoMatchReply) {
    noMatchReply = getNoMatchReply;
    var micState = sessionStorage.getItem('micState') == null ? 0 : sessionStorage.getItem('micState');

    if (micState == 1 && recognition != undefined) {
        await stopRecognition(recognition);
        sessionStorage.setItem("micState", 0);
    }
    else{
        sessionStorage.setItem("micState", 1);
        recognition = getRecognitionInstance();
        await startRecognition(recognition, voiceAction);
    }
}

// check whether voice exists in candidates. Otherwise, say "incorrect name"
function voiceAction(voice) {

    var value = voice.toLowerCase();
    candidates.set("mata", "/dataset/DiRA");
    candidates.forEach(function (key, value) {
        console.log(key + "," + value);
    });
    var res = findBestMatch(value, candidatesKeys);

    if (res[1].rating > threshold) {
        value = res[1].target;
    }
    M.toast({html: value});
    if (candidates.has(value)) {
        M.toast({html: value});

        directJump(candidates.get(value));
    } else {
        M.toast({html: noMatchReply})
        startSpeaking(noMatchReply);
    }
}

function directJump(value) {
    window.location.href = value;
}

export {
    voiceJump
}
