var inputForm = document.querySelector('form');
var inputTxt = document.querySelector('#txt');
var voiceSelect = document.querySelector('.voices-select');
var playButton = document.querySelector('.play');
var summaryButton = document.querySelector('.summaryButton');
var title = document.querySelector('.title');

var pitch = document.querySelector('#pitch');
var pitchValue = document.querySelector('.pitch-value');
var rate = document.querySelector('#rate');
var rateValue = document.querySelector('.rate-value');

var voices = [];

function populateVoiceList() {
    if (typeof speechSynthesis === 'undefined') {
        return;
    }
    voices = speechSynthesis.getVoices();

    var selectedIndex = voiceSelect.selectedIndex < 0 ? 0 : voiceSelect.selectedIndex;
    voiceSelect.innerHTML = '';
    for (var i = 0; i < voices.length; i++) {
        var option = document.createElement('option');
        option.textContent = voices[i].name + ' (' + voices[i].lang + ')';

        if (voices[i].default) {
            option.textContent += ' -- DEFAULT';
        }

        option.setAttribute('data-lang', voices[i].lang);
        option.setAttribute('data-name', voices[i].name);
        voiceSelect.appendChild(option);
        $(voiceSelect).formSelect();
    }
    voiceSelect.selectedIndex = selectedIndex;
    voices = speechSynthesis.getVoices();

    console.log("finished populateVoiceList");
}

populateVoiceList();
var spoken = false;
if (typeof speechSynthesis !== 'undefined' && speechSynthesis.onvoiceschanged !== undefined) {
    speechSynthesis.onvoiceschanged = function() {
        populateVoiceList();
        if (!spoken) {
            speakSummary();
            spoken = true;
        }
    };
}


var summary = document.querySelector('#summary');

function speakSummary() {
    if (summary.innerHTML !== '') {
        var context = "Your query is being parsed. Provenance loading. 80 percent. Finished. "
        context = context + "OpenNEX AI digital assistant has one recommendation for you. " + title.innerHTML.split("_").join("") + ". " + summary.innerHTML + ". ";
        // Check if there are related services. If so, speak them out.
        var relatedServices = $(".s1t");
        if (relatedServices.length != 0) {
            context = context + "I think you might also be interesting in the following services. "
            for (var i = 0; i < relatedServices.length; i++) {
                context = context + $($(".s1t")[i]).html() + ". " + $($(".s1")[i]).html() + ". ";
            }
        }
        var utterThis = new SpeechSynthesisUtterance(context);
        var selectedOption = voiceSelect.selectedOptions[0].getAttribute('data-name');
        console.log(selectedOption);
        // Microsoft Zira Desktop - English (United States)
        for (i = 0; i < voices.length; i++) {
            if (voices[i].name === selectedOption) {
                utterThis.voice = voices[i];
            }
        }
        utterThis.pitch = pitch.value;
        utterThis.rate = rate.value;
        speechSynthesis.speak(utterThis);
    }
}

function speak() {
    if (inputTxt.value !== '') {
        var utterThis = new SpeechSynthesisUtterance(inputTxt.value);
        var selectedOption = voiceSelect.selectedOptions[0].getAttribute('data-name');
        console.log(selectedOption);
        // Microsoft Zira Desktop - English (United States)
        for (i = 0; i < voices.length; i++) {
            if (voices[i].name === selectedOption) {
                utterThis.voice = voices[i];
            }
        }
        utterThis.pitch = pitch.value;
        utterThis.rate = rate.value;
        speechSynthesis.speak(utterThis);
    }
}

playButton.onclick = function (event) {
    console.log("hit speak!")
    // event.preventDefault();

    speak();

    inputTxt.blur();
}

summaryButton.onclick = function (event) {
    console.log("hit speak summary!")
    speakSummary();
    inputTxt.blur();
}

pitch.onchange = function () {
    pitchValue.textContent = pitch.value;
}

rate.onchange = function () {
    rateValue.textContent = rate.value;
}

voiceSelect.onchange = function () {
    speak();
}
