
function createCookie(name,value,days) {
   if (days) {
      var date = new Date();
      date.setTime(date.getTime()+(days*24*60*60*1000));
      var expires = "; expires="+date.toGMTString();
   }
   else var expires = "";
   document.cookie = name+"="+value+expires+"; path=/";
}

function alterCookie(name) {
   createCookie(name, "THISISBROKEN");
}

function eraseCookie(name) {
   createCookie(name, "", -1);
}

function xmlToString(xmlObject) {
   if (window.ActiveXObject)
      return xmlObject.xml;
   else
      return (new XMLSerializer()).serializeToString(xmlObject);
}

function getResponseEntityAsString(xmlhttp) {
   if (xmlhttp.responseText && xmlhttp.responseText != "")
      return xmlhttp.responseText;
   if (xmlhttp.responseXml)
      return xmlToString(xmlhttp.responseXML);
   return "";
}

function makeBasicAuth(user, password) {
   var token = user + ':' + password;
   var encodedToken = Base64.encode(token);
   return "Basic " + encodedToken;
}

function clearRequest() {
   document.requestForm.description.value = "";
   document.requestForm.relativeUrl.value = "";
   document.requestForm.httpVerb.value = "";
   document.requestForm.user.value = "";
   document.requestForm.password.value = "";
   document.requestForm.contentType.value = "";
   document.requestForm.entity.value = "";
}

function clearResponse() {
   document.getElementById("response").value = "";
}

function clearRequestAndResponse() {
   clearRequest();
   clearResponse();
}

function sendRequest() {

   // Validate input first
   if (document.requestForm.httpVerb.value == "") {
      alert("HTTP verb can't be empty.");
      return;
   }

   // Initializing response panel
   document.getElementById("response").value = "Waiting...";

   // Building the request
   xmlhttp = new XMLHttpRequest();
   xmlhttp.open(document.requestForm.httpVerb.value,
                encodeURI(document.requestForm.relativeUrl.value), false);

   // Adding user and password to the request
   if ((document.requestForm.user.value != "") &&
       (document.requestForm.password.value != "")) {
      auth = makeBasicAuth(document.requestForm.user.value,
                           document.requestForm.password.value);
      xmlhttp.setRequestHeader('Authorization', auth);
   }

   // Adding the entity and content-type to the request, if necessary
   if (document.requestForm.entity.value == "") {
      xmlhttp.send();
   } else {
      contentType = document.requestForm.contentType.value;
      xmlhttp.setRequestHeader("Content-Type", contentType);
      xmlhttp.send(document.requestForm.entity.value);
   }

   // Building the response to be written to the panel
   responseText = 'Status: ' + xmlhttp.status + ' ' + xmlhttp.statusText + '\n';
   responseText += xmlhttp.getAllResponseHeaders() + '\n';
   responseText += getResponseEntityAsString(xmlhttp);

   // Adding the response to the panel
   document.getElementById("response").value = responseText;
}
