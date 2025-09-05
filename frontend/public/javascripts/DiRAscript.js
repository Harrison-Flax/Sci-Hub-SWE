
// new_element=document.createElement("script");
// new_element.setAttribute("type","text/javascript");
// new_element.setAttribute("src","./speechRecognition.js");// 在这里引入了a.js
// document.body.appendChild(new_element);

import { startRecognition, getRecognitionInstance, startSpeaking } from "./speechRecognition.js";


var diagnostic = document.querySelector('.result');
var logs = document.querySelector('.logs');
var bg = document.querySelector('html');
var firstRes = document.querySelector('.firstRes');

var lati = "";
var longi = "";

function getLocation() {
    return new Promise((resolve, reject) => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition((position) => {
                lati = position.coords.latitude;
                longi = position.coords.longitude;
                resolve();
            });
        } else {
            alert("Geolocation is not supported by this browser.");
            reject();
        }
    });
}

$("#optRadio").click(function() {
    var val = $(this).prop('checked');
    if (val) {
        $('#voiceUI').hide();
        $('#textUI').show();
    } else {
        $('#textUI').hide();
        $('#voiceUI').show();
    }
});

var res = '';
var recognition = null;
document.querySelector('#speak').onclick = speakToggle;

async function speakToggle() {
    recognition = getRecognitionInstance();
    await startRecognition(recognition, speechAction, speechStop, speechError);

    $("#speak").addClass("disabled");

    logs.innerHTML = 'I am listening...';
    console.log('Ready to receive a command.');
    recognition = null;
}

document.querySelector('#sendText').onclick = async function() {
  await getLocation();
  $("#confirmRow").hide();
  $("#speak").removeClass("disabled");
  console.log('Yes clicked confirmYes');
  res = $("#textInput").val();
  var d = {
     "voice": res,
     "Lat": lati,
     "Lon": longi
  };
  $.ajax({
   url: '/dataset/intentMessager',
   data: JSON.stringify(d),
   error: function(error) {
      logs.innerHTML = 'I am sorry. Got some internal errors while processing...';
      startSpeaking("I am sorry. Got some internal errors while processing... ");
   },
   headers: {
     'Content-Type': 'application/json'
   },
   dataType: 'text',
   type: 'POST'
  }).done(function(data) {
      showMATAAnswer(data);
  });
};


document.querySelector('#sendVoiceText').onclick = function() {
  $("#confirmRow").hide();
  $("#speak").removeClass("disabled");
  console.log('Yes clicked confirmYes');
  var d = {
     "voice": res,
     "Lat": lati,
     "Lon": longi
  };
  $.ajax({
   url: '/dataset/intentMessager',
   data: JSON.stringify(d),
   error: function(error) {
      logs.innerHTML = 'I am sorry. Got some internal errors while processing...';
      startSpeaking("I am sorry. Got some internal errors while processing... ");
   },
   headers: {
     'Content-Type': 'application/json'
   },
   dataType: 'text',
   type: 'POST'
  }).done(function(data) {
    showMATAAnswer(data);
  });
};


function showMATAAnswer(data) {
    data = JSON.parse(data);
    console.log("data received!");
    console.log("response", data);
    try {
        let reply = data.replies[0];
        firstRes.innerHTML = reply;
    } catch (e) {
        firstRes.innerHTML = data.replies[0];
    }
    startSpeaking(data.replies[0]);

    var historyListString = "History: ";
    document.querySelector('.confidence').innerHTML = "<p>" + "<b>Conversation History: </b>" + "</p>";
    for (var i = data.history.length-1; i>= 0; i--) {
        if (data.history[i].type === 1) {
            document.querySelector('.confidence').innerHTML += "<p>";
            document.querySelector('.confidence').innerHTML += "<b>Question: </b> " + data.history[i]["rawText"] + "? ";
            document.querySelector('.confidence').innerHTML += "</p>";
        }
        if (data.history[i].type === 0) {
            document.querySelector('.confidence').innerHTML += "<p>";
            document.querySelector('.confidence').innerHTML += "<b>Answer: </b> " + data.history[i]["rawText"] + " ";
            document.querySelector('.confidence').innerHTML += "</p>";
        }
        // historyListString += data.history[i].json;
    }
    diagnostic.textContent = '';
}


document.querySelector('#declineSendVoiceText').onclick = function() {
  $("#confirmRow").hide();
  $("#speak").removeClass("disabled");
  diagnostic.textContent = '';
  document.querySelector('.confidence').innerHTML = '';
  logs.innerHTML = 'Ok then. Please click and speak again..';
};


async function speechAction(resRecognition) {

  res = resRecognition;
  await getLocation();
  console.log("Latitude: " + lati + "<br>Longitude: " + longi);
  console.log("recording finished");
  $("#confirmRow").show();
  diagnostic.textContent = 'You said: ' + res + '.';
  bg.style.backgroundColor = res;
  // document.querySelector('.confidence').innerHTML = 'Confidence level: ' + event.results[0][0].confidence;
  // console.log('Confidence: ' + event.results[0][0].confidence);
  logs.innerHTML = 'Please click the "Yes" button if I understand you correctly.';
};


function speechStop() {
    logs.innerHTML = 'I am stopped...as you have finished.';
};

function speechError(errorMsg) {
    diagnostic.textContent = 'Error occurred in recognition: ' + errorMsg;
};