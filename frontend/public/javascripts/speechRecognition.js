var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition;
var SpeechGrammarList = SpeechGrammarList || webkitSpeechGrammarList;
var SpeechRecognitionEvent = SpeechRecognitionEvent || webkitSpeechRecognitionEvent;

var diagnostic = document.querySelector('.result');
var logs = document.querySelector('.logs');
var bg = document.querySelector('html');
var firstRes = document.querySelector('.firstRes');

// this function is based on webkitSpeechRecognition package.
// this instance can monitor and recognize voice into text.
function getRecognitionInstance() {
    return new SpeechRecognition();
}

// start the instance to set up some configurations such as language or whether to keep on monitoring
async function startRecognition(recognition, resultFunc, stopFunc, errorFunc, continuous = true) {

    recognition.lang = 'en-US';
    recognition.continuous = continuous;
    // recognition.interimResults = true;
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;
    await recognition.start();

    recognition.onresult = async function (event) {
        // The SpeechRecognitionEvent results property returns a SpeechRecognitionResultList object
        // The SpeechRecognitionResultList object contains SpeechRecognitionResult objects.
        // It has a getter so it can be accessed like an array
        // The [last] returns the SpeechRecognitionResult at the last position.
        // Each SpeechRecognitionResult object contains SpeechRecognitionAlternative objects that contain individual results.
        // These also have getters so they can be accessed like arrays.
        // The [0] returns the SpeechRecognitionAlternative at position 0.
        // We then return the transcript property of the SpeechRecognitionAlternative object
        var last = event.results.length - 1;
        var res = event.results[last][0].transcript.trim();
        resultFunc(res);
    };

    recognition.onend = async function () {
        sessionStorage.setItem("micState", 1);
    };

    recognition.onend = async function () {
        recognition.stop();
        sessionStorage.setItem("micState", 0);
        if (stopFunc != undefined){
            stopFunc();
        }
    };


    recognition.onerror = async function (event) {
        if (errorFunc != undefined) {
            errorFunc(event.error);
        }
    };

    return null;
}

async function stopRecognition(recognition) {
    await recognition.stop();
}


function startSpeaking(voice) {
    var voices1 = [];
    var synth = window.speechSynthesis;
    voices1 = synth.getVoices();
    var utterThis = new SpeechSynthesisUtterance(voice);
    var selectedOption = undefined;
    if (voices1[0] !== undefined) {
        selectedOption = voices1[0].name;
    }
    for(let i = 0; i < voices1.length ; i++) {
        if(voices1[i].name === selectedOption) {
            utterThis.voice = voices1[i];
        }
    }
    utterThis.pitch = 1;
    utterThis.rate = 1;
    synth.speak(utterThis);
}


export {
    startRecognition,
    getRecognitionInstance,
    stopRecognition,
    startSpeaking
}



